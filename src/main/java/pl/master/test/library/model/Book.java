package pl.master.test.library.model;

import jakarta.persistence.*;
import lombok.*;
import pl.master.test.library.model.dto.ClientDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Book {
//    autora, tytuł, kategorie

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String author;
    private String title;
    private String category;

    @ManyToOne
    private Client client;
}
