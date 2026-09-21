package com.cg.config;

import com.cg.auth.User;
import com.cg.auth.UserRepository;
import com.cg.book.Book;
import com.cg.book.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Loads safe development data when the application starts.
 *
 * <p>The ADMIN account is controlled by configuration and should be disabled
 * in environments where administrator provisioning is managed externally.</p>
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            UserRepository users,
            BookRepository books,
            PasswordEncoder encoder,
            @Value("${app.admin.bootstrap-enabled:false}") boolean adminBootstrapEnabled,
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:admin123}") String adminPassword) {

        return args -> {
            if (!users.existsByUsername("demo")) {
                users.save(new User("demo", encoder.encode("password"), "USER"));
            }

            if (adminBootstrapEnabled && !users.existsByUsername(adminUsername)) {
                users.save(new User(adminUsername, encoder.encode(adminPassword), "ADMIN"));
            }

            if (books.count() == 0) {
                books.save(new Book(
                        "Effective Java", "Joshua Bloch", "9780134685991",
                        new BigDecimal("650.00"), 10));
                books.save(new Book(
                        "Clean Code", "Robert C. Martin", "9780132350884",
                        new BigDecimal("550.00"), 8));
            }
        };
    }
}
