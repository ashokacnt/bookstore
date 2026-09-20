package com.cg.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository r, PasswordEncoder e) {
        repo = r;
        encoder = e;
    }

}
