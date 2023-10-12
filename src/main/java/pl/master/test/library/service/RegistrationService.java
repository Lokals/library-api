package pl.master.test.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.ClientRepository;

import java.text.MessageFormat;

@Service
public class RegistrationService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmailService emailService;

    public void subscribeClientToCategory(Client client, String category) {
        String messageText = MessageFormat.format("Hello {0}, You have successfully subscribed to the category: {1}", client.getFirstName(), category);
        emailService.sendSimpleMessage(client.getEmail(), "Subscription Successful - Category: " + category, messageText);
    }
    public void subscribeClientToAuthor(Client client, String author) {
        String messageText = MessageFormat.format("Hello {0}, You have successfully subscribed to the author: {1}", client.getFirstName(), author);
        emailService.sendSimpleMessage(client.getEmail(), "Subscription Successful - Author: " + author, messageText);
    }
    public void unsubscribeClientToCategory(Client client, String category) {
        String messageText = MessageFormat.format("Hello {0}, You have successfully unsubscribed the category: {1}", client.getFirstName(), category);
        emailService.sendSimpleMessage(client.getEmail(), "Subscription Removal Successful - Category: " + category, messageText);
    }

    public void unsubscribeClientAuthor(Client client, String author) {
        String messageText = MessageFormat.format("Hello {0}, You have successfully unsubscribed to the author: {1}", client.getFirstName(), author);
        emailService.sendSimpleMessage(client.getEmail(), "Subscription Removal Successful - Author: " + author, messageText);
    }

    public void newLibraryPosition(Client client, String position){
        String messageText = MessageFormat.format("Hello {0}, New subscribed position appeared in our library: {1}", client.getFirstName(), position);
        emailService.sendSimpleMessage(client.getEmail(), "New position appeared in library - Book: " + position, messageText);
    }

    public void confirmationEmail(Client client, String token){
        String messageText = MessageFormat.format(
                "Hello {0}, To register and unblock your account " +
                "for full access please confirm " +
                "this mail by clicking in following link: " +
                "http://localhost:8080/api/v1/confirm?token={1}", client.getFirstName(), token);

        emailService.sendSimpleMessage(client.getEmail(), "Confirmation mail", messageText);
    }
}
