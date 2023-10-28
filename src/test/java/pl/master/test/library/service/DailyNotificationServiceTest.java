package pl.master.test.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyNotificationServiceTest {
    @InjectMocks
    private DailyNotificationService dailyNotificationService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        dailyNotificationService.setBatchSize(10);
    }

    @Test
    void sendDailyNotifications_NoSubscribedClients() {
        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList());
        when(clientRepository.findAllByEnabledTrueAndSubscriptionsNotEmpty(any())).thenReturn(emptyPage);

        dailyNotificationService.sendDailyNotifications();

        verify(clientRepository, atMostOnce()).findAllByEnabledTrueAndSubscriptionsNotEmpty(any());
        verifyNoMoreInteractions(bookRepository, registrationService);
    }

    @Test
    void sendDailyNotifications_SubscribedClients_NoNewBooks() {
        Client client = new Client();
        client.setSubscribedAuthors(Collections.singleton("Author"));
        client.setSubscribedCategories(Collections.singleton("Category"));
        when(clientRepository.findAllByEnabledTrueAndSubscriptionsNotEmpty(any())).thenReturn(new PageImpl<>(List.of(client)));
        when(bookRepository.findAllByAuthorOrCategoryInAndCreatedDateAfter(anySet(), anySet(), any())).thenReturn(Collections.emptyList());

        dailyNotificationService.sendDailyNotifications();

        verify(registrationService, never()).newLibraryPosition(anyString(), anyString());
    }

    @Test
    void sendDailyNotifications_SubscribedClients_WithNewBooks() {
        Client client = new Client();
        client.setEmail("email@example.com");
        client.setSubscribedAuthors(Collections.singleton("Author"));
        client.setSubscribedCategories(Collections.singleton("Category"));
        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setCategory("Category");
        when(clientRepository.findAllByEnabledTrueAndSubscriptionsNotEmpty(any())).thenReturn(new PageImpl<>(List.of(client)));
        when(bookRepository.findAllByAuthorOrCategoryInAndCreatedDateAfter(anySet(), anySet(), any())).thenReturn(List.of(book));

        dailyNotificationService.sendDailyNotifications();

        verify(registrationService, times(1)).newLibraryPosition(anyString(), anyString());
        verify(clientRepository, times(1)).findAllByEnabledTrueAndSubscriptionsNotEmpty(any());
        verify(bookRepository, times(1)).findAllByAuthorOrCategoryInAndCreatedDateAfter(anySet(), anySet(), any());
    }

    @Test
    public void testTransactionalBehaviorInScheduledMethod() {
        Client client = new Client();
        client.setEmail("test@example.com");
        client.setFirstName("Test");
        client.setLastName("Test");
        client.setEnabled(true);
        client.setSubscribedAuthors(new HashSet<>(Collections.singleton("Test")));
        client.setSubscribedCategories(new HashSet<>(Collections.singleton("Test")));

        Book book = new Book();
        book.setTitle("Test");
        book.setAuthor("Test");
        book.setCategory("Test");

        when(clientRepository.findAllByEnabledTrueAndSubscriptionsNotEmpty(any()))
                .thenReturn(new PageImpl<>(List.of(client)));
        when(bookRepository.findAllByAuthorOrCategoryInAndCreatedDateAfter(anySet(), anySet(), any()))
                .thenReturn(List.of(book));

        doThrow(new RuntimeException("Forced Exception"))
                .when(registrationService).newLibraryPosition(anyString(), anyString());

        assertThrows(RuntimeException.class, () -> dailyNotificationService.sendDailyNotifications());
    }
}