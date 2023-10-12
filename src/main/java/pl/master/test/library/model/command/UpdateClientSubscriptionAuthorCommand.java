package pl.master.test.library.model.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateClientSubscriptionAuthorCommand {
    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String subscribedAuthor;
}
