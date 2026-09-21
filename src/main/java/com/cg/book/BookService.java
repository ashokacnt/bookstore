package com.cg.book;

import com.cg.common.ConflictException;
import com.cg.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business operations for bookstore inventory.
 */
@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns only active books so inactive inventory is not exposed through
     * the public catalog API.
     */
    public List<BookDtos.BookResponse> findAll() {
        return repository.findAll().stream()
                .filter(Book::isActive)
                .map(this::toDto)
                .toList();
    }

    /**
     * Creates a book after enforcing ISBN uniqueness.
     */
    @Transactional
    public BookDtos.BookResponse add(BookDtos.CreateBookRequest request) {
        if (repository.findByIsbn(request.isbn()).isPresent()) {
            throw new ConflictException("Book ISBN already exists: " + request.isbn());
        }

        Book book = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.price(),
                request.stockQuantity());

        return toDto(repository.save(book));
    }

    /**
     * Finds a book for internal business operations.
     */
    public Book get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    private BookDtos.BookResponse toDto(Book book) {
        return new BookDtos.BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStockQuantity(),
                book.isActive());
    }
}
