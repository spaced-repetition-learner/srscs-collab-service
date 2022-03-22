package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CollaborationCardByCardIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CollaborationCardByCollaborationIdMap;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CollaborationCardByTransactionIdMap;
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
public class CollaborationCardRepositoryImpl implements CollaborationCardRepository {

    private final CassandraOperations cassandraTemplate;
    private final CqlTemplate cqlTemplate;

    @Autowired
    public CollaborationCardRepositoryImpl(CassandraOperations cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }

    @Override
    public @NotNull Optional<CollaborationCard> findCollaborationCardByTransactionId(@NotNull UUID transactionId) {
        CollaborationCardByTransactionIdMap resultMap = this.cassandraTemplate.selectOne(
                query(where("correlation_card_transaction_id").is(transactionId)),
                CollaborationCardByTransactionIdMap.class
        );
        return resultMap != null ? Optional.of(CollaborationCardByTransactionIdMap.mapToEntityFromDatabase(resultMap)) :
                Optional.empty();
    }

    @Override
    public @NotNull Optional<CollaborationCard> findCollaborationCardByCardId(@NotNull UUID cardId) {
        CollaborationCardByCardIdMap resultMap = this.cassandraTemplate.selectOne(
                query(where("correlation_card_id").is(cardId)),
                CollaborationCardByCardIdMap.class
        );
        return resultMap != null ? Optional.of(CollaborationCardByCardIdMap.mapToEntityFromDatabase(resultMap)) :
                Optional.empty();
    }

    @Override
    public @NotNull List<CollaborationCard> findCollaborationCardsByCollaborationId(@NotNull UUID collaborationId) {
        List<CollaborationCardByCollaborationIdMap> resultMaps = this.cassandraTemplate.select(
                query(where("collaboration_id").is(collaborationId)),
                CollaborationCardByCollaborationIdMap.class
        );
        return CollaborationCardByCollaborationIdMap.mapToEntityFromDatabase(resultMaps);
    }

    @Override
    public void saveAppendedCard(@NotNull CollaborationCard collaborationCard, @NotNull CardVersion cardVersion,
            @NotNull Correlation correlation) {
        assert correlation.card() != null;
        CollaborationCardByCardIdMap cardIdMap = new CollaborationCardByCardIdMap(
                correlation.card().getCardId(),
                collaborationCard.getCollaborationCardId(),
                cardVersion.getCardVersionId(),
                collaborationCard.getCollaboration().getCollaborationId()
        );
        cassandraTemplate.insert(cardIdMap);
    }

    @Override
    public void saveNewCollaborationCard(@NotNull CollaborationCard collaborationCard) {
        CardVersion cardVersion = collaborationCard.getCardVersions().get(0);
        assert cardVersion.getRootCard() != null;

        CollaborationCardByCollaborationIdMap collaborationIdMap = new CollaborationCardByCollaborationIdMap(
                collaborationCard.getCollaboration().getCollaborationId(),
                collaborationCard.getCollaborationCardId(),
                cardVersion.getCardVersionId(),
                cardVersion.getRootCard().getCardId()
        );
        List<CollaborationCardByTransactionIdMap> transactionIdMaps = new ArrayList<>();
        for (Correlation correlation : cardVersion.getCorrelations().stream().toList()) {
            transactionIdMaps.add(new CollaborationCardByTransactionIdMap(
                    correlation.transactionId(),
                    collaborationCard.getCollaboration().getCollaborationId(),
                    collaborationCard.getCollaborationCardId(),
                    cardVersion.getCardVersionId()
            ));
        }
        CollaborationCardByCardIdMap cardIdMap = new CollaborationCardByCardIdMap(
                cardVersion.getRootCard().getCardId(),
                collaborationCard.getCollaborationCardId(),
                cardVersion.getCardVersionId(),
                collaborationCard.getCollaboration().getCollaborationId()
        );
        cassandraTemplate.insert(collaborationIdMap);
        transactionIdMaps.forEach(cassandraTemplate::insert);
        cassandraTemplate.insert(cardIdMap);
    }
}
