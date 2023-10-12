package pl.master.test.library;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;


    public DatabaseLoader(BookRepository bookRepository, ClientRepository clientRepository){
        this.bookRepository = bookRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Book bookHarryPotterI = new Book();
        Book bookHarryPotterII = new Book();
        Book bookHarryPotterIII = new Book();
        Book bookLostI = new Book();
        Book bookLostII = new Book();
        Book bookLostIII = new Book();

        Client clientMarcin = new  Client();
        Client clientWojtek = new  Client();
        Client clientKamil = new  Client();
        Client clientJacek = new  Client();

        bookHarryPotterI.setAuthor("Rowling");
        bookHarryPotterI.setCategory("Fantasy");
        bookHarryPotterI.setTitle("Harry Potter I");

        bookHarryPotterII.setAuthor("Rowling");
        bookHarryPotterII.setCategory("Fantasy");
        bookHarryPotterII.setTitle("Harry Potter II");

        bookHarryPotterIII.setAuthor("Rowling");
        bookHarryPotterIII.setCategory("Fantasy");
        bookHarryPotterIII.setTitle("Harry Potter III");

        bookLostI.setAuthor("Miner");
        bookLostI.setCategory("Fantasy");
        bookLostI.setTitle("Lost I");

        bookLostII.setAuthor("Miner");
        bookLostII.setCategory("Thriller");
        bookLostII.setTitle("Lost II");

        bookLostIII.setAuthor("Miner");
        bookLostIII.setCategory("Thriller");
        bookLostIII.setTitle("Lost III");

        clientMarcin.setEmail("fakif81736@elixirsd.com");
        clientMarcin.setFirstName("Marcin");
        clientMarcin.setLastName("Performer");
        clientMarcin.setEnabled(true);


        clientWojtek.setEmail("fakif81736@elixirsd.com");
        clientWojtek.setFirstName("Wojtek");
        clientWojtek.setLastName("Greater");
        clientWojtek.setEnabled(true);


        clientKamil.setEmail("fakif81736@elixirsd.com");
        clientKamil.setFirstName("Kamil");
        clientKamil.setLastName("Walecznik");
        clientKamil.setEnabled(true);

        clientJacek.setEmail("fakif81736@elixirsd.com");
        clientJacek.setFirstName("Jacek");
        clientJacek.setLastName("Spartanin");

        bookRepository.save(bookHarryPotterI);
        bookRepository.save(bookHarryPotterII);
        bookRepository.save(bookHarryPotterIII);
        bookRepository.save(bookLostI);
        bookRepository.save(bookLostII);
        bookRepository.save(bookLostIII);

        clientRepository.save(clientWojtek);
        clientRepository.save(clientKamil);
        clientRepository.save(clientMarcin);
        clientRepository.save(clientJacek);
    }
}
