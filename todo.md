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
        Create new (done)
        Add Card (done)
        Create new CardVersion (done)

KAFKA
    Consumer
        User
            UserCreated (done)
            UserDisabled (done)
        DeckCard
            DeckCreated (done | test missing)
            DeckDisabled (done)
            CardCreated (done | test missing)
            CardOverridden (done | test missing)
            CardDisabled (done)
    Producer
        Command
            CreateDeckCmd (done)
            CreateCardCmd (done)
            OverrideCardCmd (done)

REST
    Collaboration
        Start new Collaboration (done)
        Retrieves Collaboration by id (done)
        Retrieves Collaborations by user-id (done)
        Invite User to Collaboration (done)
        Accept Participation (done)
        Ends Participation (done)

# event refactoring
    # CardCreated: add userId
    # CardOverridden: add userId
    # CardDisabled: add userId
    # header: 'correlationId'
        # collab: command producer
        # deck: command consumer
        # deck: event producer

# event consumer
# command producer
# testing
# asyncAPI
# refactor CollaborationCardService


0622 0900   158
0931 1301   240
1301 1400   59
1415 1423   8
1451 1546   55
1553 1623   30
1645 1735   50
1851 1907   16
1935 1950   15

0651 0840   109
0955 1045   50
1357 1540   43
1641 1651   10
1825 1840   15

0604 0620   15
0730 0825   55
0905 0922   17
0951 1011   20
1122 1600   278
1600 1630   30
1645 1745
475 -> 7.9h

0540 0715   95
1000