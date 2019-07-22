## Technical Requirements Document

This document is a summary of the requirements that our system *must* meet.


### System Requirements pertaining to Users/Groups/Channels

- The system *must* support direct messages between individual users.

- The system *must* support groups and channels, that *must* contain at least one user. 

- The system *must* support group and channel messages, where all members of the group or channel will receive the message. 

- The system *must* be able to allow users to request to join groups and channels. 

- The system *must* allow for groups and channels to have at least one moderator, who *must* be able to:
    - Decide whether users of the group/channel can invite others to the channel
    - Decide whether invitation requests, by other members of the group/channel, require moderator approval
    - Remove a member from the group/channel
    - Delete a group/channel they moderate
    - Accept an invitation from a group/channel member to become sub-channel of the channel
    - Set a master password for the channel, that every member has to type to enter the channel when they launch the program. 
    
- The system *must* allow channel members to invite another user to join the channel

- The system *must* allow channel members to invite another channel to join the channel

- The system *must* allow users to friend or follow other users or groups. 

- The system *must* support group or contact search.

- The system *must* use a database to store user data

- The system *must* support logins through Husky and LinkedIn. 

- The system may support login through Facebook

- The system *must* allow users to set a set of profile images

- The system *must* allow users to configure which profile image to show to different viewers. 

### System Requirements pertaining to Communicating

- The system *must* support group members send or reply messages to the entire group. 

- The system should support reply only to the sender, reply to a subset of the group in group messaging.

- The system may enable message forwarding. In the case of message forwarding, the system should allow the originator of a message trace where did the message go. 

- The system may enable users to start message threads or subchannels for any specific message topic. 

- The system may allow the originators of the message to set an expiration time for any follow-up messages.

- The system *must* store all messages forever. The system should offer 2 different tiers for how long messages are exposed:
    - A free tier, which exposes messages based on some time or message count metric
    - A paid tier, which leaves messages exposed

- The system *must* (use an encoding that will) allow for various character sets, specifically, those used in English, French, and Spanish. The system may support additional Asian character sets.

- The system may be capable of cross-language translation messages.

- The system *must* allow for messages to include media such as emoji, audio files, recordings, video files.

- The system should support message encryption. Additionally, the system may allow for one-time message encryption.

- The system *must* queue messages for offline users in the order that the messages are sent. The system *must* only deliver messages to an online user. The system should allow messages to be deleted from the message queue if the message had not yet been delivered, from the receiver’s if the message had been delivered but not read, and even if the message had been delivered and read).

### System Requirements pertaining to Government and Compliance

- The system *must* not break the laws in the US and *must* not break local laws in other countries where the system will be used.

- The system *must* keep a record of communications traffic including the message, from and to IP address. The system *must* keep a record of when someone logs in. The system *must* be able to provide a copy of all information above to the government.

- The system *must* not notify users that they are being monitored.

- The system may not decrypt encrypted messages when it provides the copy. The system should pass encrypted messages from user to user directly.

### Miscellaneous System Requirments

- The system should allow users to pick filter they prefer to filter coming-in messages. The system should allow users to choose behavior for messages failing to pass the filter, e.g. the message will be blocked or delivered with offensive material marked out or simply flagged.

- The system should allow users to create private messages that cannot be forwarded or cut and pasted. The system *must* make private messages encrypted during transport and storage.

- The system should allow hashtags in messages to help user categorize them.

- The system should have an option to schedule meeting/events and ask for RSVPs.
