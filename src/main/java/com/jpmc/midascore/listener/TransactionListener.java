package com.jpmc.midascore.listener;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.foundation.Transaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    private final DatabaseConduit databaseConduit;

    public TransactionListener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, Transaction> record) {
        logger.info("Raw Kafka Message: {}", record.value());

        Transaction transaction = record.value();
        if (transaction == null) {
            logger.error("Deserialization failed. Transaction is null.");
            return;
        }

        logger.info("Processing Transaction: senderId={}, recipientId={}, amount={}",
                transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());

        // Process the transaction
        databaseConduit.processTransaction(transaction);
    }
}






