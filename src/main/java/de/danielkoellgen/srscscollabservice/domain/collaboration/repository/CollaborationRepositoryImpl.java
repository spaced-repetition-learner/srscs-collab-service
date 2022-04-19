package de.danielkoellgen.srscscollabservice.domain.collaboration.repository;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByDeckCorrelationIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.CollaborationByUserIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps.ParticipantStateMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
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
    public void saveNewCollaboration(@NotNull Collaboration collaboration) {
        List<CollaborationByIdMap> mappedByIds = collaboration.getParticipants().values().stream()
                .map(participant -> CollaborationByIdMap.mapFromEntity(collaboration, participant))
                .toList();
        mappedByIds.forEach(cassandraTemplate::insert);

        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values().stream()
                .map(participantX -> collaboration.getParticipants().values().stream()
                        .map(participantY -> CollaborationByUserIdMap.mapFromEntity(
                                participantX.getUserId(),
                                collaboration,
                                participantY
                        )).toList()
                ).flatMap(Collection::stream)
                .toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);

        List<CollaborationByDeckCorrelationIdMap> mappedByDeckCorrelationIds = collaboration.getParticipants().values().stream()
                .map(participant -> CollaborationByDeckCorrelationIdMap.mapFromEntity(collaboration, participant))
                .toList();
        mappedByDeckCorrelationIds.forEach(cassandraTemplate::insert);
    }

    @Override
    public void saveNewParticipant(@NotNull Collaboration collaboration, @NotNull Participant newParticipant) {
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, newParticipant);
        cassandraTemplate.insert(mappedById);

        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values().stream()
                .map(participant -> CollaborationByUserIdMap.mapFromEntity(
                        participant.getUserId(), collaboration, newParticipant)
                ).toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);

        //TODO: Collaboration_by_deckId!!

        CollaborationByDeckCorrelationIdMap mappedByDeckCorrelationId = CollaborationByDeckCorrelationIdMap
                .mapFromEntity(collaboration, newParticipant);
        cassandraTemplate.insert(mappedByDeckCorrelationId);
    }

    @Override
    public void saveAcceptedParticipation(@NotNull Collaboration collaboration, @NotNull Participant participant) {
        CollaborationByIdMap mappedById = CollaborationByIdMap
                .mapFromEntity(collaboration, participant);
        cassandraTemplate.insert(mappedById);

        //TODO: byDeckId: update status of Participant FOR EVERY DECK

        List<CollaborationByUserIdMap> mappedByUserIds = collaboration.getParticipants().values().stream()
                .map(x -> CollaborationByUserIdMap
                        .mapFromEntity(x.getUserId(), collaboration, participant)
                ).toList();
        mappedByUserIds.forEach(cassandraTemplate::insert);
    }

    @Override
    public @NotNull Optional<Collaboration> findCollaborationById(@NotNull UUID collaborationId) {
        List<CollaborationByIdMap> byIdMaps = cassandraTemplate.select(
                query(where("collaboration_id").is(collaborationId)),
                CollaborationByIdMap.class
        );
        if (byIdMaps.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CollaborationByIdMap.mapToEntityFromDatabase(byIdMaps));
    }

    @Override
    public @NotNull Optional<UUID> findCollaborationIdByDeckCorrelationId(@NotNull UUID deckCorrelationId) {
        CollaborationByDeckCorrelationIdMap map = cassandraTemplate.selectOne(
                query(where("deck_correlation_id").is(deckCorrelationId)),
                CollaborationByDeckCorrelationIdMap.class
        );
        return map != null ? Optional.of(map.getCollaborationId()) : Optional.empty();
    }

    @Override
    public @NotNull Optional<Collaboration> findCollaborationByDeckCorrelationId(@NotNull UUID deckCorrelationId) {
        Optional<UUID> collaborationId = findCollaborationIdByDeckCorrelationId(deckCorrelationId);
        if (collaborationId.isEmpty()) {
            return Optional.empty();
        }
        return findCollaborationById(collaborationId.get());
    }

    @Override
    public @NotNull List<Collaboration> findCollaborationsByUserId(@NotNull UUID userId) {
        List<CollaborationByUserIdMap> byUserIdMaps = cassandraTemplate.select(
                query(where("user_id").is(userId)),
                CollaborationByUserIdMap.class
        );
        Map<UUID, List<CollaborationByUserIdMap>> filteredByCollabId = new HashMap<>();
        for (CollaborationByUserIdMap map : byUserIdMaps) {
            List<CollaborationByUserIdMap> updatedCollabList = Stream.concat(
                    (filteredByCollabId.containsKey(map.getCollaborationId()) ?
                            filteredByCollabId.get(map.getCollaborationId()) :
                            new ArrayList<CollaborationByUserIdMap>()
                    ).stream(),
                    Stream.of(map)
            ).collect(Collectors.toList());
            filteredByCollabId.put(map.getCollaborationId(), updatedCollabList);
        }
        return filteredByCollabId.values().stream().map(CollaborationByUserIdMap::mapToEntityFromDatabase).toList();
    }
}
