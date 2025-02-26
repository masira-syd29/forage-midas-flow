package com.jpmc.midascore;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskThreeTests {
    static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Test
    void task_three_verifier() throws InterruptedException {
        // Populate users before sending transactions
        userPopulator.populate();

        // Load transactions from the test file
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
        assertNotNull(transactionLines, "Transaction lines should not be null");

        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Give Kafka some time to process
        Thread.sleep(5000); // Increase delay if needed

        logger.info("----------------------------------------------------------");
        logger.info("Check Kafka processing results...");

        // Instead of infinite loop, log a message and exit
        for (int i = 0; i < 3; i++) {
            Thread.sleep(5000);
            logger.info("Waiting for Kafka processing...");
        }

        logger.info("Test execution completed. Check processed transactions in Kafka.");
    }
}
