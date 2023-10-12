package pl.master.test.library.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;

import java.util.List;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {


    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.client = NULL WHERE b.client.id = :clientId")
    void disassociateBooksFromClient(@Param("clientId") int clientId);

    @Transactional
    @Query("SELECT DISTINCT b.category FROM Book b")
    Set<String> findDistinctCategories();

    @Transactional
    @Query("SELECT DISTINCT b.author FROM Book b")
    Set<String> findDistinctAuthors();

    @Query("SELECT c FROM Client c WHERE :author MEMBER OF c.subscribedAuthors")
    Set<Client> findClientsSubscribedToAuthor(@Param("author") String author);

    @Query("SELECT c FROM Client c WHERE :category MEMBER OF c.subscribedCategories")
    Set<Client> findClientsSubscribedToCategory(@Param("category") String category);

}
