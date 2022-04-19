package de.danielkoellgen.srscscollabservice.events.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ProducerEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(event.getTopic(), event.getSerializedContent());
        record.headers().add(new RecordHeader("eventId", event.getEventId().toString().getBytes()));
        record.headers().add(new RecordHeader("transactionId", event.getTransactionId().toString().getBytes()));
        record.headers().add(new RecordHeader("timestamp", event.getOccurredAt().getFormatted().getBytes()));
        record.headers().add(new RecordHeader("type", event.getEventName().getBytes()));
        kafkaTemplate.send(record);
        logger.trace("Published Event '{}' to {}. [tid={}, payload={}]",
                event.getEventName(), event.getTopic(), event.getTransactionId(), event.getSerializedContent());
    }
}
