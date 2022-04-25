
CREATE KEYSPACE IF NOT EXISTS collab_service
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
    };

Use collab_service;

DROP TABLE IF EXISTS user_by_id;
DROP TABLE IF EXISTS user_by_username;
DROP TABLE IF EXISTS collaboration_by_id;
DROP TABLE IF EXISTS collaboration_by_deckid;
DROP TABLE IF EXISTS collaboration_by_userid;
DROP TABLE IF EXISTS collaboration_by_deckcorrelationid;
DROP TABLE IF EXISTS collaborationcard_by_collaborationid;
DROP TABLE IF EXISTS collaborationcard_by_correlationid;
DROP TABLE IF EXISTS collaborationcard_by_rootcardid;
DROP TABLE IF EXISTS collaborationcard_by_cardid;

DROP TYPE IF EXISTS participation_state;


CREATE TABLE user_by_id (
    user_id UUID,
    username TEXT,
    is_active BOOLEAN,
    PRIMARY KEY ( user_id )
);

CREATE TABLE user_by_username (
    username TEXT,
    user_id UUID,
    is_active BOOLEAN,
    PRIMARY KEY ( username )
);

CREATE TYPE participation_state (
    transaction_id UUID,
    status INT,
    created_at TIMESTAMP
);

CREATE TABLE collaboration_by_id (
    collaboration_id UUID,
    participant_user_id UUID,
    collaboration_name TEXT STATIC,
    participant_username TEXT,
    participant_deck_id UUID,
    participant_deck_correlation_id UUID,
    participant_status LIST<FROZEN<participation_state>>,
    PRIMARY KEY ( collaboration_id, participant_user_id )
);

CREATE TABLE collaboration_by_deckid (
    deck_id UUID,
    collaboration_id UUID,
    participant_user_id UUID,
    participant_deck_id UUID,
    participant_status LIST<FROZEN<participation_state>>,
    PRIMARY KEY ( deck_id, collaboration_id, participant_user_id )
);

CREATE TABLE collaboration_by_userid (
    user_id UUID,
    collaboration_id UUID,
    participant_user_id UUID,
    collaboration_name TEXT STATIC,
    participant_username TEXT,
    participant_deck_id UUID,
    participant_deck_correlation_id UUID,
    participant_status LIST<FROZEN<participation_state>>,
    PRIMARY KEY ( user_id, collaboration_id, participant_user_id )
);

CREATE TABLE collaboration_by_deckcorrelationid (
    deck_correlation_id UUID,
    collaboration_id UUID,
    PRIMARY KEY ( deck_correlation_id )
);

CREATE TABLE collaborationcard_by_collaborationid (
    collaboration_id UUID,
    root_card_id UUID,
    PRIMARY KEY ( collaboration_id )
);

CREATE TABLE collaborationcard_by_correlationid (
    correlation_id UUID,
    root_card_id UUID,
    PRIMARY KEY ( correlation_id )
);

CREATE TABLE collaborationcard_by_rootcardid (
    root_card_id UUID,
    correlation_id UUID,
    collaboration_card_id UUID STATIC,
    collaboration_id UUID,
    deck_id UUID,
    user_id UUID,
    card_id UUID,
    PRIMARY KEY ( root_card_id, correlation_id )
);

CREATE TABLE collaborationcard_by_cardid (
    card_id UUID,
    root_card_id UUID,
    PRIMARY KEY ( card_id )
);