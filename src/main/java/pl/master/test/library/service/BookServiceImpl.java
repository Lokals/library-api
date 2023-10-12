package pl.master.test.library.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.master.test.library.configuration.RabbitMqConfiguration;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.command.UpdateBookCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.model.dto.ClientDto;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final RabbitTemplate rabbitTemplate;
    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(BookDto::fromEntity)
                .toList();
    }

    @Override
    public BookDto findById(int id) {
        Book book = getBookById(id);
        return BookDto.fromEntity(book);
    }

    @Override
    public ClientDto findClientByBookId(int bookId) {
        Book book = getBookById(bookId);
        Client client = clientRepository.findById(book.getClient().getId()).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format(
                        "Client with id={0} not found", book.getClient().getId()))
        );
        return ClientDto.fromEntity(client);
    }

    @Override
    @Transactional
    public BookDto save(CreateBookCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateBookCommand cannot be null");
        }
        Book book = command.toEntity();
        bookRepository.save(book);
        rabbitTemplate.convertAndSend(RabbitMqConfiguration.BOOK_CREATION_QUEUE, book);
        return BookDto.fromEntity(book);
    }

    @Override
    @Transactional
    public BookDto updateBook(int id, UpdateBookCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateBookCommand cannot be null");
        }
        Book book = getBookById(id);
        Client client = clientRepository.findById(command.getClientId()).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format(
                        "Client with id={0} not found", command.getClientId()
                ))
        );
        book.setClient(client);
        return BookDto.fromEntity(bookRepository.save(book));
    }

    @Override
    public BookDto remove(int id) {
        Book book = getBookById(id);
        if (book.getClient() != null && book.getClient().getBooks().contains(book)) {
            book.getClient().getBooks().remove(book);
        }
        bookRepository.delete(book);
        return BookDto.fromEntity(book);
    }

    @Override
    public Set<String> getAllCategories() {
        return bookRepository.findDistinctCategories();
    }

    @Override
    public Set<String> getAllAuthors() {
        return bookRepository.findDistinctAuthors();
    }

    @Override
    public Book getBookById(int id) {
        return bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat
                        .format("Book with id={0} not found", id))
        );
    }
}
