package de.danielkoellgen.srscscollabservice.integrationTests.domain.user.repository;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserRepositoryIntegrationTest {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryIntegrationTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @AfterEach
    public void cleanUp() {
    }

    @Test
    public void shouldAllowToSaveAndLoadUser() throws Exception {
        // given
        User user = new User(UUID.randomUUID(), new Username("dadepu"), true);

        // when
        userRepository.save(user);

        // then
        User userById = userRepository.findUserByUserId(user.getUserId()).get();
        assertThat(userById.getUsername())
                .isEqualTo(userById.getUsername());

        // and then
        User userByUsername = userRepository.findUserByUsername(user.getUsername()).get();
        assertThat(userByUsername.getUsername())
                .isEqualTo(userByUsername.getUsername());
    }
}
