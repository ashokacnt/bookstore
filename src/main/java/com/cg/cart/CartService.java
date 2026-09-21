package com.cg.cart;

import com.cg.auth.User;
import com.cg.auth.UserRepository;
import com.cg.book.Book;
import com.cg.book.BookRepository;
import com.cg.common.BadRequestException;
import com.cg.common.ConflictException;
import com.cg.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business operations for a user's shopping cart.
 *
 * <p>Cart ownership is always derived from the authenticated username rather
 * than from a user ID supplied by the client.</p>
 */
@Service
public class CartService {

    private final CartItemRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public CartDtos.CartResponse get(String username) {
        return toResponse(cartRepository.findByUserUsernameOrderById(username));
    }

    /**
     * Adds a book to the user's cart, increasing the existing quantity when
     * the same book is already present.
     */
    @Transactional
    public CartDtos.CartResponse add(String username, CartDtos.AddItemRequest request) {
        validatePositiveQuantity(request.quantity());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + request.bookId()));

        if (!book.isActive()) {
            throw new BadRequestException("Book is not available");
        }

        var existing = cartRepository.findByUserUsernameAndBookId(username, request.bookId());
        int requestedQuantity = existing
                .map(item -> item.getQuantity() + request.quantity())
                .orElse(request.quantity());

        validateStock(book, requestedQuantity);

        if (existing.isPresent()) {
            existing.get().setQuantity(requestedQuantity);
        } else {
            cartRepository.save(new CartItem(user, book, requestedQuantity));
        }

        return get(username);
    }

    /**
     * Updates a cart item after verifying that the item belongs to the user
     * and that the requested quantity is available in stock.
     */
    @Transactional
    public CartDtos.CartResponse update(
            String username,
            Long itemId,
            CartDtos.UpdateItemRequest request) {

        validatePositiveQuantity(request.quantity());

        CartItem item = cartRepository.findByIdAndUserUsername(itemId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        validateStock(item.getBook(), request.quantity());
        item.setQuantity(request.quantity());
        return get(username);
    }

    @Transactional
    public void remove(String username, Long itemId) {
        CartItem item = cartRepository.findByIdAndUserUsername(itemId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cartRepository.delete(item);
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
    }

    private void validateStock(Book book, int quantity) {
        if (quantity > book.getStockQuantity()) {
            throw new ConflictException("Requested quantity exceeds available stock");
        }
    }

    private CartDtos.CartResponse toResponse(List<CartItem> items) {
        var responseItems = items.stream()
                .map(item -> new CartDtos.CartItemResponse(
                        item.getId(),
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getQuantity(),
                        item.getBook().getPrice(),
                        item.getBook().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        BigDecimal total = responseItems.stream()
                .map(CartDtos.CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDtos.CartResponse(responseItems, total);
    }
}
