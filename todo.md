DOMAIN
    User
        Create User (done)
        Disable user (done)
    Collaboration
        Start new Collaboration (done)
        Invite User to Collaboration (done)
        Accept Participation (done)
        End Participation (done)
    CollaborationCard

KAFKA
    Consumer
        User
            UserCreated (done)
            UserDisabled (done)
        DeckCard
            DeckCreated (done | callback missing, test missing)
            DeckDisabled (done | callback missing, test missing)
            CardCreated (done | callback missing, test missing)
            CardOverridden (done | callback missing, test missing)
            CardDisabled (done | callback missing, test missing)
    Producer
        Command
            CreateDeckCmd
            CreateCardCmd
            OverrideCardCmd

REST
    Collaboration
        Start new Collaboration (done)
        Retrieves Collaboration by id (done)
        Retrieves Collaborations by user-id (done)
        Invite User to Collaboration (done)
        Accept Participation (done)
        Ends Participation (done)


0622 0900   158
0931 1301   240
1301 1400   59
1415 1423   8
1451 1546   55
1553 1623   30
1645 1735   50
1851 1907   16
1935 1950   15

0651 0840
0955 1045
1357
