package edu.northeastern.ccs.im.communications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is similar to the java.io.PrintWriter class, but this class's
 * methods work with our non-blocking Socket classes. This class could easily be
 * made to wait for network output (e.g., be made &quot;non-blocking&quot; in
 * technical parlance), but I have not worried about it yet.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.4
 */
public class NetworkConnection implements Iterable<Message> {

    private static final Logger LOG = LogManager.getLogger(NetworkConnection.class);

    /**
     * The size of the incoming buffer.
     */
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * The default character set.
     */
    private static final String CHARSET_NAME = "us-ascii";

    /**
     * Number of times to try sending a message before we give up in frustration.
     */
    private static final int MAXIMUM_TRIES_SENDING = 100;

    /**
     * Channel over which we will send and receive messages.
     */
    private final SocketChannel channel;

    /**
     * Selector for this client's connection.
     */
    private Selector selector;

    /**
     * Selection key for this client's connection.
     */
    private SelectionKey key;

    /**
     * Byte buffer to use for incoming messages to this client.
     */
    private ByteBuffer buff;

    private CharsetDecoder decoder;

    /**
     * Queue of messages for this client.
     */
    private Queue<Message> messages;

    /**
     * Creates a new instance of this class. Since, by definition, this class sends
     * output over the network, we need to supply the non-blocking Socket instance
     * to which we will write.
     *
     * @param sockChan Non-blocking SocketChannel instance to which we will send all
     *                 communication.
     * @throws IOException Exception thrown if we have trouble completing this
     *                     connection
     */
    public NetworkConnection(SocketChannel sockChan) {
        // Create the queue that will hold the messages received from over the network
        messages = new ConcurrentLinkedQueue<>();
        // Allocate the buffer we will use to read data
        buff = ByteBuffer.allocate(BUFFER_SIZE);
        // Remember the channel that we will be using.
        // Set up the SocketChannel over which we will communicate.
        channel = sockChan;
        try {
            channel.configureBlocking(false);
            // Open the selector to handle our non-blocking I/O
            selector = Selector.open();
            // Register our channel to receive alerts to complete the connection
            key = channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            // For the moment we are going to simply cover up that there was a problem.
            LOG.error(e.toString());
            throw new AssertionError();
        }
        decoder = Charset.forName(CHARSET_NAME).newDecoder();
    }

    /**
     * set the decoder
     *
     * @param decoder decoder
     */
    public void setDecoder(CharsetDecoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Send a Message over the network. This method performs its actions by printing
     * the given Message over the SocketNB instance with which the PrintNetNB was
     * instantiated. This returns whether our attempt to send the message was
     * successful.
     *
     * @param msg Message to be sent out over the network.
     * @return True if we successfully send this message; false otherwise.
     */
    public boolean sendMessage(Message msg) {
        boolean result = true;
        String str = msg.toString();
        ByteBuffer wrapper = ByteBuffer.wrap(str.getBytes());
        int bytesWritten = 0;
        int attemptsRemaining = MAXIMUM_TRIES_SENDING;
        while (result && wrapper.hasRemaining() && (attemptsRemaining > 0)) {
            try {
                attemptsRemaining--;
                bytesWritten += channel.write(wrapper);
            } catch (IOException e) {
                // Show that this was unsuccessful
                result = false;
            }
        }
        // Check to see if we were successful in our attempt to write the message
        if (result && wrapper.hasRemaining()) {
            LOG.warn("WARNING: Sent only " + bytesWritten + " out of " + wrapper.limit()
                    + " bytes -- dropping this user.");
            result = false;
        }
        return result;
    }

    /**
     * Trade-off for testing and design
     *
     * @param selector a mockedSelector
     */
    protected void setSelector(Selector selector) {
        this.selector = selector;
    }

    /**
     * set selector key
     *
     * @param key
     */
    protected void setSelectionKey(SelectionKey key) {
        this.key = key;
    }

    /**
     * Close this client network connection.
     */
    public void close() {
        try {
            selector.close();
            channel.close();
        } catch (IOException e) {
            LOG.error("Caught exception: " + e.toString());
            throw new AssertionError();
        }
    }

    /**
     * set messages to process
     *
     * @param messages messages
     */
    protected void setMessages(Queue<Message> messages) {
        this.messages = messages;
    }

    /**
     * new message iterator
     *
     * @return new message iterator
     */
    @Override
    public Iterator<Message> iterator() {
        return new MessageIterator();
    }

    /**
     * Private class that helps iterate over a Network Connection.
     *
     * @author Riya Nadkarni
     * @version 12-27-2018
     */
    public class MessageIterator implements Iterator<Message> {

        /**
         * Default constructor.
         */
        public MessageIterator() {
            // nothing to do here
        }

        /**
         * not only check hasnext, but also put message into queue so that next can use it.
         *
         * @return
         */
        @Override
        public boolean hasNext() {
            boolean result = false;
            try {
                // If we have messages waiting for us, return true.
                if (!messages.isEmpty()) {
                    result = true;
                }
                // Otherwise, check if we can read in at least one new message
                else if (selector.selectNow() != 0) {
                    assert key.isReadable();
                    // Read in the next set of commands from the channel.
                    channel.read(buff);
                    selector.selectedKeys().remove(key);
                    buff.flip();

                    CharBuffer charBuffer = decoder.decode(buff);
                    // get rid of any extra whitespace at the beginning
                    // Start scanning the buffer for any and all messages.
                    int start = 0;
                    // Scan through the entire buffer; check that we have the minimum message size
                    throughBuffer(charBuffer);

                    // Move any read messages out of the buffer so that we can add to the end.
                    buff.position(start);
                    // Move all of the remaining data to the start of the buffer.
                    buff.compact();
                    buff.clear();
                    result = true;
                }
            } catch (IOException ioe) {
                // For the moment, we will cover up this exception and hope it never occurs.
                throw new AssertionError();
            }
            // Do we now have any messages?
            return result;
        }

        /**
         * Message will made here
         *
         * @param charBuffer charBuffer which contains json string
         */
        public void throughBuffer(CharBuffer charBuffer) {
            String json = readArgument(charBuffer);
            Message newMsg = Message.makeMessage(json);
            if (newMsg != null) {
                messages.add(newMsg);
            }
        }

        /**
         * next message
         *
         * @return next message
         */
        @Override
        public Message next() {
            if (messages.isEmpty()) {
                throw new NoSuchElementException("No next line has been typed in at the keyboard");
            }
            Message msg = messages.remove();
            LOG.info(msg.toString());
            return msg;
        }

        /**
         * Read in a new argument from the IM server.
         *
         * @param charBuffer Buffer holding text from over the network.
         * @return String holding the next argument sent over the network.
         */
        private String readArgument(CharBuffer charBuffer) {
            return charBuffer.toString();
        }
    }
}
