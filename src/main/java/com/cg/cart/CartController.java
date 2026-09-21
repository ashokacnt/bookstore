package com.cg.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST APIs for the authenticated customer's shopping cart.
 *
 * <p>All cart operations are scoped to the currently authenticated username;
 * a client cannot access another customer's cart by supplying another user ID.</p>
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    /**
     * Returns the current user's cart and calculated total.
     *
     * @param authentication authenticated security context
     * @return the user's cart
     */
    @GetMapping
    public CartDtos.CartResponse get(Authentication authentication) {
        return service.get(authentication.getName());
    }

    /**
     * Adds a book to the authenticated user's cart.
     *
     * @param authentication authenticated security context
     * @param request        book ID and requested quantity
     * @return the updated cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartDtos.CartResponse> add(
            Authentication authentication,
            @Valid @RequestBody CartDtos.AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(authentication.getName(), request));
    }

    /**
     * Changes the quantity of an existing cart item.
     *
     * @param authentication authenticated security context
     * @param itemId         cart item ID owned by the current user
     * @param request        new quantity
     * @return the updated cart
     */
    @PutMapping("/items/{itemId}")
    public CartDtos.CartResponse update(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody CartDtos.UpdateItemRequest request) {
        return service.update(authentication.getName(), itemId, request);
    }

    /**
     * Removes an item from the authenticated user's cart.
     *
     * @param authentication authenticated security context
     * @param itemId         cart item ID owned by the current user
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> remove(
            Authentication authentication,
            @PathVariable Long itemId) {
        service.remove(authentication.getName(), itemId);
        return ResponseEntity.noContent().build();
    }
}
