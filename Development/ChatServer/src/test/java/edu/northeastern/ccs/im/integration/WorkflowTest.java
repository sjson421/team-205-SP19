package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.communications.NetworkConnection;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.MessageService;
import edu.northeastern.ccs.im.services.UserService;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("squid:S2187") // this is in test package
/**
 * This class helps abstract away the items needed to be mocked when doing integration testing of
 * Prattle messaging.
 * <p>
 * Using this class we can mock the ClientRunnable, and queue up messages that the client runnable
 * can process. At the same time, the mocked clientRunnable is given access to real services, so
 * we are able to test end-to-end persistence of messages, ultimately meaning that we can show a
 * message from the "client" is actually causing things to be stored in the database (not mocked).
 */
public class WorkflowTest {
    private NetworkConnection connection;
    private ClientRunnable clientRunnable;

    WorkflowTest() {
        this.setUp();
    }

    /**
     * Sets up the mock ClientRunnable - mocks the Connection and Scheduled Future, while giving it real
     * Services to access the DAOs.
     */
    private void setUp() {
        connection = mock(NetworkConnection.class);
        ScheduledFuture future = mock(ScheduledFuture.class);
        clientRunnable = new ClientRunnable(connection);

        clientRunnable.setFuture(future);

        clientRunnable.setUserService(new UserService());
        clientRunnable.setGroupService(new GroupService());
        clientRunnable.setMsgService(new MessageService());
        clientRunnable.setInviteService(new InvitationService());
    }

    /**
     * A convenience method that allows us to dynamically queue messages that the ClientRunnable will process,
     * also mocking the incoming message iterator's hasNext() to return true every time it is called until the
     * last message queued up has been processed.
     *
     * @param messages Messages from the "client"
     */
    public void queueIncomingMessages(Message... messages) {
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);

        if (messages.length == 0) {
            when(iteratorForIncomingMessage.hasNext()).thenReturn(false);
        } else if (messages.length == 1) {
            when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
            when(iteratorForIncomingMessage.next()).thenReturn(messages[0]);
        } else {
            Boolean[] restHasNexts = new Boolean[messages.length];
            Message[] rest = Arrays.copyOfRange(messages, 1, messages.length);

            for (int i = 0; i < restHasNexts.length - 1; i++) {
                restHasNexts[i] = true;
            }
            restHasNexts[restHasNexts.length - 1] = false;

            when(iteratorForIncomingMessage.hasNext()).thenReturn(true, restHasNexts);
            when(iteratorForIncomingMessage.next()).thenReturn(messages[0], rest);
        }
    }

    /**
     * Allows us to either test login or bypass it altogether.
     *
     * @param initalized whether the client has been initialized (whether a user is logged in) - true if a user is
     *                   logged in (set it to true to bypass logging in), false if login is needed.
     */
    public void setClientRunnableInitalized(boolean initalized) {
        clientRunnable.setInitialized(initalized);
    }

    /**
     * Sets the ClientRunnable's user to the given User object.
     *
     * @param user The user tied to the ClientRunnable
     */
    public void setClientRunnableUser(User user) {
        clientRunnable.setUser(user);
    }

    /**
     * Runs the workflow (calls ClientRunnable, which should now be configured to process all Messages needed for
     * this workflow).
     */
    public void run() {
        clientRunnable.run();
    }

    private Iterator<Message> getMockedIterator() {
        return mock(Iterator.class);
    }
}
