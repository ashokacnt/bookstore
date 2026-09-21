package com.cg.order;

import com.cg.auth.UserRepository;
import com.cg.cart.CartItemRepository;
import com.cg.common.BadRequestException;
import com.cg.common.ConflictException;
import com.cg.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business operations for checkout and order history.
 */
@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final CartItemRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        CartItemRepository cartRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    /**
     * Atomically converts the current cart into an order.
     *
     * <p>The operation validates every item before changing inventory. The
     * transaction ensures that order creation, stock reduction and cart
     * cleanup succeed or fail together.</p>
     */
    @Transactional
    public OrderDtos.OrderResponse checkout(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var cartItems = cartRepository.findByUserUsernameOrderById(username);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart");
        }

        BigDecimal total = BigDecimal.ZERO;

        // Validate the complete cart before changing any stock.
        for (var item : cartItems) {
            if (!item.getBook().isActive()) {
                throw new ConflictException("Book is not available: " + item.getBook().getTitle());
            }
            if (item.getQuantity() > item.getBook().getStockQuantity()) {
                throw new ConflictException("Insufficient stock for: " + item.getBook().getTitle());
            }

            total = total.add(item.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        CustomerOrder order = new CustomerOrder(user, total);

        // Capture the current price in each order item and then decrement stock.
        for (var item : cartItems) {
            item.getBook().setStockQuantity(
                    item.getBook().getStockQuantity() - item.getQuantity());
            order.addItem(new OrderItem(
                    item.getBook(),
                    item.getQuantity(),
                    item.getBook().getPrice()));
        }

        CustomerOrder savedOrder = orderRepository.save(order);
        cartRepository.deleteAll(cartItems);
        return toResponse(savedOrder);
    }

    public List<OrderDtos.OrderResponse> history(String username) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderDtos.OrderResponse toResponse(CustomerOrder order) {
        var items = order.getItems().stream()
                .map(item -> new OrderDtos.OrderItemResponse(
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return new OrderDtos.OrderResponse(
                order.getId(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                items);
    }
}
