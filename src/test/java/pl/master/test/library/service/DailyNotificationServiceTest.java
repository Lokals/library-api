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
import pl.master.test.library.repository.BookRepository;

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
    private RegistrationService registrationService;


    @BeforeEach
    void setUp(){
        dailyNotificationService.setBatchSize(10);
    }

    @Test
    void sendDailyNotifications_NoNewBooks() {
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookRepository.findAllByCreatedDateAfter(any(LocalDateTime.class), any())).thenReturn(emptyPage);

        dailyNotificationService.sendDailyNotifications();

        verify(bookRepository, atMostOnce()).findAllByCreatedDateAfter(any(), any());
        verifyNoMoreInteractions(bookRepository, registrationService);
    }

    @Test
    void sendDailyNotifications_NewBooksAvailable() {
        Book book = new Book();
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book));
        when(bookRepository.findAllByCreatedDateAfter(any(LocalDateTime.class), any())).thenReturn(bookPage);
        when(bookRepository.findEmailsOfClientsSubscribedToAuthorOrCategory(any(), any())).thenReturn(new HashSet<>());

        dailyNotificationService.sendDailyNotifications();

        verify(bookRepository, atMostOnce()).findAllByCreatedDateAfter(any(), any());
        verify(bookRepository, atMostOnce()).findEmailsOfClientsSubscribedToAuthorOrCategory(any(), any());
        verify(registrationService, never()).newLibraryPosition(any(), any());
    }

    @Test
    void sendDailyNotifications_NewBooksButNoInterestedClients() {
        Book book = new Book();
        book.setAuthor("Author");
        book.setCategory("Category");

        when(bookRepository.findAllByCreatedDateAfter(any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.findEmailsOfClientsSubscribedToAuthorOrCategory(anyString(), anyString()))
                .thenReturn(new HashSet<>());

        dailyNotificationService.sendDailyNotifications();

        verify(registrationService, never()).newLibraryPosition(anyString(), anyString());
    }

    @Test
    void sendDailyNotifications_NewBooksAndInterestedClients() {
        Book book = new Book();
        book.setAuthor("Author");
        book.setCategory("Category");

        when(bookRepository.findAllByCreatedDateAfter(any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.findEmailsOfClientsSubscribedToAuthorOrCategory(anyString(), anyString()))
                .thenReturn(new HashSet<>(List.of("test@test.com")));

        dailyNotificationService.sendDailyNotifications();

        verify(registrationService, times(1)).newLibraryPosition(eq("test@test.com"), anyString());
    }


}