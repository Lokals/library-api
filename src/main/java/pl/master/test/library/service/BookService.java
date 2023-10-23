package pl.master.test.library.service;

import pl.master.test.library.model.Book;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.dto.BookDto;

import java.util.List;

public interface BookService {

    List<BookDto> findAll();

    BookDto findById(int id);

    BookDto save(CreateBookCommand command);

    Book getBookById(int id);
}
