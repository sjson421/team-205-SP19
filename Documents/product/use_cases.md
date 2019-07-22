## Use Cases

Here we document specific use cases that we will consider when building our system.

### Use Case Diagram

At a high level, we show what use cases we expect to see with these two diagrams.

![](./uml/uml_usecase_user.png)
![](./uml/uml_usecase_monitor.png)

### Specific Use Case Outlines

#### User

- Log in / accessing system
    - Login (First time):
        - Actor: Bob
        - Preconditions: None
        - Goal in context: Creating an account and logging in
        - Trigger: Wants to use a new messaging platform, unsatisfied by the options he's used in the past
        - Steps:
            1. Bob arrives at system login page
            2. Sees "Sign Up" or "Log in" options
            3. Bob selects "Sign Up"
            4. Sees default account creation option, as well as various OAuth options
            5. Bob selects desired login method
            6. Sees "Welcome, Bob" message and is taken to a home screen, where Bob can begin using the system
    - Login (Returning):
        - Actor: Bob
        - Preconditions: None
        - Goal in context: Logging in to the system to use it.
        - Trigger: Wants to do something within the system.
        - Steps:
            1. Bob arrives at system login page
            2. Sees "Sign Up" or "Log in" options
            3. Bob selects "Login"
            4. Sees default username and password option, as well as various OAuth options
            5. Bob selects the correct login method (used at account creation time)
            6. Sees "Welcome message" and is taken to a home screen, where Bob can begin using the system
