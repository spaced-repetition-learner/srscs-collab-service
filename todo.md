DOMAIN
    User
        Create User (done)
        Disable user (done)
    Collaboration
        Start new Collaboration (done)
        Invite User to Collaboration
        Accept Participation
        End Participation
        -> Refactor List<ParticipantStatus> to ParticipantStatus
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

REST
    Collaboration
        Start new Collaboration 
        Retrieves Collaboration by id
        Retrieves Collaborations by filter
        Invite User to Collaboration
        Accept Participation
        Ends Participation


0622 0900   158
0931 1301   240
1301 1400   59
1415 1423   8
1451
            465 -> 7.75h