package de.danielkoellgen.srscscollabservice.domain.collaboration;

import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class InviteParticipantToCollaborationIntegrationTest {

    private final UserService userService;
    private final CollaborationService collaborationService;

    private final User user1 = new User(UUID.randomUUID(), new Username("dadepu1"), true);
    private final User user2 = new User(UUID.randomUUID(), new Username("dadepu1"), true);
    private final User user3 = new User(UUID.randomUUID(), new Username("dadepu1"), true);

    @Autowired
    public InviteParticipantToCollaborationIntegrationTest(UserService userService,
            CollaborationService collaborationService) throws Exception {
        this.userService = userService;
        this.collaborationService = collaborationService;
    }

    @BeforeEach
    public void setUp() {
        userService.addExternallyCreatedUser(
                UUID.randomUUID(), user1.getUserId(), user1.getUsername(), user1.getIsActive());
        userService.addExternallyCreatedUser(
                UUID.randomUUID(), user2.getUserId(), user2.getUsername(), user2.getIsActive());
        userService.addExternallyCreatedUser(
                UUID.randomUUID(), user3.getUserId(), user3.getUsername(), user3.getIsActive());
    }

    @Test
    public void shouldPersistById() throws Exception {
        // given
        UUID collaborationId = collaborationService.startNewCollaboration(
                UUID.randomUUID(), new DeckName("THKoeln"), List.of(user1.getUsername(), user2.getUsername()));

        // when
        collaborationService.inviteUserToCollaboration(
                UUID.randomUUID(), collaborationId, user3.getUsername());
    }
}
