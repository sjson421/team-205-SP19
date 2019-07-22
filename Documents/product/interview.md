## MSD205SP19-12

### Interview with Professor (Product Owner) to get more clarity on certain concepts

1. The difference between group and channel

   A group refers to a group of users. The way one messages a group is via a channel.

2. The difference between following and joining the group

   To follow a group, the following features must exist:
     - A group member must be able to post a public announcement on behalf of the group
     - A user can _follow_ public messages. These should appear in something that serves as a 'feed', where public messages that all groups and users a user follows will be displayed
     - A user can also join a group - this enables them to message the group members in the channel
     - Users can also make public posts individually - so users should be able to follow specific users

3. Channel password set by who? When is the password used? When member join the channel? When member login the channel?

    - The channel password should be set by the user entering the channel - this provides a layer of security for certain channels. 
    - It is the group administrator that sets a password requirement for a channel - once this is enabled, all members of the group will have to configure a password for the channel that they use to access it.

Other notes:
- Potential differentiation between Group Administrator (who may not need to be in the group) and Moderator (a role that may be more focused on content). For the time being, it makes sense to have one role that allows for all functionalities
- Usage of Strategy/Visitor patterns to apply features like parental controls, or message forwarding