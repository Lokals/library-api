package pl.master.test.library.service;

import pl.master.test.library.model.Book;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.command.UpdateBookCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.model.dto.ClientDto;

import java.util.List;
import java.util.Set;

public interface BookService {

    List<BookDto> findAll();

    BookDto findById(int id);

    ClientDto findClientByBookId(int bookId);

    BookDto save(CreateBookCommand command);

    BookDto updateBook(int id, UpdateBookCommand command);

    BookDto remove(int id);

    Set<String> getAllCategories();
    Set<String> getAllAuthors();

    Book getBookById(int id);
}