- Search by user or by group:
    - Actor: Bob
    - Preconditions: System is configured, Bob is logged in
    - Goal in context: To find Alice (or #food channel) 
    - Trigger: May want to message Alice about something
    - Steps:
        1. Bob selects "Contacts" option on his home screen.
        2. Bob sees a search bar pop up
        3. Bob types "Alice" (or #food) in search bar
        4. As he types similar options pop up below
        5. Bob looks through the options and selects the Alice (#food) he was looking for.
        6a. Bob is taken to a direct message screen with Alice, where he messags her.
        6b. Bob is taken to the #food channel, whose contents he can see. He can request to join the channel.
- Follow User/Group
    - Follow a User:
        - Actor: Bob
        - Preconditions: System configured, Bob is logged in, Bob is not yet following Alice, Bob knows how to search users (See "Search by user or by group" scenario)
        - Goal in context: Bob wants to get notified when Alice gets new followers or updates her profile
        - Trigger: Alice is a really smart and influential leader, and Bob wants to keep up with what she's up to.
        - Steps:
            1. Bob searches for Alice
            2. After the direct message view opens, Bob once again clicks on her name in the conversation, and a profile view opens on his side
            3. Bob selects the "Follow" option
    - Follow a Group:
        - Actor: Dev
        - Preconditions: System configured, Dev is logged in, Dev is not yet following the Engineering group, Dev knows how to search users (See "Search by user or by group" scenario)
        - Goal in context: Dev wants to get notified when the messages are sent to Engineers
        - Trigger: Dev is in DevOps, and wants to know when builds are breaking. He figures that engineers will be contacted when builds start failing in Jenkins, and wants to be notified of those issues.
        - Steps:
            1. Dev searchs for #engineers group
            2. Dev is shown a "group" view, where he can see the group profile
            3. He can see "Request to Get Group Notifications" for this group
            4. Dev selects "Request to Get Group Notifications"
            5. A group moderator must apporve this request. If approved, Dev will notified of the approval, and will be able to see messages in #engineers
- Direct message to other User
    - Actor: Bob
    - Preconditions: System configured, Bob is logged in, Bob knows how to search users (See "Search by user or by group" scenario)
    - Goal in context: Bob wants to be able to enter a Message view with Alice
    - Trigger: Bob wants to message Alice
    - Steps:
        1. Bob searches Alice [only needs to do this if he doesn't see a conversation with Alice in his "conversations" view panel]
        2. Opens direct message view with Alice
        3. Bob sends message with Alice
- Invite other user(s) to join group or channel
    - Actor: #engineering group member
    - Preconditions: System configured, group member is logged in
    - Goal in context: To invite a non group member to join group
    - Trigger: User wants to invite another member to #engineers
    - Steps:
        1. User navigates to #engineering group message view
        2. User clicks on group settings
        3. User clicks "Members"
        4. User sees current member list and an option to "Invite user"
        5. User selects "Invite user", and a view with a search bar appears.
        6. User types name of user to add, and sends request
        7. (The request may need to be approved by moderator if group settings are configured as such)
        8. (Other user should be able to accept invitation)
- Create/see profile picture(s)
    - Actor: User
    - Preconditions: System configured, user is logged in
    - Goal in context: User wants to change default profile picture
    - Trigger: User wants to change default profile picture
    - Steps:
        1. User navigates to "settings"
        2. User selects "Profile"
        3. User selects existing picture, and clicks "Edit"
        4. A view with all active profile pictures appears
        5. User can add, or remove existing profile pictures
        6. User selects "+", and a view that shows different file upload options arrives
        7. User selects appropriate option, e.g. "Browse files from Computer"
        8. User selects the picture to upload, and hits Enter
        9. User selects picture view permissions. 
- Send message and reply to sub group
    - Actor: Bob
    - Preconditions: System is configured, Bob is logged in, Bob is currently in a group message view
    - Goal in context: To be able to respond to a select message and start a sub-channel that alerts only members participating in the sub-channel
    - Trigger: Bob wants to be able to thread messages, so as not to send messages to everyone in a group for whom a topic does not pertain
    - Steps:
        1. Bob hovers over a message to reply to
        2. Bob sees "Create Thread" option
        3. Bob selects "Create Thread"
        4. A "thread" view panel opens on the side where Bob can reply to the message.
        5. Bob types his reply, notifying only the message sender
        6. (Other users can reply to Bob, and only Bob and the message sender get notificiations, etc.)
- Send message and reply to channel
    - Actor: Bob
    - Preconditions: System is configured, Bob is logged in, Bob is currently in a group message view
    - Goal in context: To be able to reply to a message so that al group members can see
    - Trigger: Bob wants to continue a conversation that everyone should also be a part of
    - Steps:
        1. Bob hovers over a message to reply to
        2. In the "group message" view, Bob sees a bar where he can type text
        3. Bob types his reply in the bar and hits "Enter"
        4. The message is sent, and appears under the previous message, for all group members to see.

- Request to join group or channel (Same as "Follow a Group" use case, for now)
- Receive a group or channel message
    - Primary Actor: User - Ada
    - Goal in context: Ada receive and read Engineering group  message
    - Preconditions: Ada must be in the Engineering group, and Ada has configured to receive notification of that group
    - Trigger: when there is a group message
    - Scenario:
        - Receive the notification of engineering group message
        - Ada click the notification
        - The screen will show the group chat UI
        - Ada is now able to view all the group messages
    
- Invite another group or channel to join a specific group or channel
    - Primary Actor: User - Grace
    - Goal in context: Grace want to invite the DevOps group to join in the Engineering group
    - Preconditions: Grace must already in the Engineering group. And the moderator of the engineering group has set the group setting to not require moderator approval before sending the invite. And the group is not having a password.
    - Trigger: Grace is creating a group for all the engineering teams. 
    - Scenario:
        - Grace click “+” at the right top corner of the group
        - Select the DevOps group 
        - Click “apply”
        - The system will show the message to ask Grace to confirm send the invite to DevOps group to join the group
        - Grace click “confirm”

#### Moderator
- Approve user to join group
    - Primary Actor: Moderator
    - Goal in context: Approve user to join group
    - Preconditions: User must be login in and must be the moderator of the group or channel
    - Trigger: a user sent a request to join the group
    - Scenario:
        - The system shows a dialog that says xxx user is requesting to join the group
        - Moderator review user’s request message and click user’s name to look into user’s profile
        - Moderator either “accept” or “reject” the request
            - If the moderator accepts the request, the group member will be notified
            - If the moderator rejects the request, the moderator has the option to enter reject reason
    - Exception:
        - User’s profile cannot be open. The system displays the error message

- Delete user from group or channel
    - Primary Actor: Moderator
    - Goal in context: Permanently delete a group or channel
    - Preconditions: User must be login in and must be the moderator of the group or channel
    - Trigger: Moderator want to remove someone from the group or channel for some reason
    - Scenario: 
        - Moderator go to the user list of the group or channel
        - Moderator Select the user wants that he or she want to remove
        - Right-click and then select “remove from group”
        - System shows a dialog that confirms the deletion of a certain user
        - Moderator confirm the deletion

- Permanently delete group or channel
    - Primary Actor: Moderator
    - Goal in context: Permanently delete a group or channel
    - Preconditions: User must be login in and must be the moderator of the group or channel
    - Trigger: Moderator hopes to dismiss the group or channel because a new group/channel has been created or the current group/channel is no longer in use.
    - Scenario: 
        - Moderator go to setting of group or channel
        - Moderator scroll down to the bottom of the setting page
        - Select “Delete Group/Channel”
        - The system shows a dialog that asks the moderator to type the group name to confirm the deletion
        - Modeator type the group name and click “confirm”
     - Exception:
        - Moderator type the group name wrong. The system shows the group name is typed wrong and terminate the deletion process

- Set password for a channel
    - Primary Actor: Moderator
    - Goal in context: Set the password for a channel. A user will need to type channel’s password before requesting to join the channel
    - Preconditions: User must be login in and must be the moderator of the group
    - Trigger: Moderator add an extra layer of security for the channel
    - Scenario: 
        - Moderator go to the setting menu of the group 
        - Modera scroll down to the security/password field
        - Click set password
        - Moderator type the new password twice to confirm the new password
        - Click “apply”
    - Exception:
        - When the passwords typed in are inconsistent. The system notifies moderator two passwords are different and asks the moderator to re-type


- Configure group member invitation settings (to not require moderator approval before sending invite)
    - Primary Actor: Moderator
    - Goal in context: Change group member invitation settings (to not require moderator approval before sending the invite)
    - Preconditions: User must be login in and must be the moderator of the group
    - Trigger: Moderator want to change the group setting
    - Scenario: 
        - Moderator go to the setting menu of the group
        - Moderator go to the field of invitation setting
        - Change the setting from “need approve” to “no need approve”
        - Click “Apply” to apply the changes
        - Enter group password
    - Exceptions: 
        - Moderator type the wrong password. The system will allow the moderator type wrong password up to three times. More than three times, the moderator will be prevented from taking any action temporarily.

- Accept an invitation to join another group/channel as a group
    - Primary Actor: Moderator
    - Goal in context: To join or not join another group/channel as a group
    - Preconditions: User must be login in and must be the moderator of the group
    - Trigger: Receive an invitation to join another group/channel as a group
    - Scenario: 
        - The system shows a group invitation dialog
        - Moderator click group’s name in the dialog to view that group’s profile
        - Moderator select either “confirm” or “reject” the group invitation
        - The system shows another dialog to ask the moderator to confirm the action 
        - Moderator enter the group password to start the action
        - All group member is notified about the moderator’s action
        - The user who sent the invitation will be notified about the moderator’s action as well

Exceptions: 
    - A group may have multiple moderators. The moderator who reacts first will be the decision maker. Then other moderators will be notified that a decision has been made and are prohibited to change the decision. 
    - Moderator type the wrong password. The system will allow the moderator type wrong password up to three times. More than three times, the moderator will be prevented from taking any action temporarily.
