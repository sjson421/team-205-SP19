# Schema for our DB (assuming we use MongoDB)

## Users and Groups

### User
```
{
    'user_id' : int,
    'username' : string,
    'password' : string (plain-text for now),
    'name' : string,
    'friends' : [user_id (int) (ref User), ...],
    'followedBy' : [user_id (int)  (ref User), ...],
    'following' : [user_id (int)  (ref User), ...],
    'conversations' : [conversation_id (int) (ref Conversation)...], 
    'state' : {
        'online' : true (boolean),
        'last_login_timestamp' : datetime,
        'last_logout_timestamp' : datetime,
        'cr_devices' : [client_runnable_id (int), ...],
        'queue' : [message_id (int) (ref Message), ...],
        'feed' : [post_id (int) (ref Post), ...]
    },
    'profile_pics' : [
        {
            'profile_pic' : string (url to storage),
            'viewable_to' : [user_id (int)  (ref User), ...]
        },
        {
            'profile_pic' : string (url to storage),
            'viewable_to' : [user_id (int)  (ref User), ...]
        }, ...
    ]
}
```

### Group
```
{
    'group_id' : int,
    'group_name' : string,
    'hasPassword' : bool,
    'password' : string (plain-text for now),
    'members' : [user_id (int) (ref User), ...],
    'administrators' : [user_id (int) (ref User), ...],
    'followedBy' : [user_id (int) (ref User), ...],
    'conversations' : [conversation_id (int) (ref Conversation)...]
    'subgroups' : [sub_group_id (int) (ref SubGroup), ...]
}
```

### Subgroup
```
{
    'sub_group_id' : int,
    'parent_group_id' : int (ref Group),
    'members' : [user_id (int) (ref User), ...],
    'administrators' : [user_id (int) (ref User), ...],
    'conversations' : [conversation_id (int) (ref Conversation)...]
}
```

## Messaging

### Conversation
```
{
    'conversation_id' : int,
    'group_id' : int (nullable) (ref Group),
    'sub_group_id' : int (nullable - null if part of main group conversation) 
    'members' : [user_id (int), ...]
}
```

Note - a direct message is a conversation with only two members, where as a group message is a conversation with 1 or more members (I can create a group conversation (a channel) and be the only member)

### Message
```
{
    'conversation_id' : int (ref Conversation),
    'message_id' : int,
    timestamp_sent : datetime,
    timestamp_delivered : datetime,
    'sender' : user_id (int) (ref User),
    'message_body' : string,
    'media' : [ media_id (int) (ref Medium), ... ]
}
```

### Medium
```
{
    'media_id' : int,
    'storage_link' : string url
}
```

### Inviting

## Invitation
```
{
    'invite_id' : int,
    'inviter_id' : int (ref User),
    'invitee_id' : int (ref User),
    'group_id' : int (ref Group),
    'needs_moderator_approval' : bool,
    'approvedBy' : int (ref User) (nullable if no moderator approval is needed)
}
```

## Posting

### Post
```
{
    'post_id' : int,
    'author_id' : int (ref User),
    'group_id' : int (ref Group) (null if user makes public post),
    'content' : string,
    'media' : [media_id (int) (ref Medium), ...]
}
```