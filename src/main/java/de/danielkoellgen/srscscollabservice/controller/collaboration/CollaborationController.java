package de.danielkoellgen.srscscollabservice.controller.collaboration;

import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationRequestDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.CollaborationResponseDto;
import de.danielkoellgen.srscscollabservice.controller.collaboration.dto.ParticipantResponseDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<CollaborationResponseDto> startNewCollaboration(
            @RequestBody CollaborationRequestDto requestDto) {
        UUID transactionId = UUID.randomUUID();
        logger.trace("POST /collaborations: Start new Collaboration '{}'. [tid={}, payload={}]",
                requestDto.collaborationName(),
                transactionId,
                requestDto
        );
        Collaboration startedCollaboration;
        try {
            startedCollaboration = collaborationService.startNewCollaboration(
                    transactionId,
                    requestDto.getMappedCollaborationName(),
                    requestDto.getMappedInvitedUsers()
            );
        } catch (NoSuchElementException e) {
            logger.trace("Request failed. Entity not found. Responding 404. [tid={}, message={}]",
                    transactionId, e.getStackTrace());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.", e);
        }
        logger.trace("Collaboration '{}' started with {} invited Users. Responding 201. [tid={}, payload={}]",
                startedCollaboration.getName().getName(),
                startedCollaboration.getParticipants().size(),
                transactionId,
                new CollaborationResponseDto(startedCollaboration)
        );
        return new ResponseEntity<>(new CollaborationResponseDto(startedCollaboration), HttpStatus.CREATED);
    }

//    @PostMapping(value = "/collaborations/{collaboration-id}/participants", consumes = {"application/json"},
//            produces = {"application/json"})
//    public ResponseEntity<ParticipantResponseDto> inviteUserToCollaboration(
//            @PathVariable("collaboration-id") UUID collaborationId) {
//        UUID transactionId = UUID.randomUUID();
//    }

    @PostMapping(value = "/collaborations/{collaboration-id}/participants/{user-id}/state")
    public ResponseEntity<?> acceptParticipation(@PathVariable("collaboration-id") UUID collaborationId,
            @PathVariable("user-id") UUID userId) {
        UUID transactionId = UUID.randomUUID();
        logger.trace("POST /collaborations/{}/participants/{}/state: Accept Participation.[tid={}]",
                collaborationId, userId, transactionId
        );
        try {
            collaborationService.acceptParticipation(
                    transactionId, collaborationId, userId
            );
        } catch(NoSuchElementException e) {
            logger.trace("Request failed. Entity not found. Responding 404. [tid={}, message={}]",
                    transactionId, e.getStackTrace()
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found.", e);
        } catch (ParticipantStateException e) {
            logger.trace("Request failed. Responding 403. [tid={}, message={}]",
                    transactionId, e.getStackTrace()
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.", e);
        }
        logger.trace("Participation accepted. Responding 201. [tid={}]",
                transactionId
        );
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

//    @DeleteMapping(value = "/collaborations/{collaboration-id}/participants")
//    public ResponseEntity<?> endParticipation(@PathVariable("collaboration-id") UUID collaborationId,
//            @RequestParam("user-id") UUID userId) {
//        UUID transactionId = UUID.randomUUID();
//    }

    @GetMapping(value = "/collaborations/{collaboration-id}", produces = {"application/json"})
    public ResponseEntity<CollaborationResponseDto> fetchCollaborationById(
            @PathVariable("collaboration-id") UUID collaborationId) {
        UUID transactionId = UUID.randomUUID();
        logger.trace("GET /collaborations/{}: Fetch Collaboration by id. [tid={}]",
                collaborationId, transactionId);
        Collaboration collaboration;
        try {
            collaboration = collaborationRepository.findCollaborationById(collaborationId).get();
        } catch (NoSuchElementException e) {
            logger.trace("Request failed. Entity not found. Responding 404. [tid={}]",
                    transactionId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found.", e);
        }
        logger.trace("Collaborations retrieved. Responding 200. [tid={}, payload={}]",
                transactionId, new CollaborationResponseDto(collaboration));
        return new ResponseEntity<>(
                new CollaborationResponseDto(collaboration),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "/collaborations", produces = {"application/json"})
    public ResponseEntity<List<CollaborationResponseDto>> fetchCollaborationByUserId(@RequestParam("user-id") UUID userId) {
        UUID transactionId = UUID.randomUUID();
        logger.trace("GET /collaborations?user-id={}: Fetch Collaboration by user-id. [tid={}]",
                userId, transactionId);
        List<CollaborationResponseDto> collaboration = collaborationRepository
                .findCollaborationsByUserId(userId).stream()
                .map(CollaborationResponseDto::new)
                .toList();
        logger.trace("{} Collaborations retrieved. Responding 200. [tid={}, payload={}]",
                collaboration.size(),
                transactionId,
                collaboration
        );
        return new ResponseEntity<>(
                collaboration, HttpStatus.OK
        );
    }
}
