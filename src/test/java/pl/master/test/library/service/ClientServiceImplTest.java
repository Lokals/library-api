package pl.master.test.library.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateClientCommand;
import pl.master.test.library.model.command.UpdateClientCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionAuthorCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionCategoryCommand;
import pl.master.test.library.model.dto.ClientDto;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;
import pl.master.test.library.repository.ConfirmationTokenRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @InjectMocks
    private ClientServiceImpl clientService;

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RegistrationService registrationService;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    private BookRepository bookRepository;

    private static Client client;
    private static Book book;


    @BeforeEach
    void setUp() {

        Set<Book> books = new HashSet<>();
        books.add(book);
        book = Book.builder()
                .category("Fantasy")
                .title("Potter")
                .id(1)
                .author("Rowling")
                .build();

        client = Client.builder()
                .id(1)
                .email("temp@mail.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .subscribedAuthors(new HashSet<>())
                .subscribedCategories(new HashSet<>())
                .build();
    }

    @Test
    void findAll_ResultsInClientDtoListBeingReturned() {
        ClientDto clientDto = ClientDto.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .enabled(client.isEnabled())
                .build();
        List<ClientDto> clientsDtoFromRepo = List.of(clientDto);

        when(clientRepository.findAllClientsAsDto()).thenReturn(clientsDtoFromRepo);

        List<ClientDto> clientsDtoReturned = clientService.findAll();

        assertEquals(clientsDtoFromRepo.size(), clientsDtoReturned.size());
        assertEquals(clientDto.getId(), clientsDtoReturned.get(0).getId());
        assertEquals(clientDto.getEmail(), clientsDtoReturned.get(0).getEmail());
        assertEquals(clientDto.getFirstName(), clientsDtoReturned.get(0).getFirstName());
        assertEquals(clientDto.getLastName(), clientsDtoReturned.get(0).getLastName());
    }

    @Test
    void findById_ClientFound_ResultsClientDtoReturned() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDto clientDtoReturned = clientService.findById(clientId);

        assertEquals(client.getId(), clientDtoReturned.getId());
        assertEquals(client.getFirstName(), clientDtoReturned.getFirstName());
        assertEquals(client.getLastName(), clientDtoReturned.getLastName());
        assertEquals(client.getEmail(), clientDtoReturned.getEmail());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void save_CreateClient_CommandIsNull_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> clientService.save(null));
    }

    @Test
    void save_CreateClient_CommandIsValid_ReturnsClientDto() {
        CreateClientCommand command = new CreateClientCommand();
        when(clientRepository.save(any())).thenReturn(client);

        ClientDto clientDto = clientService.save(command);

        assertNotNull(clientDto);
        verify(confirmationTokenRepository).save(any());
        verify(registrationService).confirmationEmail(any(), any());
    }


    @Test
    void updateClient_ClientFound_UpdatesAndReturnsClientDto() {
        int clientId = 1;
        UpdateClientCommand command = new UpdateClientCommand();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);

        ClientDto updatedClient = clientService.updateClient(clientId, command);

        assertNotNull(updatedClient);
        assertEquals(command.getFirstName(), updatedClient.getFirstName());
        assertEquals(command.getLastName(), updatedClient.getLastName());
        assertEquals(command.getEmail(), updatedClient.getEmail());
    }

    @Test
    void subscribeToCategory_AccountNotEnabled_ThrowsIllegalArgumentException() {

        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.subscribeToCategory(clientId, command));
    }


    @Test
    void subscribeToCategory_ClientAlreadySubscribed_ThrowsIllegalArgumentException() {

        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(true);
        client.getSubscribedCategories().add("Fantasy");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));


        assertThrows(IllegalArgumentException.class, () -> clientService.subscribeToCategory(clientId, command));
    }

    @Test
    void subscribeToCategory_ValidClientIdAndCategory_ClientSuccessfullySubscribed() {

        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(true);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);


        clientService.subscribeToCategory(clientId, command);

        assertTrue(clientService.getSubscribedCategories(clientId).contains("Fantasy"));
    }

    @Test
    void unsubscribeFromCategory_ClientDoesNotExist_ThrowsEntityNotFoundException() {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.unsubscribeFromCategory(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromCategory_ClientNotEnabled_ThrowsIllegalArgumentException() {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(false);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.unsubscribeFromCategory(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromCategory_ClientNotSubscribedToCategory_ThrowsIllegalArgumentException() {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(true);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.unsubscribeFromCategory(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromCategory_SuccessfulUnsubscription() {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Fantasy");
        client.setEnabled(true);
        client.getSubscribedCategories().add("Fantasy");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientDto returnedDto = clientService.unsubscribeFromCategory(clientId, command);

        assertFalse(client.getSubscribedCategories().contains("Fantasy"));
        assertEquals(client.getId(), returnedDto.getId());
        assertEquals(client.getFirstName(), returnedDto.getFirstName());
        assertEquals(client.getLastName(), returnedDto.getLastName());
        assertEquals(client.getEmail(), returnedDto.getEmail());
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
    }

    @Test
    void subscribeToAuthor_ClientDoesNotExist_ThrowsEntityNotFoundException() {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.subscribeToAuthor(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void subscribeToAuthor_ClientNotEnabled_ThrowsIllegalArgumentException() {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");
        client.setEnabled(false);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.subscribeToAuthor(clientId, command));
        verify(clientRepository).findById(clientId);
    }


    @Test
    void subscribeToAuthor_SuccessfulSubscription() {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("J.K. Rowling");
        client.setEnabled(true);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientDto returnedDto = clientService.subscribeToAuthor(clientId, command);

        assertTrue(client.getSubscribedAuthors().contains("J.K. Rowling"));
        assertEquals(client.getId(), returnedDto.getId());
        assertEquals(client.getFirstName(), returnedDto.getFirstName());
        assertEquals(client.getLastName(), returnedDto.getLastName());
        assertEquals(client.getEmail(), returnedDto.getEmail());
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
    }



    @Test
    void subscribeToAuthor_ClientAlreadySubscribedToAuthor_ThrowsIllegalArgumentException() {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");
        client.setEnabled(true);
        client.getSubscribedAuthors().add("Rowling");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));


        assertThrows(IllegalArgumentException.class, () -> clientService.subscribeToAuthor(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromAuthor_ClientNotEnabled_ThrowsIllegalArgumentException() {
        int clientId = 1;
        client.setEnabled(false);
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.unsubscribeFromAuthor(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromAuthor_ClientNotSubscribedToAuthor_ThrowsIllegalArgumentException() {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> clientService.unsubscribeFromAuthor(clientId, command));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void unsubscribeFromAuthor_SuccessfulUnsubscription() {
        int clientId = 1;
        client.getSubscribedAuthors().add("Rowling");
        client.setEnabled(true);
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientDto result = clientService.unsubscribeFromAuthor(clientId, command);
        assertNotNull(result);
        assertFalse(client.getSubscribedAuthors().contains("Rowling"));
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
        verify(registrationService).unsubscribeClientAuthor(client, "Rowling");
    }

    @Test
    void getSubscribedCategories_ClientNotFound_ThrowsIllegalArgumentException() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clientService.getSubscribedCategories(clientId));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getSubscribedCategories_ClientHasNoSubscribedCategories_ReturnsEmptyList() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Set<String> categories = clientService.getSubscribedCategories(clientId);
        assertTrue(categories.isEmpty());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getSubscribedCategories_ClientHasSubscribedCategories_ReturnsCategories() {
        int clientId = 1;
        client.getSubscribedCategories().add("Horror");
        client.getSubscribedCategories().add("Fantasy");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Set<String> categories = clientService.getSubscribedCategories(clientId);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Horror"));
        assertTrue(categories.contains("Fantasy"));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getSubscribedAuthors_ClientNotFound_ThrowsIllegalArgumentException() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clientService.getSubscribedAuthors(clientId));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getSubscribedAuthors_ClientHasNoSubscribedAuthors_ReturnsEmptyList() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Set<String> authors = clientService.getSubscribedAuthors(clientId);
        assertTrue(authors.isEmpty());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getSubscribedAuthors_ClientHasSubscribedAuthors_ReturnsAuthors() {
        int clientId = 1;
        client.getSubscribedAuthors().add("Rowling");
        client.getSubscribedAuthors().add("Tolkien");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Set<String> authors = clientService.getSubscribedAuthors(clientId);
        assertEquals(2, authors.size());
        assertTrue(authors.contains("Rowling"));
        assertTrue(authors.contains("Tolkien"));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getClientById_ClientDoesNotExist_ThrowsEntityNotFoundException() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.getClientById(clientId));
        verify(clientRepository).findById(clientId);
    }

    @Test
    void getClientById_ClientExists_ReturnsClient() {
        int clientId = 1;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Client foundClient = clientService.getClientById(clientId);

        assertNotNull(foundClient);
        assertEquals(client.getId(), foundClient.getId());
        verify(clientRepository).findById(clientId);
    }






}