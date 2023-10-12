package pl.master.test.library.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    @ManyToOne
    @JoinColumn(nullable = false, name = "client_id")
    private Client client;
    @PrePersist
    protected void onCreate() {
        this.token = UUID.randomUUID().toString();
    }
}
