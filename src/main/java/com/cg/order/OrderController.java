package com.cg.order;

import com.cg.common.IdempotencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST APIs for checkout and customer order history.
 *
 * <p>Checkout converts the authenticated user's current cart into an order.
 * The Idempotency-Key header protects the operation from duplicate order
 * creation when a client retries a request after a timeout.</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final IdempotencyService idempotency;

    public OrderController(OrderService service, IdempotencyService idempotency) {
        this.service = service;
        this.idempotency = idempotency;
    }

    /**
     * Creates an order from the authenticated user's cart.
     *
     * <p>The service validates the cart, verifies stock, creates the order,
     * decreases inventory and clears the cart. A repeated request using the
     * same idempotency key returns the previously created order.</p>
     *
     * @param authentication authenticated security context
     * @param key            client-generated idempotency key
     * @return the created order, or the original order for a safe retry
     */
    @PostMapping
    public ResponseEntity<OrderDtos.OrderResponse> checkout(
            Authentication authentication,
            @RequestHeader("Idempotency-Key") String key) {
        var existing = idempotency.get(authentication.getName(), key);
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(existing);
        }

        var created = service.checkout(authentication.getName());
        idempotency.put(authentication.getName(), key, created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Returns the authenticated user's order history.
     *
     * @param authentication authenticated security context
     * @return orders belonging only to the current user
     */
    @GetMapping
    public List<OrderDtos.OrderResponse> history(Authentication authentication) {
        return service.history(authentication.getName());
    }
}
