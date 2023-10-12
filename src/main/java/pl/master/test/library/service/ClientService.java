package pl.master.test.library.service;

import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateClientCommand;
import pl.master.test.library.model.command.UpdateClientCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionAuthorCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionCategoryCommand;
import pl.master.test.library.model.dto.ClientDto;

import java.util.List;
import java.util.Set;

public interface ClientService {

    List<ClientDto> findAll();

    ClientDto findById(int id);

    ClientDto save(CreateClientCommand command);

    ClientDto updateClient(int id, UpdateClientCommand command);
    ClientDto remove(int id);

    ClientDto subscribeToCategory(int clientId, UpdateClientSubscriptionCategoryCommand command);

    ClientDto unsubscribeFromCategory(int clientId, UpdateClientSubscriptionCategoryCommand command);

    ClientDto subscribeToAuthor(int clientId, UpdateClientSubscriptionAuthorCommand command);

    ClientDto unsubscribeFromAuthor(int clientId, UpdateClientSubscriptionAuthorCommand command);

    Set<String> getSubscribedCategories(int clientId);

    Set<String> getSubscribedAuthors(int clientId);

    Client getClientById(int clientId);

}
