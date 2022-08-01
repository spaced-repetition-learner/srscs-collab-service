package de.danielkoellgen.srscscollabservice.domain.collaboration.repository;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByDeckCorrelationIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByDeckIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByUserIdMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Component
@Scope("singleton")
public class CollaborationRepositoryImpl implements CollaborationRepository {

    private final CassandraOperations cassandraTemplate;

    @Autowired
    public CollaborationRepositoryImpl(CassandraOperations cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    @Override
    @NewSpan("save collaboration")
    public void saveNewCollaboration(@NotNull Collaboration collaboration) {
        List<CollaborationByIdMap> mappedByIds = collaboration.getParticipants().values()
                .stream()
                .map(participant -> CollaborationByIdMap.mapFromEntity(collaboration, participant))
                .toList();
        mappedByIds.forEach(cassandraTemplate::insert);

        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(participantX -> collaboration.getParticipants().values().stream()
                        .map(participantY -> CollaborationByUserIdMap.mapFromEntity(
                                participantX.getUserId(),
                                collaboration,
                                participantY
                        )).toList())
                .flatMap(Collection::stream)
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("save participant")
    public void saveNewParticipant(@NotNull Collaboration collaboration,
            @NotNull Participant newParticipant) {
        // by id
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, newParticipant);
        cassandraTemplate.insert(mappedById);

        // by deck-id
        //      for each user's deck
        //          -> insert participant
        List<CollaborationByDeckIdMap> mappedByDeckId = collaboration.getParticipants().values()
                .stream()
                .filter(x -> x.getDeck() != null)
                .map(x -> CollaborationByDeckIdMap
                        .mapFromEntity(x.getDeck().getDeckId(), collaboration, newParticipant))
                .toList();
        mappedByDeckId.forEach(cassandraTemplate::insert);

        // by user-id (1)
        //      for each user
        //          -> update participant
        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(x.getUserId(), collaboration, newParticipant))
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);


