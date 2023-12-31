package pl.master.test.library.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.dto.BookDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {


    @Transactional
    @Query("SELECT DISTINCT b.category FROM Book b")
    Set<String> findDistinctCategories();

    @Transactional
    @Query("SELECT DISTINCT b.author FROM Book b")
    Set<String> findDistinctAuthors();

    @Query("SELECT DISTINCT c.email FROM Client c WHERE (:author MEMBER OF c.subscribedAuthors OR :category MEMBER OF c.subscribedCategories) AND c.enabled = true")
    Set<String> findEmailsOfClientsSubscribedToAuthorOrCategory(@Param("author") String author, @Param("category") String category);

    @Query("SELECT NEW pl.master.test.library.model.dto.BookDto(b.id, b.author, b.title, b.category) FROM Book b")
    List<BookDto> findAllBooksAsDto();

    @Query("SELECT b FROM Book b WHERE b.createdDate > :date")
    Page<Book> findAllByCreatedDateAfter(LocalDateTime date, Pageable pageable);


    @Query("SELECT b FROM Book b WHERE " +
            "(b.author IN :authors OR b.category IN :categories) " +
            "AND b.createdDate > :date")
    List<Book> findAllByAuthorOrCategoryInAndCreatedDateAfter(
            @Param("authors") Set<String> subscribedAuthors,
            @Param("categories") Set<String> subscribedCategories,
            @Param("date") LocalDateTime date);


}
