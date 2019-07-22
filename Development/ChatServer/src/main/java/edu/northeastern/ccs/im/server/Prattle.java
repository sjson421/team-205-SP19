package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.communications.NetworkConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. It
 * does not send a response when the user has gone off-line.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public abstract class Prattle {

    /**
     * Prattle log
     */
    private static final Logger LOG = LogManager.getLogger(Prattle.class);

    /**
     * Don't do anything unless the server is ready.
     */
    private static boolean isReady = false;

    /**
     * service related with group in DB
     */
    private static GroupService groupService;

    /**
     * Collection of threads that are currently being used.
     */
    private static ConcurrentLinkedQueue<ClientRunnable> active;

    /**
     * All of the static initialization occurs in this "method" */
    static {
        // Create the new queue of active threads.
        active = new ConcurrentLinkedQueue<>();
        groupService = new GroupService();
    }

    /**
     * set active runnables
     * @param active list of runnables
     */
    @SuppressWarnings("squid:S1319") // type of legacy code, do not change before testing it will work
    public static void setActive(ConcurrentLinkedQueue<ClientRunnable> active) {
        Prattle.active = active;
    }

    /**
     * set group service
     * @param groupService groupService
     */
    public static void setGroupService(GroupService groupService) {
        Prattle.groupService = groupService;
    }

    /**
     * Broadcast a given message to all the other IM clients currently on the
     * system. This message _will_ be sent to the client who originally sent it.
     *
     * @param message Message that the client sent.
     */
    public static void broadcastMessage(Message message) {
        // Loop through all of our active threads
        for (ClientRunnable tt : active) {
            // Do not send the message to any clients that are not ready to receive it.
            if (tt.isInitialized()) {
                tt.enqueueMessage(message);
            }
        }
    }

    /**
     * It is a send to group msg, it will use group id to check all users in that group
     * for now only online users will be delivered
     * <p>
     * Use HashTable to check user of the client
     *
     * @param message the message to send
     */
    public static void sendToGroup(Message message, Group group) {
        for (ClientRunnable tt : active) {
            if (!tt.isInitialized()) {
                continue;
            }
            User user = tt.getUser();
            if (groupService.containsUser(group, user)) {
                tt.enqueueMessage(message);
            }
        }
    }

    public static void sendToUser(Message message, User user) {
        for (ClientRunnable tt : active) {
            if (!tt.isInitialized()) {
                continue;
            }
            User activeUser = tt.getUser();
            if (user.getUsername().equals(activeUser.getUsername())) {
                tt.enqueueMessage(message);
            }
        }
    }

    /**
     * Send message to only a list of people.
     * @param users a list of user
     * @param msg the message needed to be sent out
     */
    //Emma
    public static void sendToAListOfPPL(List<User> users, Message msg) {
        List<String> usersName = users
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        for (ClientRunnable tt : active) {
            if (usersName.contains(tt.getName())) {
                tt.enqueueMessage(msg);
            }
        }
    }

    /**
     * Remove the given IM client from the list of active threads.
     *
     * @param dead Thread which had been handling all the I/O for a client who has
     *             since quit.
     */
    public static void removeClient(ClientRunnable dead) {
        if (!active.remove(dead)) {
            LOG.info("Could not find a thread that I tried to remove!\n");
        }
    }

    /**
     * Terminates the server.
     */
    public static void stopServer() {
        isReady = false;
    }

    /**
     * Terminates the server.
     */
    public static void startServer() {
        isReady = true;
    }

    public static boolean getReady() {
        return isReady;
    }

    /**
     * Start up the threaded talk server. This class accepts incoming connections on
     * a specific port specified on the command-line. Whenever it receives a new
     * connection, it will spawn a thread to perform all of the I/O with that
     * client. This class relies on the server not receiving too many requests -- it
     * does not include any code to limit the number of extant threads.
     *
     * @param args String arguments to the server from the command line. At present
     *             the only legal (and required) argument is the port on which this
     *             server should list.
     * @throws IOException Exception thrown if the server cannot connect to the port
     *                     to which it is supposed to listen.
     */
    public static void main(String[] args) {
        // Connect to the socket on the appropriate port to which this server connects.
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            // Create our pool of threads on which we will execute.
            ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(ServerConstants.THREAD_POOL_SIZE);
            // If we get this far than the server is initialized correctly
            isReady = true;

            LOG.info("Server is now running on port 4545!");

            // Now listen on this port as long as the server is ready
            runLoop(selector, serverSocket, threadPool);
        } catch (IOException ex) {
            LOG.error("Fatal error: " + ex.getMessage());
            throw new IllegalStateException(ex.getMessage());
        }
    }

    protected static void runLoop(Selector selector, ServerSocketChannel serverSocket, ScheduledExecutorService threadPool) throws IOException {
        while (isReady) {
            // Check if we have a valid incoming request, but limit the time we may wait.
            while (selector.select(ServerConstants.DELAY_IN_MS) != 0) {
                // Get the list of keys that have arrived since our last check
                Set<SelectionKey> acceptKeys = selector.selectedKeys();
                // Now iterate through all of the keys
                Iterator<SelectionKey> it = acceptKeys.iterator();
                while (it.hasNext()) {
                    // Get the next key; it had better be from a new incoming connection
                    SelectionKey key = it.next();
                    it.remove();
                    // Assert certain things I really hope is true
                    // Need to reduce impossible condition like assert (true) -> raise error
                    if (!key.isAcceptable()) {
                        throw new AssertionError();
                    }
                    // Need to reduce impossible condition like assert (true) -> raise error
                    if (key.channel() != serverSocket) {
                        throw new AssertionError();
                    }
                    // Create new thread to handle client for which we just received request.
                    createClientThread(serverSocket, threadPool);
                }
            }
        }
    }

    /**
     * Create a new thread to handle the client for which a request is received.
     *
     * @param serverSocket The channel to use.
     * @param threadPool   The thread pool to add client to.
     */
    private static void createClientThread(ServerSocketChannel serverSocket, ScheduledExecutorService threadPool) {
        try {
            // Accept the connection and create a new thread to handle this client.
            SocketChannel socket = serverSocket.accept();
            // Make sure we have a connection to work with.
            if (socket != null) {
                NetworkConnection connection = new NetworkConnection(socket);
                ClientRunnable tt = new ClientRunnable(connection);
                // Add the thread to the queue of active threads
                active.add(tt);
                // Have the client executed by our pool of threads.
                ScheduledFuture<?> clientFuture = threadPool.scheduleAtFixedRate(tt, ServerConstants.CLIENT_CHECK_DELAY,
                        ServerConstants.CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
                tt.setFuture(clientFuture);
            }
        } catch (AssertionError ae) {
            LOG.error("Caught Assertion: " + ae.toString());
        } catch (IOException e) {
            LOG.error("Caught Exception: " + e.toString());
        }
    }
}