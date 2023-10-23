package pl.master.test.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.dto.ClientDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Query(value = "SELECT * FROM users WHERE email = ?", nativeQuery = true)
    Optional<Client> findByEmail(String email);

    @Query("SELECT NEW pl.master.test.library.model.dto.ClientDto(c.id, c.firstName, c.lastName, c.email, c.enabled) FROM Client c")
    List<ClientDto> findAllClientsAsDto();
}
