package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

@Component
public class KafkaProducer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final String topic;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public KafkaProducer(@Value("${general.kafka-topic}") String topic, KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String transactionLine) {
        try {
            logger.info("Raw Transaction Line Received: {}", transactionLine);
            if (transactionLine == null || transactionLine.trim().isEmpty()) {
                logger.error("Received an empty transaction line.");
                return;
            }

            transactionLine = transactionLine.trim().replaceAll("[\r\n]", "");
            String[] transactionData = transactionLine.split("\\s*,\\s*"); // Handles spaces around commas

            logger.debug("Parsed Transaction Data Array: {}", Arrays.toString(transactionData));

//            if (transactionData.length % 3 != 0) {
//                logger.error("Invalid transaction line format: {} | Data: {}", transactionLine, Arrays.toString(transactionData));
//                return;
//            }
            if (transactionData.length % 3 != 0 && transactionData.length % 2 != 0) {
                logger.error("Invalid transaction line format: {} | Data: {}", transactionLine, Arrays.toString(transactionData));
                return;
            }


            ObjectMapper objectMapper = new ObjectMapper();

            for (int i = 0; i < transactionData.length; i += 3) {
                try {
                    long senderId = parseLongSafe(transactionData[i], "Sender ID");
                    long recipientId = parseLongSafe(transactionData[i + 1], "Recipient ID");
                    float amount = parseFloatSafe(transactionData[i + 2]);

                    if (senderId == -1 || recipientId == -1 || amount == -1) {
                        logger.error("Skipping invalid transaction: {}", Arrays.toString(transactionData));
                        continue;
                    }

                    Transaction transaction = new Transaction(senderId, recipientId, amount);

                    String transactionJson = objectMapper.writeValueAsString(transaction);
                    logger.info("Serialized Transaction JSON: {}", transactionJson);

                    // Add Debugging Log BEFORE Sending to Kafka
                    logger.info("🚀 Sending to Kafka: senderId={}, recipientId={}, amount={}",
                            senderId, recipientId, amount);

                    kafkaTemplate.send(topic, transaction);
                    logger.info("✅ Sent Transaction: senderId={}, recipientId={}, amount={}",
                            senderId, recipientId, amount);

                } catch (Exception e) {
                    logger.error("Error parsing transaction: {}", Arrays.toString(transactionData), e);
                }
            }

        } catch (Exception e) {
            logger.error("Error processing transaction data: {}", transactionLine, e);
        }
    }

    private long parseLongSafe(String value, String fieldName) {
        try {
            if (value == null || value.trim().isEmpty()) {
                logger.error("Empty value received for {}.", fieldName);
                return -1;
            }

            value = value.trim();

            // Remove decimal if present (e.g., "12345.0" -> "12345")
            if (value.contains(".")) {
                value = value.split("\\.")[0];
            }

            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.error("Invalid long value for {}: '{}'", fieldName, value);
            return -1;
        }
    }

    private float parseFloatSafe(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                logger.error("Empty value received for Amount.");
                return -1;
            }
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid float value: '{}'", value);
            return -1;
        }
    }
}

