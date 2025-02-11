package com.jpmc.midascore.listener;

import com.jpmc.midascore.foundation.Transaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, Transaction> record) {
        Transaction transaction = record.value();
        logger.info("Received Transaction: senderId={}, recipientId={}, amount={}",
                transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
    }
}

