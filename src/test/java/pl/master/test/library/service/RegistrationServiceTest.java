package pl.master.test.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.master.test.library.model.Client;
import pl.master.test.library.properties.LibraryApiProperties;
import pl.master.test.library.repository.ClientRepository;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @InjectMocks
    private RegistrationService registrationService;

    @Mock
    private LibraryApiProperties libraryApiProperties;
    @Mock
    private EmailService emailService;

    private static Client client;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .firstName("Jehbleh")
                .email("test@example.com")
                .build();
//        when(libraryApiProperties.getBaseUrl()).thenReturn("http://localhost:8080/api/v1");

    }

    @Test
    void subscribeClientToCategory_ValidClient_EmailSent() {
        registrationService.subscribeClientToCategory(client, "Sci-Fi");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Subscription Successful - Category: Sci-Fi",
                "Hello Jehbleh, You have successfully subscribed to the category: Sci-Fi");
    }

    @Test
    void subscribeClientToAuthor_ValidClient_EmailSent() {
        registrationService.subscribeClientToAuthor(client, "Stephen King");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Subscription Successful - Author: Stephen King",
                "Hello Jehbleh, You have successfully subscribed to the author: Stephen King");
    }

    @Test
    void unsubscribeClientToCategory_ValidClient_EmailSent() {
        registrationService.unsubscribeClientToCategory(client, "Sci-Fi");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Subscription Removal Successful - Category: Sci-Fi",
                "Hello Jehbleh, You have successfully unsubscribed the category: Sci-Fi");
    }

    @Test
    void unsubscribeClientAuthor_ValidClient_EmailSent() {
        registrationService.unsubscribeClientAuthor(client, "Stephen King");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Subscription Removal Successful - Author: Stephen King",
                "Hello Jehbleh, You have successfully unsubscribed to the author: Stephen King");
    }

    @Test
    void newLibraryPosition_ValidClient_EmailSent() {
        registrationService.newLibraryPosition(client.getEmail(), "The Dark Tower");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Subscribed books appeared",
                "The Dark Tower");
    }

    @Test
    void confirmationEmail_ValidClient_EmailSent() {
        registrationService.confirmationEmail(client, "sample-token");

        verify(emailService, times(1)).sendSimpleMessage(client.getEmail(),
                "Confirmation mail",
                "Hello Jehbleh, To register and unblock your account for full access please confirm this mail by clicking in following link: null/confirm?token=sample-token");
    }
}