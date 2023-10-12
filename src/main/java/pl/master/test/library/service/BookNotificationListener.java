package pl.master.test.library.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.master.test.library.configuration.RabbitMqConfiguration;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookNotificationListener {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RegistrationService registrationService;

    @RabbitListener(queues = RabbitMqConfiguration.BOOK_CREATION_QUEUE)
    public void handleBookCreation(Book book) {
        String position = "Title: "+ book.getTitle() + " Author: " + book.getAuthor();
        Set<Client> clientsToNotify = new HashSet<>();
        Set<Client> authorSubscribers = bookRepository.findClientsSubscribedToAuthor(book.getAuthor());
        Set<Client> categorySubscribers = bookRepository.findClientsSubscribedToCategory(book.getCategory());
        clientsToNotify.addAll(categorySubscribers);
        clientsToNotify.addAll(authorSubscribers);

        for (Client client : clientsToNotify) {
            if (client.isEnabled()) {
                registrationService.newLibraryPosition(client, position);
            }
        }

    }
}
