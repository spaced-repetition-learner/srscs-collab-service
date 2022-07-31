package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CorrelationByCardId;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CorrelationByCollaborationId;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CorrelationByCorrelationId;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CorrelationByRootCardId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Component
@Scope("singleton")
public class CollaborationCardRepositoryImpl implements CollaborationCardRepository {

    private final CassandraOperations cassandraTemplate;

    @Autowired
    public CollaborationCardRepositoryImpl(CassandraOperations cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    @Override
    @NewSpan("save collaboration-card")
    public void saveNewCollaborationCard(@NotNull CollaborationCard collaborationCard) {
        List<CorrelationByCorrelationId> mappedByCorrelationId = CorrelationByCorrelationId.mapFromEntity(
                collaborationCard.getCorrelations());
        mappedByCorrelationId.forEach(cassandraTemplate::insert);

        List<CorrelationByRootCardId> mappedByRootCardId = CorrelationByRootCardId.mapFromEntity(
                collaborationCard.getCollaborationId(),
                collaborationCard.getCollaborationCardId(),
                collaborationCard.getCorrelations());
        mappedByRootCardId.forEach(cassandraTemplate::insert);

        CorrelationByCollaborationId mappedByCollaborationId = CorrelationByCollaborationId.mapFromEntity(
                collaborationCard.getCollaborationId(), collaborationCard.getCorrelations().get(0));
        cassandraTemplate.insert(mappedByCollaborationId);

        List<CorrelationByCardId> mappedByCardId = collaborationCard.getCorrelations().stream()
                .filter(x -> x.cardId() != null)
                .map(CorrelationByCardId::mapFromEntity)
                .toList();
        mappedByCardId.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("save correlation")
    public void saveUpdatedCorrelation(@NotNull CollaborationCard collaborationCard, @NotNull Correlation correlation) {
        assert correlation.cardId() != null;

        CorrelationByCardId mappedByCardId = CorrelationByCardId.mapFromEntity(correlation);
        cassandraTemplate.insert(mappedByCardId);

        List<CorrelationByRootCardId> mappedByRootCardId = CorrelationByRootCardId.mapFromEntity(
                collaborationCard.getCollaborationId(),
                collaborationCard.getCollaborationCardId(),
                List.of(correlation)
        );
        mappedByRootCardId.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("save card-version")
    public void saveNewCardVersion(@NotNull CollaborationCard collaborationCard, @NotNull List<Correlation> newCorrelations) {
        CorrelationByCollaborationId mappedByCollaborationId = CorrelationByCollaborationId.mapFromEntity(
                collaborationCard.getCollaborationId(), newCorrelations.get(0));
        cassandraTemplate.insert(mappedByCollaborationId);

        List<CorrelationByCorrelationId> mappedByCorrelationIds = CorrelationByCorrelationId.mapFromEntity(
                newCorrelations);
        mappedByCorrelationIds.forEach(cassandraTemplate::insert);

        List<CorrelationByRootCardId> mappedByRootCardId = CorrelationByRootCardId.mapFromEntity(
                collaborationCard.getCollaborationId(),
                collaborationCard.getCollaborationCardId(),
                newCorrelations);
        mappedByRootCardId.forEach(cassandraTemplate::insert);

        List<CorrelationByCardId> mappedByCardId = newCorrelations.stream()
                .filter(x -> x.cardId() != null)
                .map(CorrelationByCardId::mapFromEntity)
                .toList();
        mappedByCardId.forEach(cassandraTemplate::insert);
    }

    @Override
    @NewSpan("find collaboration-card by correlation-id")
    public Optional<CollaborationCard> findCollaborationCardWithCorrelation_byCorrelationId(@NotNull UUID correlationId) {
        CorrelationByCorrelationId byCorrelationId = cassandraTemplate.selectOne(
                query(where("correlation_id").is(correlationId)), CorrelationByCorrelationId.class
        );
        if (byCorrelationId == null) {
            return Optional.empty();
        }
        UUID rootCardId = byCorrelationId.getRootCardId();
        List<CorrelationByRootCardId> byRootCardIds = cassandraTemplate.select(
                query(where("root_card_id").is(rootCardId)), CorrelationByRootCardId.class
        );
        if (byRootCardIds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new CollaborationCard(
                byRootCardIds.get(0).getCollaborationCardId(),
                byRootCardIds.get(0).getCollaborationId(),
                null,
                byRootCardIds.stream()
                        .filter(x -> x.getCorrelationId().equals(correlationId))
                        .map(Correlation::new)
                        .toList()
        ));
    }

    @Override
    @NewSpan("find collaboration-card by root-card-id")
    public Optional<CollaborationCard> findCollaborationCardWithCorrelation_byCardIdOnRootCardId(@NotNull UUID cardId) {
        CorrelationByCardId byCardId = cassandraTemplate.selectOne(
                query(where("card_id").is(cardId)), CorrelationByCardId.class
        );
        if (byCardId == null) {
            return Optional.empty();
        }
        UUID rootCardId = byCardId.getRootCardId();
        List<CorrelationByRootCardId> byRootCardIds = cassandraTemplate.select(
                query(where("root_card_id").is(rootCardId)), CorrelationByRootCardId.class
        );
        if (byRootCardIds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new CollaborationCard(
                byRootCardIds.get(0).getCollaborationCardId(),
                byRootCardIds.get(0).getCollaborationId(),
                null,
                byRootCardIds.stream()
                        .map(Correlation::new)
                        .toList()
        ));
    }
}
