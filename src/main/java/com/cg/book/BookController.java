package com.cg.book;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST APIs for bookstore inventory.
 *
 * <p>Book browsing is public. Creating inventory records is restricted to
 * users with the ADMIN role.</p>
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    /**
     * Returns all books currently available in the bookstore catalog.
     *
     * @return the current book catalog
     */
    @GetMapping
    public List<BookDtos.BookResponse> list() {
        return service.findAll();
    }

    /**
     * Adds a new book to the bookstore inventory.
     *
     * <p>The service validates required fields, price, stock quantity and
     * ISBN uniqueness before persisting the book.</p>
     *
     * @param request book title, author, ISBN, price and stock quantity
     * @return the newly created book
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDtos.BookResponse> add(
            @Valid @RequestBody BookDtos.CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(request));
    }
}
