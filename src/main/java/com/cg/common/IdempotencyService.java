package com.cg.common;

import com.cg.order.OrderDtos;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keeps checkout responses for retry-safe processing during the application
 * lifetime. A key is also associated with the authenticated user so one user
 * cannot replay another user's checkout response.
 */
@Service
public class IdempotencyService {

    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();

    public OrderDtos.OrderResponse get(String username, String key) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return null;
        }

        if (!entry.username().equals(username)) {
            throw new ConflictException("Idempotency-Key is already associated with another user");
        }

        return entry.response();
    }

    public void put(String username, String key, OrderDtos.OrderResponse response) {
        entries.putIfAbsent(key, new Entry(username, response));
    }

    private record Entry(String username, OrderDtos.OrderResponse response) {
    }
}
