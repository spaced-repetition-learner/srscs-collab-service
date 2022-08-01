package de.danielkoellgen.srscscollabservice.domain.user.repository;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.maps.UserByUserIdMap;
import de.danielkoellgen.srscscollabservice.domain.user.repository.maps.UserByUsernameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Component
@Scope("singleton")
public class UserRepositoryImpl implements UserRepository {

    private final CassandraOperations cassandraTemplate;
    private final CqlTemplate cqlTemplate;

    private final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Autowired
    public UserRepositoryImpl(CassandraOperations cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }

    @Override
    public void save(User user) {
        logger.trace("Saving User...");
        UserByUserIdMap userByUserIdMap = new UserByUserIdMap(user);
        logger.debug("{}", userByUserIdMap);
        UserByUsernameMap userByUsernameMap = new UserByUsernameMap(user);
        logger.debug("{}", userByUsernameMap);
        cassandraTemplate.batchOps()
                .insert(userByUserIdMap)
                .insert(userByUsernameMap)
                .execute();
    }

    @Override
    public Optional<User> findUserByUserId(UUID userId) {
        UserByUserIdMap userByUserIdMap = cassandraTemplate.selectOne(
                query(where("user_id").is(userId)),
                UserByUserIdMap.class);
        if (userByUserIdMap != null) {
            try {
                return Optional.of(userByUserIdMap.mapToUser());
            } catch (Exception e) {
                throw new RuntimeException("Invalid mapping to type username from database.");
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserByUsername(Username username) {
        UserByUsernameMap userByUsernameMap = cassandraTemplate.selectOne(
                query(where("username").is(username.getUsername())),
                UserByUsernameMap.class);
        if (userByUsernameMap != null) {
            try {
                return Optional.of(userByUsernameMap.mapToUser());
            } catch (Exception e) {
                throw new RuntimeException("Invalid mapping to type username from database.");
            }
        } else {
            return Optional.empty();
        }
    }
}
