package de.danielkoellgen.srscscollabservice.domain.collaboration;

import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.*;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CreateNewCollaborationIntegrationTest {

    private final CollaborationService collaborationService;
    private final UserService userService;

    private final CollaborationRepository collaborationRepository;

    private final UUID        user1_userId = UUID.randomUUID();
    private final Username    user1_username = new Username("dadepu");

    private final UUID        user2_userId = UUID.randomUUID();
    private final Username    user2_username = new Username("melSienna");

    @Autowired
    public CreateNewCollaborationIntegrationTest(CollaborationService collaborationService, UserService userService,
            CollaborationRepository collaborationRepository) throws Exception {
        this.collaborationService = collaborationService;
        this.userService = userService;
        this.collaborationRepository = collaborationRepository;
    }

    @BeforeEach
    public void setUp() {
        userService.addExternallyCreatedUser(UUID.randomUUID(), user1_userId, user1_username, true);
        userService.addExternallyCreatedUser(UUID.randomUUID(), user2_userId, user2_username, true);
    }

    @Test
    public void shouldAllowToCreateNewCollaborationAndFetchById() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        DeckName name = new DeckName("anyName");

        // when
        UUID collaborationId = collaborationService.startNewCollaboration(transactionId, name, List.of(user1_username, user2_username));

        // then
        Collaboration fetchedById = collaborationRepository.findCollaborationById(collaborationId).get();
        assertThat(fetchedById.getName())
                .isEqualTo(name);
        assertThat(fetchedById.getCollaborationStatus())
                .isEqualTo(CollaborationStatus.ACTIVE);
        assertThat(fetchedById.getParticipants().values().size())
                .isEqualTo(2);

        // and then
        Participant p1 = fetchedById.getParticipants().get(user1_userId);
        assertThat(p1.getUser().getUserId())
                .isEqualTo(user1_userId);
        assertThat(p1.getUser().getUsername())
                .isEqualTo(user1_username);
        assertThrows(IllegalEntityPersistenceState.class, p1::getDeck);

        // and then
        State p1State = p1.getCurrentState();
        assertThat(p1.getStatus().size())
                .isEqualTo(1);
        assertThat(p1State.transactionId())
                .isEqualTo(transactionId);
        assertThat(p1State.status())
                .isEqualTo(ParticipantStatus.INVITED);
        assertThat(p1State.createdAt())
                .isBefore(LocalDateTime.now());
    }

    @Test
    public void shouldAllowToFetchCollaborationIdByDeckCorrelationId() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        DeckName name = new DeckName("anyName");
        UUID collaborationId = collaborationService.startNewCollaboration(transactionId, name, List.of(user1_username, user2_username));

        // when
        Collaboration fetchedById = collaborationRepository.findCollaborationById(collaborationId).get();
        UUID p1DeckCorrelationId = fetchedById.getParticipants().values().stream().toList().get(0).getDeckCorrelationId();
        UUID p2DeckCorrelationId = fetchedById.getParticipants().values().stream().toList().get(0).getDeckCorrelationId();

        // then
        UUID idFetchedByCorrelationIdP1 = collaborationRepository.findCollaborationIdByDeckCorrelationId(p1DeckCorrelationId).get();
        assertThat(idFetchedByCorrelationIdP1)
                .isEqualTo(collaborationId);

        // and then
        UUID idFetchedByCorrelationIdP2 = collaborationRepository.findCollaborationIdByDeckCorrelationId(p2DeckCorrelationId).get();
        assertThat(idFetchedByCorrelationIdP2)
                .isEqualTo(collaborationId);
    }

    @Test
    public void shouldAllowToFetchCollaborationByUserId() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        DeckName name = new DeckName("anyName");
        UUID collaborationId = collaborationService.startNewCollaboration(transactionId, name, List.of(user1_username, user2_username));

        // when
        List<Collaboration> collaborationsUser1 = collaborationRepository.findCollaborationsByUserId(user1_userId);
        List<Collaboration> collaborationsUser2 = collaborationRepository.findCollaborationsByUserId(user2_userId);

        // then
        assertThat(collaborationsUser1.size())
                .isEqualTo(collaborationsUser2.size())
                .isEqualTo(1);

        // and then
        Collaboration cb1 = collaborationsUser1.get(0);
        assertThat(cb1.getCollaborationId())
                .isEqualTo(collaborationId);
        assertThat(cb1.getName())
                .isEqualTo(name);
        assertThat(cb1.getParticipants().values().size())
                .isEqualTo(2);

        // and then
        Participant p1 = cb1.getParticipants().get(user1_userId);
        assertThat(p1.getUser().getUsername())
                .isEqualTo(user1_username);
        assertThrows(IllegalEntityPersistenceState.class, () -> {
            p1.getDeck();
        });

        // and then
    }
}
