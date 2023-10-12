package pl.master.test.library.model.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateClientCommand {

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String firstName;
    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String lastName;
    @NotNull
    @Email
    private String email;
}
