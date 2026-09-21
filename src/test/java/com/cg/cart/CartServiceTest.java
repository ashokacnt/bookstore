package com.cg.cart;

import java.math.BigDecimal;
import java.util.Optional;
import com.cg.auth.User;
import com.cg.auth.UserRepository;
import com.cg.book.Book;
import com.cg.book.BookRepository;
import com.cg.common.ConflictException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartServiceTest {
    @Test
    void addFailsWhenStockIsInsufficient() {
        var carts = mock(CartItemRepository.class);
        var books = mock(BookRepository.class);
        var users = mock(UserRepository.class);
        var u = new User("demo", "p", "USER");
        var b = new Book("T", "A", "1", BigDecimal.TEN, 1);
        when(users.findByUsername("demo")).thenReturn(Optional.of(u));
        when(books.findById(1L)).thenReturn(Optional.of(b));
        assertThatThrownBy(() -> new CartService(carts, books, users).add("demo", new CartDtos.AddItemRequest(1L, 2))).isInstanceOf(ConflictException.class);
    }
}
