package com.cxy.travelaiagent.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSchemaInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserSchemaInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.initSchema();
    }
}
