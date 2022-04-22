package de.danielkoellgen.srscscollabservice.events.consumer;

import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.KafkaDeckCardsEventConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class KafkaDeckCardsEventConsumerIntegrationTest {

    private final UserService userService;
    private final CollaborationService collaborationService;

    private final KafkaDeckCardsEventConsumer kafkaDeckCardsEventConsumer;

    private User user1;
    private User user2;
    private User user3;

    @Autowired
    public KafkaDeckCardsEventConsumerIntegrationTest(UserService userService, CollaborationService collaborationService,
            KafkaDeckCardsEventConsumer kafkaDeckCardsEventConsumer) {
        this.userService = userService;
        this.collaborationService = collaborationService;
        this.kafkaDeckCardsEventConsumer = kafkaDeckCardsEventConsumer;
    }

    @BeforeEach
    public void setUp() throws Exception {
        user1 = userService.addExternallyCreatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Username("dadepu")
        );
        user2 = userService.addExternallyCreatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Username("melsienna")
        );
        user3 = userService.addExternallyCreatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Username("sarahve")
        );
    }

    @AfterEach
    public void cleanUp() {

    }

    @Test
    public void shouldAddDeckWhenReceivingDeckCreatedEventMatchingCorrelation() throws Exception {
        // given
        Collaboration collaboration = collaborationService.startNewCollaboration(
                UUID.randomUUID(), new DeckName("anyName"), List.of(user1.getUsername(), user2.getUsername())
        );
        UUID invitationAcceptedId = UUID.randomUUID();
        collaborationService.acceptParticipation(
                invitationAcceptedId, collaboration.getCollaborationId(), user2.getUserId()
        );

        // when

        // then
    }
}
