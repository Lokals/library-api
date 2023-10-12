package pl.master.test.library.model.command;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pl.master.test.library.model.Client;

@Data
public class CreateClientCommand {

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String firstName;
    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String lastName;
    @NotNull
    @Email
    private String email;


    public Client toEntity(){
        return Client.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }

}
