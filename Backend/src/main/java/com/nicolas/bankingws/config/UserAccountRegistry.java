package com.nicolas.bankingws.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserAccountRegistry {

    private final Map<String, String> userToAccount = new ConcurrentHashMap<>();

    public UserAccountRegistry() {
        userToAccount.put("nicolas", "ACC-1001");
        userToAccount.put("julian", "ACC-1002");
        userToAccount.put("carla", "ACC-1003");
    }

    public String getAccountForUser(String username) {
        return userToAccount.get(username);
    }
}
