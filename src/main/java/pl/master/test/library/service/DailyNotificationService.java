package pl.master.test.library.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyNotificationService {
    private final BookRepository bookRepository;
    private final RegistrationService registrationService;
    private final ClientRepository clientRepository;

    @Value("${notification.batch.size}")
    private int batchSize;

    @Scheduled(cron = "${scheduled.cron.expression}")
    @Transactional
    public void sendDailyNotifications() {
        int pageNumber = 0;
        Page<Client> clientPage;

        do {
            Pageable pageable = PageRequest.of(pageNumber, batchSize);
            clientPage = clientRepository.findAllByEnabledTrueAndSubscriptionsNotEmpty(pageable);

            processClientsBatch(clientPage.getContent());

            pageNumber++;
        } while (clientPage.hasNext());
    }

    public void processClientsBatch(List<Client> clients) {
        for (Client client : clients) {
            List<Book> booksOfInterest = bookRepository.findAllByAuthorOrCategoryInAndCreatedDateAfter(
                    client.getSubscribedAuthors(),
                    client.getSubscribedCategories(),
                    LocalDateTime.now().minusDays(1));

            if (!booksOfInterest.isEmpty()) {
                String notificationMessage = generateNotificationMessage(booksOfInterest);
                registrationService.newLibraryPosition(client.getEmail(), notificationMessage);
            }
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
