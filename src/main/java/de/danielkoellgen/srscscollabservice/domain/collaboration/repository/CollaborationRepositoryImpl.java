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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Component
@Scope("singleton")
public class CollaborationRepositoryImpl implements CollaborationRepository {

    private final CassandraOperations cassandraTemplate;
    private final CqlTemplate cqlTemplate;

    @Autowired
    public CollaborationRepositoryImpl(CassandraOperations cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }

    @Override
    public void saveNewCollaboration(@NotNull Collaboration collaboration) {
        List<CollaborationByIdMap> mappedByIds = new ArrayList<>();
        List<CollaborationByUserIdMap> mappedByUserIds = new ArrayList<>();
        List<CollaborationByDeckCorrelationIdMap> mappedByDeckCorrelationIds = new ArrayList<>();

        for (Participant participant : collaboration.getParticipants().values()) {
            mappedByIds.add(new CollaborationByIdMap(
                    collaboration.getCollaborationId(),
                    participant.getUser().getUserId(),
                    collaboration.getName().getName(),
                    participant.getUser().getUsername().getUsername(),
                    null,
                    participant.getDeckCorrelationId(),
                    participant.getStatus().stream().map(ParticipantStateMap::new).toList()
            ));

            for (Participant innerParticipant: collaboration.getParticipants().values()) {
                mappedByUserIds.add(new CollaborationByUserIdMap(
                        participant.getUser().getUserId(),
                        collaboration.getCollaborationId(),
                        innerParticipant.getUser().getUserId(),
                        collaboration.getName().getName(),
                        innerParticipant.getUser().getUsername().getUsername(),
                        null,
                        innerParticipant.getStatus().stream().map(ParticipantStateMap::new).toList()
                ));
            }

            mappedByDeckCorrelationIds.add(new CollaborationByDeckCorrelationIdMap(
                    participant.getDeckCorrelationId(),
                    collaboration.getCollaborationId()
            ));
        }
        mappedByIds.forEach(cassandraTemplate::insert);
        mappedByUserIds.forEach(cassandraTemplate::insert);
        mappedByDeckCorrelationIds.forEach(cassandraTemplate::insert);
    }

    @Override
    public void saveNewParticipant(@NotNull Collaboration collaboration, @NotNull Participant newParticipant) {

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
        return List.of();
    }
}
