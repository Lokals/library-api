package pl.master.test.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.master.test.library.model.ConfirmationToken;

import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Integer> {

    @Query("SELECT ct FROM ConfirmationToken ct WHERE ct.token = ?1")
    Optional<ConfirmationToken> findByToken(String token);

}
