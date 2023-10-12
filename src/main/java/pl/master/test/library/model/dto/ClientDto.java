package pl.master.test.library.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.master.test.library.model.Client;

@Data
@Builder
public class ClientDto {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;


    public static ClientDto fromEntity(Client client){
        return ClientDto.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .enabled(client.isEnabled())
                .build();
    }
}
