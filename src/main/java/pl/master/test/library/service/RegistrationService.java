package pl.master.test.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.master.test.library.model.Client;
import pl.master.test.library.properties.LibraryApiProperties;
import pl.master.test.library.repository.ClientRepository;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final LibraryApiProperties libraryApiProperties;
    private final EmailService emailService;


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

    public void newLibraryPosition(String emailClient, String messageText) {
        emailService.sendSimpleMessage(emailClient, "Subscribed books appeared", messageText);
    }

    public void confirmationEmail(Client client, String token) {
        String messageText = MessageFormat.format(
                "Hello {0}, To register and unblock your account " +
                        "for full access please confirm " +
                        "this mail by clicking in following link: " +
                        "{1}/confirm?token={2}", client.getFirstName(), libraryApiProperties.getBaseUrl(), token);

        emailService.sendSimpleMessage(client.getEmail(), "Confirmation mail", messageText);
    }
}
