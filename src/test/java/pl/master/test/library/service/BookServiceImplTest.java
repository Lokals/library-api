package pl.master.test.library.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    private static Book book;
    private static Client client;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1)
                .author("Rowling")
                .title("Potter")
                .category("Fantasy")
                .build();

        client = Client.builder()
                .id(1)
                .email("temp@mail.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .build();

        bookService = new BookServiceImpl(bookRepository);

    }

    @Test
    void findAll_ResultsInBookDtoListBeingReturned() {

        BookDto bookDto = BookDto.builder()
                .title(book.getTitle())
                .category(book.getCategory())
                .author(book.getAuthor())
                .id(book.getId())
                .build();
        List<BookDto> booksFromRepo = List.of(bookDto);
        when(bookRepository.findAllBooksAsDto()).thenReturn(booksFromRepo);

        List<BookDto> booksDtoReturned = bookService.findAll();

        assertEquals(booksFromRepo.size(), booksDtoReturned.size());
        assertEquals(book.getId(), booksDtoReturned.get(0).getId());
        assertEquals(book.getAuthor(), booksDtoReturned.get(0).getAuthor());
    }

    @Test
    void findById_BookFound_ResultsBookDtoReturned() {
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDto bookDtoReturned = bookService.findById(bookId);

        assertEquals(book.getId(), bookDtoReturned.getId());
        assertEquals(book.getAuthor(), bookDtoReturned.getAuthor());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void save_ValidCreateBookCommand_BookSavedAndReturned() {
        CreateBookCommand command = new CreateBookCommand();
        command.setAuthor("Rowling");
        command.setTitle("Harry Potter");
        command.setCategory("Fantasy");

        Book createdBook = new Book();
        createdBook.setAuthor(command.getAuthor());
        createdBook.setTitle(command.getTitle());
        createdBook.setCategory(command.getCategory());

        when(bookRepository.save(any(Book.class))).thenReturn(createdBook);

        BookDto savedBookDto = bookService.save(command);

        verify(bookRepository, times(1)).save(any(Book.class)); // Ensure repository's save method was called.
        assertEquals(createdBook.getAuthor(), savedBookDto.getAuthor());
    }

    @Test
    void save_NullCommand_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookService.save(null));
    }

    @Test
    void save_ValidCommand_BookSavedAndReturned() {
        CreateBookCommand command = new CreateBookCommand();
        command.setAuthor("Rowling");
        command.setTitle("Potter");
        command.setCategory("Fantasy");
        command.toEntity();

        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto savedBookDto = bookService.save(command);


        assertEquals(book.getAuthor(), savedBookDto.getAuthor());
        assertEquals(book.getTitle(), savedBookDto.getTitle());
        assertEquals(book.getCategory(), savedBookDto.getCategory());
    }

    @Test
    void getBookById_BookFound_ReturnsBook() {
        int bookId = 1;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Book returnedBook = bookService.getBookById(bookId);

        assertEquals(book.getId(), returnedBook.getId());
        assertEquals(book.getAuthor(), returnedBook.getAuthor());
        assertEquals(book.getTitle(), returnedBook.getTitle());
        assertEquals(book.getCategory(), returnedBook.getCategory());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void getBookById_BookNotFound_ThrowsEntityNotFoundException() {
        int nonExistentBookId = 100;

        when(bookRepository.findById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(nonExistentBookId));
        verify(bookRepository).findById(nonExistentBookId);
    }


}