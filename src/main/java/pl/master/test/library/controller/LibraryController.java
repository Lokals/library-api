package pl.master.test.library.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.master.test.library.model.ConfirmationToken;
import pl.master.test.library.model.command.*;
import pl.master.test.library.model.dto.BookDto;
import pl.master.test.library.model.dto.ClientDto;
import pl.master.test.library.repository.ConfirmationTokenRepository;
import pl.master.test.library.service.BookService;
import pl.master.test.library.service.ClientService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LibraryController {

    private final BookService bookService;
    private final ClientService clientService;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @GetMapping("/books")
    public List<BookDto> findAllBooks(){
        return bookService.findAll();
    }

    @GetMapping("/books/{id}")
    public BookDto findBookById(@PathVariable int id){
        return bookService.findById(id);
    }

    // TODO: 08/10/2023 Nie dziala bo nie ustawiles debilu klienta
    @GetMapping("/books/{id}/client")
    public ClientDto findClientByBookId(@PathVariable int id){
        return bookService.findClientByBookId(id);
    }


    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto saveBook(@RequestBody @Valid CreateBookCommand command) {
        return bookService.save(command);
    }


    @GetMapping("/clients")
    public List<ClientDto> findAllClients(){
        return clientService.findAll();
    }

    @GetMapping("/clients/{id}")
    public ClientDto findClientById(@PathVariable int id){
        return clientService.findById(id);
    }

    @PostMapping("/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto saveClient(@RequestBody @Valid CreateClientCommand command) {
        return clientService.save(command);
    }

    @PostMapping("/clients/{clientId}/subscriptions/categories")
    public ClientDto subscribeToCategory(@PathVariable int clientId, @RequestBody @Valid UpdateClientSubscriptionCategoryCommand command) {
        return clientService.subscribeToCategory(clientId, command);
    }

    @DeleteMapping("/clients/{clientId}/subscriptions/categories")
    public ClientDto unsubscribeFromCategory(@PathVariable int clientId, @RequestBody @Valid UpdateClientSubscriptionCategoryCommand command) {
        return clientService.unsubscribeFromCategory(clientId, command);
    }

    @PostMapping("/clients/{clientId}/subscriptions/authors")
    public ClientDto subscribeToAuthor(@PathVariable int clientId, @RequestBody @Valid UpdateClientSubscriptionAuthorCommand command) {
        return clientService.subscribeToAuthor(clientId, command);
    }

    @DeleteMapping("/clients/{clientId}/subscriptions/authors")
    public ClientDto unsubscribeFromAuthor(@PathVariable int clientId, @RequestBody @Valid UpdateClientSubscriptionAuthorCommand command) {
        return clientService.unsubscribeFromAuthor(clientId, command);

    }

    @GetMapping("/clients/{clientId}/subscriptions/categories")
    public ResponseEntity<Set<String>> getSubscribedCategories(@PathVariable int clientId) {
        Set<String> categories = clientService.getSubscribedCategories(clientId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/clients/{clientId}/subscriptions/authors")
    public ResponseEntity<Set<String>> getSubscribedAuthors(@PathVariable int clientId) {
        Set<String> authors = clientService.getSubscribedAuthors(clientId);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/confirm")
    public ResponseEntity confirm(@RequestParam("token") String token) {
        Optional<ConfirmationToken> optToken = confirmationTokenRepository.findByToken(token);
        if (optToken.isPresent()) {
            ConfirmationToken confirmationToken = optToken.get();
            confirmationToken.getClient().setEnabled(true);
            confirmationTokenRepository.delete(confirmationToken);
            return ResponseEntity.ok("Email confirmed!");
        } else {
            return ResponseEntity.badRequest().body("Invalid token.");
        }
    }
}
