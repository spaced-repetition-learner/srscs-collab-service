package de.danielkoellgen.srscscollabservice.core.converter;

import de.danielkoellgen.srscscollabservice.events.producer.ProducerEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class ProducerEventToConsumerRecordConverter implements
        Converter<ProducerEvent, ConsumerRecord<String, String>> {

    @Override
    public ConsumerRecord<String, String> convert(ProducerEvent event) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                event.getTopic(), 1, 1L, "", event.getSerializedContent());
        record.headers().add(
                new RecordHeader("eventId", event.getEventId().toString().getBytes()));
        record.headers().add(
                new RecordHeader("transactionId", event.getTransactionId().toString().getBytes()));
        record.headers().add(
                new RecordHeader("timestamp", event.getOccurredAt().getFormatted().getBytes()));
        record.headers().add(
                new RecordHeader("type", event.getEventName().getBytes()));
        return record;
    }
}
