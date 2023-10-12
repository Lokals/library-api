package pl.master.test.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.master.test.library.model.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Query(value = "SELECT * FROM users WHERE email = ?", nativeQuery = true)
    Optional<Client> findByEmail(String email);
}
