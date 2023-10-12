package pl.master.test.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookNotificationListenerTest {


    @InjectMocks
    private BookNotificationListener listener;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RegistrationService registrationService;

    private static Book book;
    private static Client client1;
    private static Client client2;

    @BeforeEach
    void setUp() {

        book = Book.builder()
                .author("Rowling")
                .title("Potter")
                .category("Fantasy")
                .build();

        client1 = new Client();
        client1.setEnabled(true);

        client2 = new Client();
        client2.setEnabled(false);
    }

    @Test
    void handleBookCreation_ClientsSubscribedToAuthorAndCategory_ClientsNotified() {
        Set<Client> clientsSubscribedToAuthor = new HashSet<>();
        clientsSubscribedToAuthor.add(client1);

        Set<Client> clientsSubscribedToCategory = new HashSet<>();
        clientsSubscribedToCategory.add(client2);

        when(bookRepository.findClientsSubscribedToAuthor(book.getAuthor())).thenReturn(clientsSubscribedToAuthor);
        when(bookRepository.findClientsSubscribedToCategory(book.getCategory())).thenReturn(clientsSubscribedToCategory);

        listener.handleBookCreation(book);

        verify(registrationService).newLibraryPosition(client1, "Title: Potter Author: Rowling");
        verify(registrationService, never()).newLibraryPosition(client2, "Title: Potter Author: Rowling");
    }

    @Test
    void handleBookCreation_NoClientsSubscribed_NoNotification() {
        when(bookRepository.findClientsSubscribedToAuthor(book.getAuthor())).thenReturn(new HashSet<>());
        when(bookRepository.findClientsSubscribedToCategory(book.getCategory())).thenReturn(new HashSet<>());

        listener.handleBookCreation(book);

        verify(registrationService, never()).newLibraryPosition(any(Client.class), anyString());
    }
}