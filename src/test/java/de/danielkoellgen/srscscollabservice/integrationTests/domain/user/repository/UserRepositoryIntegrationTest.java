package de.danielkoellgen.srscscollabservice.integrationTests.domain.user.repository;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.UUID;

@SpringBootTest
public class UserRepositoryIntegrationTest {

    private final CassandraOperations cassandraTemplate;

    @Autowired
    public UserRepositoryIntegrationTest(CassandraOperations cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    @Test
    public void shouldSaveAndLoadUser() throws Exception {
        // given
        User user = new User(UUID.randomUUID(), new Username("dadepu"), true);

        // when
        cassandraTemplate.insert(user);
    }
}
