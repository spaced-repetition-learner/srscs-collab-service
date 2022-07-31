package de.danielkoellgen.srscscollabservice.events.consumer;

import de.danielkoellgen.srscscollabservice.core.converter.ProducerEventToConsumerRecordConverter;
import de.danielkoellgen.srscscollabservice.core.events.producer.UserCreatedProd;
import de.danielkoellgen.srscscollabservice.core.events.producer.UserDisabledProd;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscscollabservice.events.consumer.user.KafkaUserEventConsumer;
import de.danielkoellgen.srscscollabservice.events.consumer.user.dto.UserCreatedDto;
import de.danielkoellgen.srscscollabservice.events.consumer.user.dto.UserDisabledDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class KafkaUserEventConsumerIntegrationTest {

    private final KafkaUserEventConsumer kafkaUserEventConsumer;

    private final UserService userService;

    private final UserRepository userRepository;

    private final ProducerEventToConsumerRecordConverter mapToConsumerRecordConverter;

    private UUID userId;
    private Username username;

    @Autowired
    public KafkaUserEventConsumerIntegrationTest(KafkaUserEventConsumer kafkaUserEventConsumer, UserService userService,
            ProducerEventToConsumerRecordConverter mapToConsumerRecordConverter, UserRepository userRepository) {
        this.kafkaUserEventConsumer = kafkaUserEventConsumer;
        this.userService = userService;
        this.mapToConsumerRecordConverter = mapToConsumerRecordConverter;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() throws Exception {
        userId = UUID.randomUUID();
        username = new Username("dadepu");
    }

    @AfterEach
    public void cleanUp() {

    }

    @Test
    public void shouldCreateUserWhenReceivingUserCreatedEvent() throws Exception {
        // given
        UserCreatedProd userCreatedProd = new UserCreatedProd(
                "", new UserCreatedDto(userId, username.getUsername())
        );

        // when
        kafkaUserEventConsumer.receive(
                mapToConsumerRecordConverter.convert(userCreatedProd)
        );

        // then
        User fetchedUser = userRepository.findUserByUserId(userId).orElseThrow();
        assertThat(fetchedUser.getUsername())
                .isEqualTo(username);

        // and then
        User fetchedUserByUsername = userRepository.findUserByUsername(username).orElseThrow();
        assertThat(fetchedUserByUsername.getUserId())
                .isEqualTo(userId);
    }

    @Test
    public void shouldDisableUserWhenReceivingUserDisabledEvent() throws Exception {
        // given
        userService.addExternallyCreatedUser(userId, username);
        UserDisabledProd userDisabledProd = new UserDisabledProd("", new UserDisabledDto(userId));

        // when
        kafkaUserEventConsumer.receive(mapToConsumerRecordConverter.convert(userDisabledProd));

        // then
        User fetchedUserById = userRepository.findUserByUserId(userId).orElseThrow();
        assertThat(fetchedUserById.getIsActive())
                .isFalse();

        // and then
        User fetchedUserByUsername = userRepository.findUserByUsername(username).orElseThrow();
        assertThat(fetchedUserByUsername.getIsActive())
                .isFalse();
    }
}
