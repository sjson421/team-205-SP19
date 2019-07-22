## Examples of How the schmea allows for messaging

### User

User 1
```
{
    'user_id' : 1,
    'username' : 'raghavp96',
    'password' : 'myPassword123' (plain-text for now),
    ...
    'name' : 'Raghavprasanna Rajagopalan',
    'friends' : [2],
    'followedBy' : [2],
    'state' : {
        'online' : true,
        'cr_devices' : [CR-1, CR-3],
        'queue' : [],
        'feed' : []
    },
    'group_passwords' : [
        {
            'group_id' : 1,
            'password' : 'safe'
        }
    ],
    'conversations' : [1, 2]
}
```

User 2
```
{
    'user_id' : 2,
    'username' : 'test',
    'password' : 'pwd' (plain-text for now),
    ...
    'name' : 'Test User',
    'friends' : [1],
    'followedBy' : [],
    'state' : {
        'online' : false,
        'cr_devices' : [],
        'queue' : [1, 2, 3],
        'feed' : []
    },
    'group_passwords' : [
        {
            'group_id' : 1,
            'password' : ''
        }
    ],
    'conversations' : [1, 2, 3]
}
```

User 3
```
{
    'user_id' : 3,
    'username' : 'newKid',
    'password' : 'pwd' (plain-text for now),
    ...
    'name' : 'New Kid',
    'friends' : [],
    'followedBy' : [],
    'state' : {
        'online' : false,
        'cr_devices' : [],
        'queue' : [],
        'feed' : [1]
    },
    'group_passwords' : [],
    'conversations' : [3]
}
```

Group1
```
{
    'group_id' : 1,
    'group_name' : 'first-group',
    'members' : [1, 2],
    ...
    'followedBy' : [3],
    'conversations' : [2]
}
```

Conversation1 (Direct Message)
```
{
    'conversation_id' : 1,
    'members' : [1, 2]
}
```

Conversation2 (Group1 Message)
```
{
    'conversation_id' : 2,
    'members' : [1, 2]
}
```

Conversation3
```
{
    'conversation_id' : 3,
    'members' : [2, 3]
}
```


Message1
```
{
    'conversation_id' : 1,
    'message_id' : 1,
    timestamp_sent : datetime,
    'sender' : 1,
    'message_body' : "Hey test",
    'media' : []
    readBy : []
}
```

Message2
```
{
    'conversation_id' : 2,
    'message_id' : 2,
    timestamp_sent : datetime,
    'sender' : 1,
    'message_body' : "Hey group what is everyone up to?",
    'media' : []
    readBy : []
}
```

Message3
```
{
    'conversation_id' : 3,
    'message_id' : 3,
    timestamp_sent : datetime,
    'sender' : 2,
    'message_body' : "Hey new kid",
    'media' : []
    readBy : [3]
}
```