package de.danielkoellgen.srscscollabservice.controller.collaboration;

import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationRequestDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationResponseDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.ParticipantRequestDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.ParticipantResponseDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStatus;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CollaborationCardControllerIntegrationTest {

    private final WebTestClient webTestClientCollaboration;

    private final UserService userService;

    private final CollaborationRepository collaborationRepository;

    private User user1;
    private User user2;
    private User user3;

    @Autowired
    public CollaborationCardControllerIntegrationTest(CollaborationController collaborationController,
            UserService userService, CollaborationRepository collaborationRepository) {
        this.webTestClientCollaboration = WebTestClient.bindToController(collaborationController).build();
        this.userService = userService;
        this.collaborationRepository = collaborationRepository;
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
    public void shouldAllowToStartNewCollaborations() {
        // given
        CollaborationRequestDto requestDto = new CollaborationRequestDto(
                List.of(
                        user1.getUsername().getUsername(),
                        user2.getUsername().getUsername()
                ),
                "anyName"
        );

        // when
        CollaborationResponseDto responseDto = webTestClientCollaboration.post().uri("/collaborations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CollaborationResponseDto.class)
                .returnResult().getResponseBody();
        assert responseDto != null;

        // then
        assertThat(responseDto.collaborationName())
                .isEqualTo("anyName");
        assertThat(responseDto.participants())
                .hasSize(2);

        // and then
        ParticipantResponseDto user1 = responseDto.participants().stream()
                .filter(user -> user.userId().equals(this.user1.getUserId()))
                .toList()
                .get(0);
        assertThat(user1.participantStatus())
                .isEqualTo(ParticipantStatus.toStringFromEnum(ParticipantStatus.INVITED));

        // and then
        Collaboration fetchedCollaboration = collaborationRepository
                .findCollaborationById(responseDto.collaborationId()).orElseThrow();
    }

    @Test
    public void shouldAllowToFetchCollaborationsById() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2)
        );
        UUID collaborationId = startedCollaboration.collaborationId();

        // when
        CollaborationResponseDto responseDto = webTestClientCollaboration.get()
                .uri("/collaborations/"+collaborationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CollaborationResponseDto.class)
                .returnResult().getResponseBody();

        // then
        assertThat(startedCollaboration)
                .isEqualTo(responseDto);
    }

    @Test
    public void shouldAllowToFetchCollaborationsByUserId() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2)
        );
        UUID collaborationId = startedCollaboration.collaborationId();

        // when
        List<CollaborationResponseDto> responseDtosByUserId = webTestClientCollaboration.get()
                .uri("/collaborations?user-id="+user2.getUserId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CollaborationResponseDto.class)
                .returnResult().getResponseBody();
        CollaborationResponseDto responseDto = responseDtosByUserId.get(0);

        // then
        CollaborationResponseDto responseDtoById = fetchExternalCollaborationById(responseDto.collaborationId());
        assertThat(responseDto)
                .isEqualTo(responseDtoById);
    }

    @Test
    public void shouldAllowToAcceptInvitations() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2)
        );
        UUID collaborationId = startedCollaboration.collaborationId();
        UUID acceptedUserUserId = user1.getUserId();

        // when
        webTestClientCollaboration.post()
                .uri("/collaborations/"+collaborationId+"/participants/"+acceptedUserUserId+"/state")
                .exchange()
                .expectStatus().isCreated();

        // then
        CollaborationResponseDto fetchedCollaboration = fetchExternalCollaborationById(collaborationId);
        ParticipantResponseDto user1Dto = fetchedCollaboration.participants().stream()
                .filter(x -> x.userId().equals(acceptedUserUserId))
                .toList()
                .get(0);
        assertThat(user1Dto.participantStatus())
                .isEqualTo(ParticipantStatus.toStringFromEnum(
                        ParticipantStatus.INVITATION_ACCEPTED
                ));
    }

    @Test
    public void shouldAllowToDeclineInvitations() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2));
        UUID collaborationId = startedCollaboration.collaborationId();
        UUID declinedUserId = user1.getUserId();

        // when
        webTestClientCollaboration.delete()
                .uri("/collaborations/"+collaborationId+"/participants/"+declinedUserId+"")
                .exchange()
                .expectStatus().isOk();

        // then
        CollaborationResponseDto fetchedCollaboration = fetchExternalCollaborationById(collaborationId);
        ParticipantResponseDto user1Dto = fetchedCollaboration.participants().stream()
                .filter(x -> x.userId().equals(declinedUserId))
                .toList()
                .get(0);
        assertThat(user1Dto.participantStatus())
                .isEqualTo(ParticipantStatus.toStringFromEnum(
                        ParticipantStatus.INVITATION_DECLINED
                ));
    }

    @Test
    public void shouldAllowToEndAcceptedInvitations() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2));
        UUID collaborationId = startedCollaboration.collaborationId();
        UUID acceptedUserUserId = user1.getUserId();
        webTestClientCollaboration.post()
                .uri("/collaborations/"+collaborationId+"/participants/"+acceptedUserUserId+"/state")
                .exchange()
                .expectStatus().isCreated();

        // when
        webTestClientCollaboration.delete()
                .uri("/collaborations/"+collaborationId+"/participants/"+acceptedUserUserId+"")
                .exchange()
                .expectStatus().isOk();

        // then
        CollaborationResponseDto fetchedCollaboration = fetchExternalCollaborationById(collaborationId);
        ParticipantResponseDto user1Dto = fetchedCollaboration.participants().stream()
                .filter(x -> x.userId().equals(acceptedUserUserId))
                .toList()
                .get(0);
        assertThat(user1Dto.participantStatus())
                .isEqualTo(ParticipantStatus.toStringFromEnum(
                        ParticipantStatus.TERMINATED
                ));
    }

    @Test
    public void shouldAllowToInviteUsersToParticipate() {
        // given
        CollaborationResponseDto startedCollaboration = externallyStartCollaboration(
                List.of(user1, user2));
        UUID collaborationId = startedCollaboration.collaborationId();
        ParticipantRequestDto requestDto = new ParticipantRequestDto(user3.getUsername().getUsername());

        // when
        ParticipantResponseDto responseDto = webTestClientCollaboration.post()
                .uri("/collaborations/"+collaborationId+"/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ParticipantResponseDto.class)
                .returnResult().getResponseBody();

        // then
        CollaborationResponseDto fetchedCollaboration = fetchExternalCollaborationById(collaborationId);
        assertThat(fetchedCollaboration.participants())
                .hasSize(3);
        ParticipantResponseDto invitedUserDto = fetchedCollaboration.participants().stream()
                .filter(x -> x.userId().equals(user3.getUserId()))
                .toList()
                .get(0);
        assertThat(invitedUserDto.participantStatus())
                .isEqualTo(ParticipantStatus.toStringFromEnum(
                        ParticipantStatus.INVITED
                ));
    }

    public @NotNull CollaborationResponseDto externallyStartCollaboration(List<User> users) {
        // given
        CollaborationRequestDto requestDto = new CollaborationRequestDto(
                users.stream()
                        .map(x -> x.getUsername().getUsername())
                        .toList(),
                "anyName"
        );
        CollaborationResponseDto responseDto = webTestClientCollaboration.post().uri("/collaborations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CollaborationResponseDto.class)
                .returnResult().getResponseBody();
        assert responseDto != null;
        return responseDto;
    }

    public @NotNull CollaborationResponseDto fetchExternalCollaborationById(@NotNull UUID collaborationId) {
        CollaborationResponseDto responseDto = webTestClientCollaboration.get()
                .uri("/collaborations/"+collaborationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CollaborationResponseDto.class)
                .returnResult().getResponseBody();
        assert responseDto != null;
        return responseDto;
    }
}
