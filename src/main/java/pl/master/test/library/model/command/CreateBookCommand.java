package pl.master.test.library.model.command;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import pl.master.test.library.model.Book;

@Data
public class CreateBookCommand {

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String author;

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    private String title;
    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String category;

    public Book toEntity(){
        return Book.builder()
                .author(author)
                .title(title)
                .category(category)
                .build();
    }

}
