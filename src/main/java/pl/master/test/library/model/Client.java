package pl.master.test.library.model;

        import jakarta.persistence.*;
        import lombok.*;

        import java.util.HashSet;
        import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled = false;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Book> books;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "client_subscription_category",
            joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "category_name")
    private Set<String> subscribedCategories;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "client_subscription_author",
            joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "author_name")
    private Set<String> subscribedAuthors;
}
