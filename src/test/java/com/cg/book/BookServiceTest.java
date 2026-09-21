package com.cg.book;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import com.cg.common.ConflictException;

class BookServiceTest {
    @Test
    void addBookRejectsDuplicateIsbn() {
        BookRepository repo = mock(BookRepository.class);
        when(repo.findByIsbn("123")).thenReturn(Optional.of(new Book()));
        BookService service = new BookService(repo);
        assertThatThrownBy(() -> service.add(new BookDtos.CreateBookRequest("T", "A", "123", BigDecimal.TEN, 1))).isInstanceOf(ConflictException.class);
    }

    @Test
    void listReturnsOnlyActiveBooks() {
        BookRepository repo = mock(BookRepository.class);
        Book b = new Book("T", "A", "1", BigDecimal.TEN, 2);
        b.setActive(true);
        when(repo.findAll()).thenReturn(java.util.List.of(b));
        assertThat(new BookService(repo).findAll()).hasSize(1);
    }
}
