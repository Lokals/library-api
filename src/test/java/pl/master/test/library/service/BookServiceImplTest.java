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
import pl.master.test.library.configuration.RabbitMqConfiguration;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.command.UpdateBookCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.model.dto.ClientDto;
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
                .books(new HashSet<>())
                .build();

        book.setClient(client);
        bookService = new BookServiceImpl(bookRepository, clientRepository, rabbitTemplate);

    }

    @Test
    void findAll_ResultsInBookDtoListBeingReturned() {
        List<Book> booksFromRepo = List.of(book);
        when(bookRepository.findAll()).thenReturn(booksFromRepo);

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
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), any(Book.class));

        BookDto savedBookDto = bookService.save(command);
        verify(bookRepository, times(1)).save(any(Book.class));
        assertEquals(book.getAuthor(), savedBookDto.getAuthor());
    }

    @Test
    void updateBook_ValidUpdateBookCommand_BookUpdatedAndReturned() {
        int bookId = 1;
        UpdateBookCommand command = new UpdateBookCommand();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(clientRepository.findById(anyInt())).thenReturn(Optional.of(client));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto updatedBookDto = bookService.updateBook(bookId, command);

        assertEquals(book.getId(), updatedBookDto.getId());
        assertEquals(book.getAuthor(), updatedBookDto.getAuthor());
    }

    @Test
    void findClientByBookId_ValidBookId_ReturnsClientDto() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

        ClientDto result = bookService.findClientByBookId(book.getId());

        assertEquals(client.getId(), result.getId());
        assertEquals(client.getEmail(), result.getEmail());
        assertEquals(client.getFirstName(), result.getFirstName());
        assertEquals(client.getLastName(), result.getLastName());
    }

    @Test
    void findClientByBookId_InvalidBookId_ThrowsEntityNotFoundException() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.findClientByBookId(book.getId()));
    }

    @Test
    void findClientByBookId_ValidBookIdButNoClient_ThrowsEntityNotFoundException() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        assertThrows(EntityNotFoundException.class, () -> bookService.findClientByBookId(book.getId()));
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

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfiguration.BOOK_CREATION_QUEUE), bookCaptor.capture());
        Book sentBook = bookCaptor.getValue();
        assertNotNull(sentBook);
        assertEquals(book.getAuthor(), sentBook.getAuthor());
    }

    @Test
    void updateBook_ValidCommand_BookUpdatedAndReturned() {
        UpdateBookCommand command = new UpdateBookCommand();
        command.setClientId(1);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto updatedBookDto = bookService.updateBook(1, command);

        assertEquals(book.getId(), updatedBookDto.getId());
        assertEquals(book.getAuthor(), updatedBookDto.getAuthor());
        assertEquals(client.getId(), updatedBookDto.getId());
    }
    @Test
    void updateBook_NullCommand_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(1, null));
    }

    @Test
    void updateBook_InvalidBookId_ThrowsEntityNotFoundException() {
        UpdateBookCommand command = new UpdateBookCommand();
        command.setClientId(1);
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.updateBook(1, command));
    }

    @Test
    void updateBook_InvalidClientId_ThrowsEntityNotFoundException() {
        UpdateBookCommand command = new UpdateBookCommand();
        command.setClientId(1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(clientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.updateBook(1, command));
    }

    @Test
    void remove_ValidBookId_BookRemovedAndReturned() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        BookDto removedBookDto = bookService.remove(1);

        assertEquals(book.getId(), removedBookDto.getId());
        verify(bookRepository).delete(book);
    }

    @Test
    void remove_BookIdDoesNotExist_ThrowsEntityNotFoundException() {
        when(bookRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.remove(2));
    }

    @Test
    void remove_BookWithClientAssociation_BookRemovedFromClientAndDeleted() {
        book.setClient(client);
        client.setBooks(new HashSet<>(List.of(book)));

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        BookDto removedBookDto = bookService.remove(1);

        assertEquals(book.getId(), removedBookDto.getId());
        assertFalse(client.getBooks().contains(book));
        verify(bookRepository).delete(book);
    }

    @Test
    void remove_BookWithoutClient_NoAssociationToRemove() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        BookDto removedBookDto = bookService.remove(1);

        assertEquals(book.getId(), removedBookDto.getId());
        verify(bookRepository).delete(book);
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