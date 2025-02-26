package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DatabaseConduit {
    public static final Logger logger = LogManager.getLogger(DatabaseConduit.class);
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // Invalid sender or recipient, discard transaction
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();
        float amount = transaction.getAmount();

        if (sender.getBalance() < amount) {
            return; // Insufficient funds, discard transaction
        }

        // Deduct from sender and add to recipient
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        if (sender.getName().equals("waldorf")) {
            logger.info("Updated Balance: User={} Balance={}", sender.getName(), sender.getBalance());
        }

        // Save transaction record
        transactionRecordRepository.save(new TransactionRecord(sender, recipient, amount));

        // Save updated user balances
        userRepository.save(sender);
        userRepository.save(recipient);
    }

    public void save(UserRecord user) {
        userRepository.save(user);
    }
}

