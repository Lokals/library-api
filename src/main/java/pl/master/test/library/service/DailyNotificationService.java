package pl.master.test.library.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyNotificationService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RegistrationService registrationService;


    @Value("${notification.batch.size}")
    private int batchSize;

    @Scheduled(cron = "${scheduled.cron.expression}")
    public void sendDailyNotifications() {
        int pageNumber = 0;
        Page<Book> bookPage;

        do {
            Pageable pageable = PageRequest.of(pageNumber, batchSize);
            bookPage = bookRepository.findAllByCreatedDateAfter(LocalDateTime.now().minusDays(1), pageable);

            processBooksBatch(bookPage.getContent());

            pageNumber++;
        } while (bookPage.hasNext());

    }

    private void processBooksBatch(List<Book> booksBatch) {
        Map<String, List<Book>> clientBooksMap = new HashMap<>();
        for (Book book : booksBatch) {
            Set<String> interestedClients = bookRepository.findEmailsOfClientsSubscribedToAuthorOrCategory(book.getAuthor(), book.getCategory());

            for (String email : interestedClients) {
                clientBooksMap.computeIfAbsent(email, k -> new ArrayList<>()).add(book);
            }
        }
        for (Map.Entry<String, List<Book>> entry : clientBooksMap.entrySet()) {

            String clientEmail = entry.getKey();
            List<Book> books = entry.getValue();

            String notificationMessage = generateNotificationMessage(books);
            registrationService.newLibraryPosition(clientEmail, notificationMessage);
        }
    }

    private String generateNotificationMessage(List<Book> books) {
        return books.stream()
                .map(book -> "Title: " + book.getTitle() + ", Author: " + book.getAuthor())
                .collect(Collectors.joining("\n", "New books available:\n", ""));
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
