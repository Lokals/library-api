package pl.master.test.library.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.repository.BookRepository;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAllBooksAsDto();
    }

    @Override
    public BookDto findById(int id) {
        Book book = getBookById(id);
        return BookDto.fromEntity(book);
    }

    @Override
    @Transactional
    public BookDto save(CreateBookCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateBookCommand cannot be null");
        }
        Book book = command.toEntity();
        bookRepository.save(book);
        return BookDto.fromEntity(book);
    }

    @Override
    public Book getBookById(int id) {
        return bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat
                        .format("Book with id={0} not found", id))
        );
    }
}
