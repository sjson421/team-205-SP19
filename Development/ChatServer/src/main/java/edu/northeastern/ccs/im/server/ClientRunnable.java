package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.communications.NetworkConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.InvitationStatus;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.MessageService;
import edu.northeastern.ccs.im.services.UserService;
import edu.northeastern.ccs.im.utils.PasswordHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class ClientRunnable implements Runnable {
    private static final Logger LOG = LogManager.getLogger(ClientRunnable.class);
    private static final String USER_NAME = "user_name";
    private static final String RECIPIENT_NAME = "recipient_name";
    private static final String MSG_TYPE = "msg_type";
    private static final String SENDER_NAME = "sender_name";
    private static final String TEXT = "text";
    private static final String GROUP_NAME = "group_name";
    private static final String INVITE_MSG = "Invite ";
    private static final String INVITE_MSG_PART2 = " to join the group ";

    /**
     * Utility class which we will use to send and receive communication to this
     * client.
     */
    private NetworkConnection connection;


    /**
     * Name that the client used when connecting to the server.
     */
    private String name;

    /**
     * User for this client Runnable
     */
    private User user;

    /**
     * Whether this client has been initialized, set its user name, and is ready to
     * receive messages.
     */
    private boolean initialized;

    /**
     * Whether this client has been terminated, either because he quit or due to
     * prolonged inactivity.
     */
    private boolean terminate;

    /**
     * The timer that keeps track of the clients activity.
     */
    private ClientTimer timer;

    /**
     * The future that is used to schedule the client for execution in the thread
     * pool.
     */
    private ScheduledFuture<?> runnableMe;

    /**
     * Collection of messages queued up to be sent to this client.
     */
    private Queue<Message> waitingList;

    /**
     * User Dao
     */
    private UserService userService;

    /**
     * Group Service
     */
    private GroupService groupService;
    private MessageService msgService;
    //Emma
    private InvitationService inviteService;

    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param network NetworkConnection used by this new client
     */
    public ClientRunnable(NetworkConnection network) {
        // Create the class we will use to send and receive communication
        connection = network;
        // Mark that we are not initialized
        initialized = false;
        // Mark that we are not terminated
        terminate = false;
        // Create the queue of messages to be sent
        waitingList = new ConcurrentLinkedQueue<>();
        // Mark that the client is active now and start the timer until we
        // terminate for inactivity.
        timer = new ClientTimer();
        userService = new UserService();
        groupService = new GroupService();
        msgService = new MessageService();
        //Emma
        inviteService = new InvitationService();
    }

    public void setMsgService(MessageService msgService) {
        this.msgService = msgService;
    }

    /**
     * Use the given concurrent linked queue used for testing)
     *
     * @param concurrentLinkedQueue the waiting list to use
     */
    void setWaitingList(ConcurrentLinkedQueue<Message> concurrentLinkedQueue) {
        this.waitingList = concurrentLinkedQueue;
    }

    /**
     * user dao setter
     *
     * @param service set user service
     */
    public void setUserService(UserService service) {
        this.userService = service;
    }


    public void setTimer(ClientTimer timer) {
        this.timer = timer;
    }

    /**
     * user dao setter
     *
     * @param groupService set user Dao
     */
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * user dao setter for invitation service.
     *
     * @param inviteService service for invitation feature
     */
    public void setInviteService(InvitationService inviteService) {
        this.inviteService = inviteService;
    }

    /**
     * return user
     *
     * @return user for this runnable
     */
    public User getUser() {
        return user;
    }

    /**
     * Sends the requesting user the queue of messages sent from their last logout
     */
    private void sendMessageQueue() {
        Message systemMsg = Message.makeSystemMessage("Getting queued messages...");
        sendMessage(systemMsg);
        User receiver = this.getUser();

        List<Date> logouts = receiver.getLogouts();

        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE_RECEIVER_ID_KEY, receiver.getId());
        List<edu.northeastern.ccs.im.models.Message> receiverMessages = msgService.getMessagesByMap(map);

        if (logouts == null) {
            for (int i = 0; i < receiverMessages.size(); i++) {
                edu.northeastern.ccs.im.models.Message msg = receiverMessages.get(i);
                if (!msg.isDeleted()) {
                    sendMessageList(msg, receiver);
                }
            }
        } else {
            Date lastLogout = logouts.get(logouts.size() - 1);

            for (int i = 0; i < receiverMessages.size(); i++) {
                edu.northeastern.ccs.im.models.Message msg = receiverMessages.get(i);
                if (msg.getTimestampSent().compareTo(lastLogout) > 0 && !msg.isDeleted()) {
                    sendMessageList(msg, receiver);
                }
            }
        }
        Message allSentMsg = Message.makeSystemMessage("All queued messages sent!");
        sendMessage(allSentMsg);
    }

    /**
     * Sends the requesting user his or her message history
     */
    private void sendMessageHistory() {
        Message systemMsg = Message.makeSystemMessage("Getting message history...");
        sendMessage(systemMsg);
        User receiver = this.getUser();

        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE_RECEIVER_ID_KEY, receiver.getId());
        List<edu.northeastern.ccs.im.models.Message> receiverMessages = msgService.getMessagesByMap(map);

        for (int i = 0; i < receiverMessages.size(); i++) {
            edu.northeastern.ccs.im.models.Message msg = receiverMessages.get(i);
            if (!msg.isDeleted()) {
                sendMessageList(msg, receiver);
            }
        }

        Message allSentMsg = Message.makeSystemMessage("All messages sent!");
        sendMessage(allSentMsg);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            LOG.info(e);
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessageList(edu.northeastern.ccs.im.models.Message msg, User receiver) {
        String sentText = "(" + msg.getId() + ") " + msg.getMessageBody();

        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put(MSG_TYPE, "USER");
        receiverMap.put(SENDER_NAME, msg.getSender().getUsername());
        receiverMap.put(TEXT, sentText);
        receiverMap.put(RECIPIENT_NAME, receiver.getUsername());

        Message queuedMsg = Message.makeDirectMessage(msg.getSender().getUsername(), sentText, receiverMap);

        sendIncomingDirectMessage(queuedMsg);
    }

    /**
     * Check to see for an initialization attempt and process the message sent.
     * <p>
     * It will become true after login and register
     */
    // there is no need to check init every time
    // clientRUnnbale should only check before create (login)
    private void checkForInitialization() {
        // Check if there are any input messages to read
        Iterator<Message> messageIter = connection.iterator();
        if (messageIter.hasNext()) {
            Message msg = messageIter.next();
            Map<String, String> map = msg.getMsgToInfo();

            if (msg.isRegisterMessage()) {
                registerNewUser(map);
            } else {
                login(map);
            }
        }
    }

    /**
     * Handle login, if success, there is no need to initialize
     */
    private void login(Map<String, String> info) {
        String pw = info.get("pw");
        String userName = info.get(USER_NAME);
        User userTologin = getUser(userName);

        if (userTologin == null) {
            Message msg = Message.makeSystemMessage("Login failed! There is no such user!");
            sendMessage(msg);
            return;
        }

        if (!tryLogin(userTologin, pw)) {
            Message msg = Message.makeSystemMessage("Login failed! Password is incorrect!");
            sendMessage(msg);
            return;
        }

        timer.updateAfterInitialization();
        initialized = true;
        user = userTologin;
        Message msg = Message.makeSystemMessage("Login Success!");
        sendMessage(msg);

        //add login time to user database
        this.user.addLogin(new Date());
        userService.updateUser(this.user);
    }

    /**
     * Try to get user via userName
     *
     * @param userName user name privided from client
     * @return User if find, else null
     */
    private User getUser(String userName) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_NAME, userName);
        List<User> users = userService.getUsersByMap(criteria);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    /**
     * Try to login some user
     *
     * @param user that we want to login
     * @param pw   password provided by client
     * @return true if hash equals, else false
     */
    private boolean tryLogin(User user, String pw) {
        String pwHash = user.getPwHash();
        String salt = user.getSalt();
        return PasswordHelper.verifyPassword(pw, salt, pwHash);
    }

    /**
     * info has user_name and pw field
     *
     * @param info
     * @return true if success
     */
    private void registerNewUser(Map<String, String> info) {
        String pw = info.get("pw");
        String userName = info.get(USER_NAME);
        String publicKey = info.get("public_key");
        boolean result = userService.createUser(userName, pw, publicKey);
        if (result) {
            // confirm register success
            Message msg = Message.makeSystemMessage("Register Success!");
            sendMessage(msg);
        } else {
            // register failed
            Message msg = Message.makeSystemMessage("Register Failed, use another user name");
            connection.sendMessage(msg);
        }
    }

    /**
     * Check if the message is properly formed. At the moment, this means checking
     * that the identifier is set properly.
     *
     * @param msg Message to be checked
     * @return True if message is correct; false otherwise
     */
    private boolean messageChecks(Message msg) {
        return (msg.getName() != null) && (msg.getName().compareToIgnoreCase(getName()) == 0);
    }

    /**
     * Method for storing messages, used when sending messages
     *
     * @param recipientName username of recipient
     * @param msgText       message text
     * @return Success status of message storage on database
     */
    public boolean storeMessage(String recipientName, String msgText) {
        Map<String, Object> search = new HashMap<>();
        search.put(USER_USER_NAME_KEY, recipientName);

        List<User> recipients = userService.getUsersByMap(search);

        if (!recipients.isEmpty()) {
            return msgService.createMessage(new Date(), this.getUser(), recipients.get(0), msgText);
        } else {
            return false;
        }
    }

    /**
     * Method for storing broadcast messages with recipient as null
     *
     * @param msgText message text
     * @return Success status of message storage on database
     */
    public boolean storeBroadcastMessage(String msgText) {
        return msgService.createMessage(new Date(), this.getUser(), null, msgText);
    }

    /**
     * Immediately send this message to the client. This returns if we were
     * successful or not in our attempt to send the message.
     *
     * @param message Message to be sent immediately.
     * @return True if we sent the message successfully; false otherwise.
     */
    private boolean sendMessage(Message message) {
        LOG.info("\t" + message);
        return connection.sendMessage(message);
    }

    /**
     * Add the given message to this client to the queue of message to be sent to
     * the client.
     *
     * @param message Complete message to be sent.
     */
    public void enqueueMessage(Message message) {
        waitingList.add(message);
    }

    /**
     * Get the name of the user for which this ClientRunnable was created.
     *
     * @return Returns the name of this client.
     */
    public String getName() {
        return getUser().getUsername();
    }

    /**
     * Set the name of the user for which this ClientRunnable was created.
     *
     * @param name The name for which this ClientRunnable.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return if this thread has completed the initialization process with its
     * client and is read to receive messages.
     *
     * @return True if this thread's client should be considered; false otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Perform the periodic actions needed to work with this client.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        // The client must be initialized before we can do anything else
        if (!initialized) {
            checkForInitialization();
        } else {
            handleIncomingMessages();
            handleOutgoingMessages();
        }
        // Finally, check if this client have been inactive for too long and,
        // when they have, terminate the client.
        if (timer.isBehind()) {
            LOG.error("Timing out or forcing off a user " + name);
            terminate = true;
        }
        if (terminate) {
            terminateClient();
        }
    }

    /**
     * Checks incoming messages and performs appropriate actions based on the type
     * of message.
     * <p>
     * <p>
     * Distinguish different message for toGroup, Broadcast or toUser
     */
    protected void handleIncomingMessages() {
        Iterator<Message> messageIter = connection.iterator();
        if (messageIter.hasNext()) {
            Message msg = messageIter.next();
            if (msg.terminate()) {
                terminate = true;

                enqueueMessage(Message.makeQuitMessage(name));
                String myName = this.getName();

                Map<String, String> broadcastMap = new HashMap<>();
                broadcastMap.put(MSG_TYPE, "BCT");
                broadcastMap.put(SENDER_NAME, myName);
                broadcastMap.put("text", "User " + myName + " has left the server.");

                Message quitMsg = Message.makeBroadcastMessage(myName, broadcastMap);
                sendMessage(quitMsg);
            } else {
                sendIncomingMessage(msg);
            }
        }
    }

    /**
     * @param msg Message to handle by Prattle
     */
    private void sendIncomingMessage(Message msg) {
        if (messageChecks(msg)) {
            sendValidIncomingMessage(msg);
        }
    }

    /**
     * message is valid, send for broadcast ot group
     *
     * @param msg incoming message
     */
    private void sendValidIncomingMessage(Message msg) {

        if (msg.isBroadcastMessage()) {
            storeBroadcastMessage(msg.getText());
            Prattle.broadcastMessage(msg);
        } else if (msg.isToGroup()) {
            sendIncomingGroupMessage(msg);
        } else if (msg.isCreateGroupMessage()) {
            createGroupMessage(msg);
        } else if (msg.isGetQueueMessage()) {
            sendMessageQueue();
        } else if (msg.isGetMessageHistory()) {
            sendMessageHistory();
        } else if (msg.isInvitation()) {
            processInvitationMessage(msg);
        } else if (msg.isToUser()) {
            sendIncomingDirectMessage(msg);
        } else if (msg.isDeleteMessage()) {
            sendDeleteMessage(msg);
        } else if (msg.isGetPublicKey()) {
            sendGetPublicKeyMessage(msg.getMsgToInfo());
        }
    }

    /**
     * This method process and catergoize the incoming invitation message.
     * @param msg invitation message
     */
    private void processInvitationMessage(Message msg) {
      InvitationStatus status = InvitationStatus.valueOf(msg.getMsgToInfo().get("invite_status"));

      if (status == InvitationStatus.APPROVED || status == InvitationStatus.DENIED) {
        // approve or reject invite
        processInvitationDecision(msg, status);
      } else if (status == InvitationStatus.CREATED) {
        // send the invite to admin for decision
        sendInviteToAdmin(msg);
      } else {
        enqueueMessage(Message.makeSystemMessage("The invitation status is wrong."));
      }
    }

    /**
     * Get user's public key send back to one who requests it.
     */
    private void sendGetPublicKeyMessage(Map<String, String> map) {
        String userName = map.get(RECIPIENT_NAME);
        User receiver = getUser(userName);
        if (receiver == null) {
            sendMessage(Message.makeSystemMessage("The user you want to have private conversation does not exist!"));
        } else {
            sendMessage(Message.makeReturnKeyMessage(receiver.getUsername(), receiver.getPublicKey()));
        }
    }

    /**
     * Create an invitation based on given information.
     * @param inviteeUser User. the invitee user
     * @param invitor String. the name of invitor
     * @param group Group. the group the invitee will be join in
     * @return the id of invitation if the invitation is created sucessfully. return null
     */
    private String createInvitation(User inviteeUser, String invitor, Group group) {
      String inviteID;
      if (invitor == null) {
        // ppl request to join in without being invited
        inviteID = inviteService.createInvitation(null, inviteeUser,
                group, true, null);
        // the default status for invitation is set to be "CREATED".
      } else {
        User invitorUser = userService.getUserByName(invitor);
        // ppl invite someone to join in
        inviteID = inviteService.createInvitation(invitorUser, inviteeUser,
                group, true, null);
        // the default status for invitation is set to be "CREATED".

      }

      return inviteID;
    }
    /**
     * Send the invitation message to group admin for decision.
     *
     * @param msg message
     */
    private void sendInviteToAdmin(Message msg) {
        // pre condition checking
        String groupName = msg.getMsgToInfo().get(GROUP_NAME);
        Group group = groupService.getGroupByName(groupName);
        if (group == null) {
            enqueueMessage(Message.makeSystemMessage("There is no such group: " + groupName));
            return;
        }

        String invitor = msg.getMsgToInfo().get("invitor");
        User inviteeUser = userService.getUserByName(msg.getMsgToInfo().get("invitee"));
        if (inviteeUser == null) {
            enqueueMessage(Message.makeSystemMessage("There is no invitee"));
            return;
        }

        List<User> allGroupUsers = group.getUsers();
        if (allGroupUsers.contains(inviteeUser)) {
            enqueueMessage(Message.makeSystemMessage(inviteeUser.getUsername() +
                    " is already in the group " + groupName));
            return;
        }

        if (invitor != null && !allGroupUsers.contains(user)) {
            enqueueMessage(Message.makeSystemMessage("You can't invite people because you are " +
                    "not in the group " + group.getGroupName()));
            return;
        }

        // get invitation id
        String inviteID = createInvitation(inviteeUser, invitor, group);
        if (inviteID == null) {
          enqueueMessage(Message.makeSystemMessage("The invitation is not created successfully."));
          return;
        }

        // notify admin of group about invitation
        List<User> adminList = groupService.getAdmin(group);
        for (User admin : adminList) {
            Map<String, String> map = new HashMap<>();
            map.put(MSG_TYPE, "USER");
            map.put(SENDER_NAME, user.getUsername());

            if (invitor == null) {
              // user directly request join the group
              map.put("text", INVITE_MSG + inviteID + ": " + inviteeUser.getUsername()
                      + " request to join the group " + groupName);
            } else {
              // user is invited by other user to join in
              map.put("text", INVITE_MSG + inviteID + ": " + user.getUsername() + " invite "
                      + inviteeUser.getUsername() + INVITE_MSG_PART2 + groupName);
            }

            map.put(RECIPIENT_NAME, admin.getUsername());
            Message newMsg = Message.makeDirectMessage(user.getUsername(),
                    "someone request to join the group", map);
            Prattle.sendToUser(newMsg, admin);
        }

        if (invitor != null && invitor.compareTo(user.getUsername()) == 0) {
          // current user invite other user
          enqueueMessage(Message.makeSystemMessage(INVITE_MSG + inviteID + ": you invite "
                  + inviteeUser.getUsername() + INVITE_MSG_PART2 + groupName));
        }
    }

    /**
     * Process the invitation decision from admin. If current user is not admin, current user cannot
     * process the invitation.
     *
     * @param msg    message
     * @param status the status of invitation
     */
    private void processInvitationDecision(Message msg, InvitationStatus status) {
        // current invitation
        Invitation currInvite = inviteService.getInvitationByID(msg.getMsgToInfo().get("invite_id"));

        if (currInvite == null) {
          // other admin may has already processed invitation
          enqueueMessage(Message.makeSystemMessage("Invitation doesn't exist anymore. " +
                  "This invitation either has already been processed, or not exist."));
          return;
        }

        Group group = currInvite.getGroup();
        if (group == null) {
            return;
        }

        List<User> groupAdmins = group.getAdministrators();

        // sanity check. check whether current user is indeed the admin of the group
        if (!groupAdmins.contains(user)) {
            enqueueMessage(Message.makeSystemMessage("Current user is not the admin of group "
                    + group.getGroupName()));
            return;
        }

        // update the invitation status and send message
        updateInvitationBasedOnDecision(currInvite, status, group, groupAdmins);
    }

    /**
     * Update the given invitation based on the admin decision. If the invitation has been updated
     * before, do nothing and inform user the invitation has been processed.
     *
     * @param currInvite the input invitation
     * @param status the updated status for the invitation
     * @param group the group the invitee will be joined in
     * @param groupAdmin the list of admin of that group
     */
    //Emma
    private void updateInvitationBasedOnDecision(Invitation currInvite, InvitationStatus status,
                                                 Group group, List<User> groupAdmin) {


        // update the status of invitation and add the user if need
        inviteService.updateInvitationToNewStatus(currInvite, status);

        List<User> notifyUser = new ArrayList<>(groupAdmin);
        User invitee = currInvite.getInvitee();
        notifyUser.add(invitee);
        User invitor = currInvite.getInviter();
        if (invitor != null) {
          notifyUser.add(invitor);
        }

        // delete the invitation after update
        inviteService.deleteInvitation(currInvite);

        // notify the group admin, invitee, inviter about the invitation decision
        for (User oneUser : notifyUser) {
            Map<String, String> map = new HashMap<>();
            map.put(MSG_TYPE, "USER");
            map.put(SENDER_NAME, user.getUsername());

            if (status == InvitationStatus.APPROVED) {
                // user directly request join the group
                map.put("text", invitee.getUsername() + " has been added to group "
                        + group.getGroupName());
            } else if (status == InvitationStatus.DENIED){
                // user is invited by other user to join in
                map.put("text", user.getUsername() + " denied " + invitee.getUsername() +
                      INVITE_MSG_PART2 + group.getGroupName());
            }

            map.put(RECIPIENT_NAME, oneUser.getUsername());
            Message newMsg = Message.makeDirectMessage(user.getUsername(),
                    "invitation decision", map);
            Prattle.sendToUser(newMsg, oneUser);
        }

        enqueueMessage(Message.makeSystemMessage("You have processed invite "
              + currInvite.getId()));
    }

    /**
     * send msg to some group
     *
     * @param msg msg
     */
    private void sendIncomingGroupMessage(Message msg) {
        String groupName = msg.getMsgToInfo().get(GROUP_NAME);

        Map<String, Object> map = new HashMap<>();
        map.put(GROUP_GROUP_NAME_KEY, groupName);
        List<Group> groups = groupService.getGroupsByMap(map);
        if (groups.isEmpty()) {
            enqueueMessage(Message.makeSystemMessage("There is no such group: " + groupName));
            return;
        }

        Group group = groups.get(0);
        if (groupService.containsUser(group, user)) {
            if (group != null && group.getUsers() != null) {
                for (User recipient : group.getUsers()) {
                    storeMessage(recipient.getUsername(), msg.getText());
                }
            }
            Prattle.sendToGroup(msg, group);
        } else {
            enqueueMessage(Message.makeSystemMessage("user is not in " + groupName));
        }
    }

    /**
     * send msg to some user
     *
     * @param msg msg
     */
    private void sendIncomingDirectMessage(Message msg) {
        String recipientName = msg.getMsgToInfo().get(RECIPIENT_NAME);

        Map<String, Object> map = new HashMap<>();
        map.put(USER_USER_NAME_KEY, recipientName);
        List<User> users = userService.getUsersByMap(map);
        if (users.isEmpty()) {
            enqueueMessage(Message.makeSystemMessage("There is no such user: " + recipientName));
            return;
        }

        storeMessage(recipientName, msg.getText());
        Prattle.sendToUser(msg, users.get(0));
    }

    /**
     * Send message with delete message request to database
     *
     * @param msg
     */
    private void sendDeleteMessage(Message msg) {
        Map<String, String> msgToInfo = msg.getMsgToInfo();
        String objectIdString = msgToInfo.get("message_id");
        ObjectId objectId = new ObjectId(objectIdString);

        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE_MESSAGE_ID_KEY, objectId);

        List<edu.northeastern.ccs.im.models.Message> messages = msgService.getMessagesByMap(map);
        if (messages.isEmpty()) {
            enqueueMessage(Message.makeSystemMessage("There is no such message: " + objectIdString));
        } else {
            if (msgService.deleteMessage(messages.get(0))) {
                enqueueMessage(Message.makeSystemMessage("Deleted message: " + objectIdString));
            } else {
                enqueueMessage(Message.makeSystemMessage("Could not delete message: " + objectIdString));
            }
        }
    }

    /**
     * The message that we want to send to group
     *
     * @param message message from client
     */
    private void createGroupMessage(Message message) {
        String groupName = message.getMsgToInfo().get(GROUP_NAME);

        List<User> admins = new ArrayList<>();
        admins.add(user);

        if (groupService.createGroup(groupName, admins)) {
            enqueueMessage(Message.makeSystemMessage("Group was successfully created: " + groupName));
        } else {
            enqueueMessage(Message.makeSystemMessage("Could not create group: " + groupName));
        }
    }

    /**
     * Sends the enqueued messages to the printer and makes sure they were sent out.
     * <p>
     * Message is in Queue and it need to implement strategy and visitor pattern
     */
    protected void handleOutgoingMessages() {
        // Check to make sure we have a client to send to.
        boolean keepAlive = true;
        if (!waitingList.isEmpty()) {
            keepAlive = false;
            // Send out all of the message that have been added to the
            // queue.
            do {
                Message msg = waitingList.remove();
                boolean sentGood = sendMessage(msg);
                keepAlive |= sentGood;
                // Update the time until we terminate the client for inactivity.
                timer.updateAfterActivity();

            } while (!waitingList.isEmpty());
        }
        terminate |= !keepAlive;
    }

    /**
     * set if the client is initialized
     *
     * @param initialized initialized
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * set the user of this client runnable
     *
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Store the object used by this client runnable to control when it is scheduled
     * for execution in the thread pool.
     *
     * @param future Instance controlling when the runnable is executed from within
     *               the thread pool.
     */
    public void setFuture(ScheduledFuture<?> future) {
        runnableMe = future;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    public void terminateClient() {
        User deadUser = this.getUser();

        //Add logout time to database on terminate
        if (deadUser != null) {
            deadUser.addLogout(new Date());
            userService.updateUser(deadUser);
        }

        // Once the communication is done, close this connection.
        connection.close();
        // Remove the client from our client listing.
        Prattle.removeClient(this);
        // And remove the client from our client pool.
        runnableMe.cancel(false);
    }
}