package pl.master.test.library.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.master.test.library.model.Book;

@Data
@Builder
public class BookDto {

    private int id;
    private String author;
    private String title;
    private String category;

    public static BookDto fromEntity(Book book){

        return BookDto.builder()
                .id(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .category(book.getCategory())
                .build();
    }
}
