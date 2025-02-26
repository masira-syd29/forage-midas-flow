package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class TaskTwoTests {
    static final Logger logger = LoggerFactory.getLogger(TaskTwoTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private FileLoader fileLoader;

    @Test
    void task_two_verifier() throws InterruptedException {
        // Load transaction data from the test file
        String[] transactionLines = fileLoader.loadStrings("/test_data/poiuytrewq.uiop");
        logger.info("Loaded Transactions: {}", Arrays.toString(transactionLines));

        if (transactionLines == null || transactionLines.length == 0) {
            logger.error("No transactions found in the test file.");
            return;
        }

        // Process valid transactions
        for (String transactionLine : transactionLines) {
            if (transactionLine == null || transactionLine.trim().isEmpty()) {
                logger.warn("Skipping empty transaction line.");
                continue;
            }
            kafkaProducer.send(transactionLine);
        }

        // Allow some time for Kafka to process messages
        Thread.sleep(5000);

        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to watch for incoming transactions");
        logger.info("Test execution complete. Check logs for received transactions.");
    }
}
