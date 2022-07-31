package de.danielkoellgen.srscscollabservice.controller.collaboration;

import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationRequestDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationResponseDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.ParticipantRequestDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.ParticipantResponseDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.CollaborationStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class CollaborationController {

    private final CollaborationService collaborationService;

    private final CollaborationRepository collaborationRepository;

    private final Logger logger = LoggerFactory.getLogger(CollaborationController.class);

    @Autowired
    public CollaborationController(CollaborationService collaborationService,
            CollaborationRepository collaborationRepository) {
        this.collaborationService = collaborationService;
        this.collaborationRepository = collaborationRepository;
    }

    @PostMapping(value = "/collaborations", consumes = {"application/json"}, produces = {"application/json"})
    @NewSpan("controller-start-new-collaboration")
    public ResponseEntity<CollaborationResponseDto> startNewCollaboration(
            @RequestBody CollaborationRequestDto requestDto) {
        logger.debug("POST /collaborations: Start new Collaboration.");
        logger.debug("{}", requestDto);

        Collaboration startedCollaboration;
        try {
            startedCollaboration = collaborationService.startNewCollaboration(
                    requestDto.getMappedCollaborationName(),
                    requestDto.getMappedInvitedUsers()
            );
            CollaborationResponseDto responseDto = new CollaborationResponseDto(startedCollaboration);
            logger.trace("Responding 201.");
            logger.debug("{}", responseDto);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (NoSuchElementException e) {
            logger.trace("Request failed with 404. Entity not found.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.", e);
        }
    }

    @PostMapping(value = "/collaborations/{collaboration-id}/participants", consumes = {"application/json"},
            produces = {"application/json"})
    @NewSpan("controller-invite-user-to-collaboration")
    public ResponseEntity<ParticipantResponseDto> inviteUserToCollaboration(
            @PathVariable("collaboration-id") UUID collaborationId, @RequestBody ParticipantRequestDto requestDto) {
        logger.debug("POST /collaborations/{}/participants: Invite User to participate.", collaborationId);
        logger.debug("{}", requestDto);

        Participant newParticipant;
        try {
            newParticipant = collaborationService.inviteUserToCollaboration(
                    collaborationId, requestDto.getMappedUsername()
            );
            ParticipantResponseDto responseDto = new ParticipantResponseDto(newParticipant);
            logger.trace("Responding 201.");
            logger.debug("{}", responseDto);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (NoSuchElementException e) {
            logger.trace("Request failed with 404. Entity not found.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.",e);

        } catch (CollaborationStateException e) {
            logger.trace("Request failed with 403.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.", e);
        }
    }

    @PostMapping(value = "/collaborations/{collaboration-id}/participants/{user-id}/state")
    @NewSpan("controller-accept-participation")
    public ResponseEntity<?> acceptParticipation(@PathVariable("collaboration-id") UUID collaborationId,
            @PathVariable("user-id") UUID userId) {
        logger.debug("POST /collaborations/{}/participants/{}/state: Accept Participation.", collaborationId, userId);

        try {
            collaborationService.acceptParticipation(collaborationId, userId);
            logger.trace("Responding 201.");
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (NoSuchElementException e) {
            logger.trace("Request failed with 404.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.", e);

        } catch (ParticipantStateException e) {
            logger.trace("Request failed with 403.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.", e);
        }
    }

    @DeleteMapping(value = "/collaborations/{collaboration-id}/participants/{user-id}")
    @NewSpan("controller-end-participation")
    public ResponseEntity<?> endParticipation(@PathVariable("collaboration-id") UUID collaborationId,
            @PathVariable("user-id") UUID userId) {
        logger.debug("DELETE /collaborations/{}/participants/{}: End Participation.", collaborationId, userId);

        try {
            collaborationService.endParticipation(collaborationId, userId);
            logger.trace("Responding 200.");
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (NoSuchElementException e) {
            logger.trace("Request failed with 404.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.", e);

        } catch (ParticipantStateException e) {
            logger.trace("Request failed with 403.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.", e);
        }
    }

    @GetMapping(value = "/collaborations/{collaboration-id}", produces = {"application/json"})
    @NewSpan("controller-fetch-collaboration-by-id")
    public ResponseEntity<CollaborationResponseDto> fetchCollaborationById(
            @PathVariable("collaboration-id") UUID collaborationId) {
        logger.debug("GET /collaborations/{}: Fetch Collaboration by id.", collaborationId);

        try {
            Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();
            CollaborationResponseDto responseDto = new CollaborationResponseDto(collaboration);
            logger.trace("Responding 200.");
            logger.trace("{}", responseDto);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (NoSuchElementException e) {
            logger.trace("Request failed with 404.");
            logger.trace("{}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found.", e);
        }
    }

    @GetMapping(value = "/collaborations", produces = {"application/json"})
    @NewSpan("controller-fetch-collaboration-by-userid")
    public ResponseEntity<List<CollaborationResponseDto>> fetchCollaborationByUserId(@RequestParam("user-id") UUID userId) {
        logger.debug("GET /collaborations?user-id={}: Fetch Collaboration by user-id.", userId);

        logger.trace("Fetching Collaborations by user-id {}.", userId);
        List<CollaborationResponseDto> collaborations = collaborationRepository
                .findCollaborationsByUserId(userId).stream()
                .map(CollaborationResponseDto::new)
                .toList();
        logger.debug("{} Collaborations fetched.", collaborations.size());
        logger.debug("{}", collaborations);

        logger.trace("Responding 200.");
        return new ResponseEntity<>(collaborations, HttpStatus.OK);
    }
}
