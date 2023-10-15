package pl.master.test.library.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.ConfirmationToken;
import pl.master.test.library.model.command.CreateClientCommand;
import pl.master.test.library.model.command.UpdateClientCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionAuthorCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionCategoryCommand;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.model.dto.ClientDto;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;
import pl.master.test.library.repository.ConfirmationTokenRepository;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final RegistrationService registrationService;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Override
    public List<ClientDto> findAll() {
        return clientRepository.findAllClientsAsDto();
    }

    @Override
    public ClientDto findById(int id) {
        Client client = getClientById(id);
        return ClientDto.fromEntity(client);
    }

    @Override
    public ClientDto save(CreateClientCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateClientCommand cannot be null");
        }
        Client client = command.toEntity();
        clientRepository.save(client);
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setClient(client);
        confirmationTokenRepository.save(confirmationToken);
        registrationService.confirmationEmail(client, confirmationToken.getToken());
        return ClientDto.fromEntity(client);
    }

    @Override
    public ClientDto updateClient(int id, UpdateClientCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateClientCommand cannot be null");
        }
        Client client = getClientById(id);
        client.setLastName(command.getLastName());
        client.setFirstName(command.getFirstName());
        client.setEmail(command.getEmail());
        return ClientDto.fromEntity(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientDto remove(int id) {
        Client client = getClientById(id);
        if (!client.getBooks().isEmpty()){
            bookRepository.disassociateBooksFromClient(id);
        }
        clientRepository.delete(client);
        return ClientDto.fromEntity(client);
    }

    @Override
    @Transactional
    public ClientDto subscribeToCategory(int clientId, UpdateClientSubscriptionCategoryCommand command) {
        Client client = getClientById(clientId);
        if (!client.isEnabled()){
            throw new IllegalArgumentException("Account is not enabled, please confirm by name");
        }
        if (client.getSubscribedCategories().contains(command.getSubscribedCategory())) {
            throw new IllegalArgumentException("Client do subscribe already that category");
        }
        client.getSubscribedCategories().add(command.getSubscribedCategory());
        registrationService.subscribeClientToCategory(client, command.getSubscribedCategory());
        return  ClientDto.fromEntity(clientRepository.save(client));
    }

    @Override
    public ClientDto unsubscribeFromCategory(int clientId, UpdateClientSubscriptionCategoryCommand command) {
        Client client = getClientById(clientId);
        if (!client.isEnabled()){
            throw new IllegalArgumentException("Account is not enabled, please confirm by name");
        }
        if (!client.getSubscribedCategories().contains(command.getSubscribedCategory())) {
            throw new IllegalArgumentException("Client do not subscribe that category");
        }
        client.getSubscribedCategories().remove(command.getSubscribedCategory());
        registrationService.unsubscribeClientToCategory(client, command.getSubscribedCategory());
        return  ClientDto.fromEntity(clientRepository.save(client));
    }

    @Override
    public ClientDto subscribeToAuthor(int clientId, UpdateClientSubscriptionAuthorCommand command) {
        Client client = getClientById(clientId);
        if (!client.isEnabled()){
            throw new IllegalArgumentException("Account is not enabled, please confirm by name");
        }
        if (client.getSubscribedAuthors().contains(command.getSubscribedAuthor())) {
            throw new IllegalArgumentException("Client do subscribe already that author");
        }
        client.getSubscribedAuthors().add(command.getSubscribedAuthor());
        registrationService.subscribeClientToAuthor(client, command.getSubscribedAuthor());
        return  ClientDto.fromEntity(clientRepository.save(client));
    }

    @Override
    public ClientDto unsubscribeFromAuthor(int clientId, UpdateClientSubscriptionAuthorCommand command) {
        Client client = getClientById(clientId);
        if (!client.isEnabled()){
            throw new IllegalArgumentException("Account is not enabled, please confirm by name");
        }
        if (!client.getSubscribedAuthors().contains(command.getSubscribedAuthor())) {
            throw new IllegalArgumentException("Client do not subscribe that category");
        }
        client.getSubscribedAuthors().remove(command.getSubscribedAuthor());
        registrationService.unsubscribeClientAuthor(client, command.getSubscribedAuthor());
        return  ClientDto.fromEntity(clientRepository.save(client));
    }

    @Override
    public Set<String> getSubscribedCategories(int clientId) {
        return clientRepository.findById(clientId)
                .map(Client::getSubscribedCategories)
                .orElseThrow(() -> new IllegalArgumentException("Client not found!"));
    }

    @Override
    public Set<String> getSubscribedAuthors(int clientId) {
        return clientRepository.findById(clientId)
                .map(Client::getSubscribedAuthors)
                .orElseThrow(() -> new IllegalArgumentException("Client not found!"));
    }

    @Override
    public Client getClientById(int clientId) {
        return clientRepository.findById(clientId).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format(
                        "Client with id={0} not found", clientId))
        );
    }

}
