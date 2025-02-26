package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPopulator {
    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private DatabaseConduit databaseConduit;

    public void populate() {
        String[] userLines = fileLoader.loadStrings("/test_data/lkjhgfdsa.hjkl");

        for (String userLine : userLines) {
            String[] userData = userLine.trim().split(",\\s*"); // Split correctly with optional space
            if (userData.length != 2) {
                System.err.println("Skipping invalid line: " + userLine);
                continue; // Skip malformed lines
            }

            try {
                float amount = Float.parseFloat(userData[1].trim()); // Remove extra spaces
                UserRecord user = new UserRecord(userData[0].trim(), amount);
                databaseConduit.save(user);
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid amount: " + userData[1] + " in line: " + userLine);
            }
        }
    }

}
