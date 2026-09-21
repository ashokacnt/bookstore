package com.cg.order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import com.cg.auth.User;
import com.cg.auth.UserRepository;
import com.cg.cart.CartItemRepository;
import com.cg.common.BadRequestException;
import org.junit.jupiter.api.Test;

class OrderServiceTest {
    @Test
    void checkoutRejectsEmptyCart() {
        var orders = mock(CustomerOrderRepository.class);
        var carts = mock(CartItemRepository.class);
        var users = mock(UserRepository.class);
        when(users.findByUsername("demo")).thenReturn(Optional.of(new User("demo", "p", "USER")));
        when(carts.findByUserUsernameOrderById("demo")).thenReturn(List.of());
        assertThatThrownBy(() -> new OrderService(orders, carts, users).checkout("demo")).isInstanceOf(BadRequestException.class);
    }
}