        // by user-id (2)
        //      by new user-id
        //          for each user
        //              -> insert participant
        mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(newParticipant.getUserId(), collaboration, x))
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("update accepted participant")
    public void updateAcceptedParticipant(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        // by id
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, participant);
        cassandraTemplate.insert(mappedById);

        // by deck-id
        //      for each user's deck
        //          -> update participant
        List<CollaborationByDeckIdMap> mappedByDeckId = collaboration.getParticipants().values()
                .stream()
                .filter(x -> x.getDeck() != null)
                .map(x -> CollaborationByDeckIdMap
                        .mapFromEntity(x.getDeck().getDeckId(), collaboration, participant))
                .toList();
        mappedByDeckId.forEach(cassandraTemplate::insert);

        // by user-id
        //      for each user
        //          -> update participant
        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(x.getUserId(), collaboration, participant))
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);

        // by correlation-id
        if (participant.getDeckCorrelationId() != null) {
            CollaborationByDeckCorrelationIdMap mappedByDeckCorrelationId =
                    CollaborationByDeckCorrelationIdMap
                        .mapFromEntity(collaboration, participant);
            cassandraTemplate.insert(mappedByDeckCorrelationId);
        }
    }

    @Override
    @NewSpan("update terminated participant")
    public void updateTerminatedParticipant(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        // by id
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, participant);
        cassandraTemplate.insert(mappedById);

        // by deck-id
        //      for each user's deck
        //          -> update participant
        List<CollaborationByDeckIdMap> mappedByDeckId = collaboration.getParticipants().values()
                .stream()
                .filter(x -> x.getDeck() != null)
                .map(x -> CollaborationByDeckIdMap
                        .mapFromEntity(x.getDeck().getDeckId(), collaboration, participant))
                .toList();
        mappedByDeckId.forEach(cassandraTemplate::insert);

        // by user-id
        //      for each user
        //          -> update participant
        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(x.getUserId(), collaboration, participant))
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("update deck-added participant")
    public void updateDeckAddedParticipant(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        // by id
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, participant);
        cassandraTemplate.insert(mappedById);

        // by deck-id (1)
        //      for each user's deck
        //          -> update participant
        List<CollaborationByDeckIdMap> mappedByDeckId = collaboration.getParticipants().values()
                .stream()
                .filter(x -> x.getDeck() != null)
                .map(x -> CollaborationByDeckIdMap
                        .mapFromEntity(x.getDeck().getDeckId(), collaboration, participant))
                .toList();
        mappedByDeckId.forEach(cassandraTemplate::insert);

        // by deck-id (2)
        //      by new deck-id
        //          for each user
        //              -> save new
        mappedByDeckId = collaboration.getParticipants().values().stream()
                .map(x -> CollaborationByDeckIdMap
                        .mapFromEntity(participant.getDeck().getDeckId(), collaboration, x))
                .toList();
        mappedByDeckId.forEach(cassandraTemplate::insert);

        // by user-id
        //      for each user
        //          -> update participant
        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values()
                .stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(x.getUserId(), collaboration, participant))
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("find collaboration by id")
    public @NotNull Optional<Collaboration> findCollaborationById(@NotNull UUID collaborationId) {
        List<CollaborationByIdMap> byIdMaps = cassandraTemplate.select(
                query(where("collaboration_id").is(collaborationId)),
                CollaborationByIdMap.class);
        if (byIdMaps.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CollaborationByIdMap.mapToEntityFromDatabase(byIdMaps));
    }

    @Override
    @NewSpan("find collaboration by deck-correlation-id")
    public @NotNull Optional<UUID> findCollaborationIdByDeckCorrelationId(
            @NotNull UUID deckCorrelationId) {
        CollaborationByDeckCorrelationIdMap map = cassandraTemplate.selectOne(
                query(where("deck_correlation_id").is(deckCorrelationId)),
                CollaborationByDeckCorrelationIdMap.class);
        return map != null ? Optional.of(map.getCollaborationId()) : Optional.empty();
    }

    @Override
    @NewSpan("find collaboration by deck-correlation-id")
    public @NotNull Optional<Collaboration> findCollaborationByDeckCorrelationId(
            @NotNull UUID deckCorrelationId) {
        Optional<UUID> collaborationId = findCollaborationIdByDeckCorrelationId(deckCorrelationId);
        if (collaborationId.isEmpty()) {
            return Optional.empty();
        }
        return findCollaborationById(collaborationId.get());
    }

    @Override
    @NewSpan("find collaborations by user-id")
    public @NotNull List<Collaboration> findCollaborationsByUserId(@NotNull UUID userId) {
        List<CollaborationByUserIdMap> byUserIdMaps = cassandraTemplate.select(
                query(where("user_id").is(userId)),
                CollaborationByUserIdMap.class
        );
        Map<UUID, List<CollaborationByUserIdMap>> filteredByCollabId = new HashMap<>();
        for (CollaborationByUserIdMap map : byUserIdMaps) {
            List<CollaborationByUserIdMap> updatedCollabList = Stream.concat(
                            (filteredByCollabId.containsKey(map.getCollaborationId())
                                    ? filteredByCollabId.get(map.getCollaborationId())
                                    : new ArrayList<CollaborationByUserIdMap>())
                                    .stream(),
                            Stream.of(map))
                    .collect(Collectors.toList());
            filteredByCollabId.put(map.getCollaborationId(), updatedCollabList);
        }
        return filteredByCollabId.values()
                .stream()
                .map(CollaborationByUserIdMap::mapToEntityFromDatabase)
                .toList();
    }

    @Override
    @NewSpan("find collaboration by deck-id")
    public @NotNull Optional<Collaboration> findCollaborationByDeckId(@NotNull UUID deckId) {
        List<CollaborationByDeckIdMap> selectByDeckId = cassandraTemplate.select(
                query(where("deck_id").is(deckId)), CollaborationByDeckIdMap.class);
        if (selectByDeckId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CollaborationByDeckIdMap.mapToEntityFromDatabase(selectByDeckId)); //TODO
    }
}
