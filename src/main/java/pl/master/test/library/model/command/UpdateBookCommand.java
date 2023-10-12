package pl.master.test.library.model.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateBookCommand {

    @NotNull(message = "NULL_VALUE")
    @Positive(message = "MIN_VALUE_1")
    private int clientId;
}
