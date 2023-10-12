package pl.master.test.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.master.test.library.model.Book;
import pl.master.test.library.model.Client;
import pl.master.test.library.model.command.CreateBookCommand;
import pl.master.test.library.model.command.CreateClientCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionAuthorCommand;
import pl.master.test.library.model.command.UpdateClientSubscriptionCategoryCommand;
import pl.master.test.library.repository.BookRepository;
import pl.master.test.library.repository.ClientRepository;
import pl.master.test.library.repository.ConfirmationTokenRepository;
import pl.master.test.library.service.ClientService;
import pl.master.test.library.service.EmailService;

import java.math.BigDecimal;
import java.util.*;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @MockBean
    private EmailService emailService;

    private Book book;
    private Client client;

    @BeforeEach
    void init() {
        Mockito.doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());


        client = clientRepository.save(
                Client.builder()
                        .id(1)
                        .firstName("Wojtek")
                        .lastName("Twardziel")
                        .email("nagrzany@client.com")
                        .enabled(true)
                        .build()
        );

        book = bookRepository.save(

                Book.builder()
                        .id(1)
                        .title("Fajne")
                        .author("Takie")
                        .category("Duze")
                        .build()
        );
        book.setClient(client);

    }

    @AfterEach
    void tearDown() {
        confirmationTokenRepository.deleteAll();
        bookRepository.deleteAll();
        clientRepository.deleteAll();

    }

    @Test
    void testFindAllBooks_ResultsInAllClientsBeingReturned() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Fajne"))
                .andExpect(jsonPath("$[0].author").value("Takie"))
                .andExpect(jsonPath("$[0].category").value("Duze"));
    }

    @Test
    void testFindBookById_ResultsInAllClientsBeingReturned() throws Exception {
        mockMvc.perform(get("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Fajne"))
                .andExpect(jsonPath("$.author").value("Takie"))
                .andExpect(jsonPath("$.category").value("Duze"));
    }

    @Test
    void testFindBookById_NoBookFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/books/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindClientByBookId_BookHasClient_ResultsInClientBeingReturned() throws Exception {
        book.setClient(client);
        bookRepository.save(book);


        mockMvc.perform(get("/api/v1/books/" + book.getId() + "/client")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Wojtek"))
                .andExpect(jsonPath("$.lastName").value("Twardziel"))
                .andExpect(jsonPath("$.email").value("nagrzany@client.com"));
    }

    @Test
    void testFindClientByBookId_BookDoNotExist_ResultsIn404NotFound() throws Exception {

        mockMvc.perform(get("/api/v1/books/100/client")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveClient_ValidInput_ResultsInClientBeingCreated() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("Killer");
        command.setLastName("Siarra");
        command.setEmail("siarra.killer@aneks.com");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Killer"))
                .andExpect(jsonPath("$.lastName").value("Siarra"))
                .andExpect(jsonPath("$.email").value("siarra.killer@aneks.com"));
    }

    @Test
    void testSaveClient_NullEmail_ResultsInBadRequest() throws Exception {

        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("Test");
        command.setLastName("Test");
        command.setEmail(null);

        String requestBody = new ObjectMapper().writeValueAsString(command);


        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testFindAllClients_ResultsInAllClientsBeingReturned() throws Exception {

        Client client1 = Client.builder()
                .firstName("Janko")
                .lastName("Daraka")
                .email("dupa.sraka@test.com")
                .build();
        clientRepository.save(client1);

        Client client2 = Client.builder()
                .firstName("Janczko")
                .lastName("Darczaka")
                .email("dupa.sraka@test.com")
                .build();
        clientRepository.save(client2);

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].firstName").value(client.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(client.getLastName()))
                .andExpect(jsonPath("$[0].email").value((client.getEmail())))
                .andExpect(jsonPath("$[1].firstName").value(client1.getFirstName()))
                .andExpect(jsonPath("$[1].lastName").value(client1.getLastName()))
                .andExpect(jsonPath("$[1].email").value((client1.getEmail())))
                .andExpect(jsonPath("$[2].firstName").value(client2.getFirstName()))
                .andExpect(jsonPath("$[2].lastName").value(client2.getLastName()))
                .andExpect(jsonPath("$[2].email").value(client2.getEmail()));
    }

    @Test
    void testFindAllClients_NoClients_ResultsInEmptyList() throws Exception {

        clientRepository.deleteAll();

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFindAllClients_ResponseStructureIsAsExpected() throws Exception {

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].someOtherField").doesNotExist());
    }

    @Test
    void testSaveBook_ValidInput_ResultsInBookBeingCreated() throws Exception {
        CreateBookCommand command = new CreateBookCommand();
        command.setAuthor("Rowling");
        command.setTitle("Harry-Potter");
        command.setCategory("Fantasy");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(command)))
                .andDo(print()) 
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").value("Rowling"))
                .andExpect(jsonPath("$.title").value("Harry-Potter"))
                .andExpect(jsonPath("$.category").value("Fantasy"));
    }

    @Test
    void testFindClientById_ValidId_ResultsInClientBeingReturned() throws Exception {
        int clientId = client.getId();

        mockMvc.perform(get("/api/v1/clients/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(client.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(client.getLastName()))
                .andExpect(jsonPath("$.email").value(client.getEmail()));
    }

    @Test
    void testFindClientById_NonExistentId_ResultsInNotFound() throws Exception {
        int invalidId = 100;

        mockMvc.perform(get("/api/v1/clients/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindClientById_ResponseStructureIsAsExpected() throws Exception {
        int clientId = client.getId();

        mockMvc.perform(get("/api/v1/clients/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.someOtherField").doesNotExist());
    }

    @Test
    void testSaveClient_InvalidFirstName_ResultsInBadRequest() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("jonas");
        command.setLastName("Daraka");
        command.setEmail("dupa.sraka@test.com");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testSaveClient_InvalidLastName_ResultsInBadRequest() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("Janko");
        command.setLastName("drake");
        command.setEmail("dupa.sraka@test.com");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("lastName"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testSaveClient_InvalidEmail_ResultsInBadRequest() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("Janko");
        command.setLastName("Daraka");
        command.setEmail("lipo.mailo");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("email"))
                .andExpect(jsonPath("$.violations[0].message").value("@Email"));
    }

    @Test
    void testSaveClient_NullFirstName_ResultsInBadRequest() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName(null);
        command.setLastName("Daraka");
        command.setEmail("sraka@test.com");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testSaveClient_NullLastName_ResultsInBadRequest() throws Exception {
        CreateClientCommand command = new CreateClientCommand();
        command.setFirstName("Janko");
        command.setLastName(null);
        command.setEmail("dupa.sraka@test.com");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("lastName"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testSubscribeToCategory_ValidSubscription_ResultsInSuccessfulSubscription() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Horror");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testSubscribeToCategory_NullCategory_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory(null);

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedCategory"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testSubscribeToCategory_InvalidCategory_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Invalid_category1");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedCategory"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testUnsubscribeFromCategory_ValidUnsubscription_ResultsInSuccessfulUnsubscription() throws Exception {
        int clientId = 1;
        client.setSubscribedCategories(Collections.singleton("Horror"));
        clientRepository.save(client);
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Horror");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

    }

    @Test
    void testUnsubscribeFromCategory_NullCategory_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory(null);

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedCategory"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testUnsubscribeFromCategory_InvalidCategory_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionCategoryCommand command = new UpdateClientSubscriptionCategoryCommand();
        command.setSubscribedCategory("Invalid_category1");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedCategory"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testSubscribeToAuthor_ValidInput_ResultsInAuthorSubscription() throws Exception {

        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testSubscribeToAuthor_InvalidAuthorName_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("rowling");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedAuthor"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testSubscribeToAuthor_NullAuthorName_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor(null);

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(post("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedAuthor"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testUnsubscribeFromAuthor_ValidInput_ResultsInAuthorUnsubscription() throws Exception {
        int clientId = 1;
        client.setSubscribedAuthors(Collections.singleton("Rowling"));
        clientRepository.save(client);
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("Rowling");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribedAuthors[0]").doesNotExist());
    }

    @Test
    void testUnsubscribeFromAuthor_InvalidAuthorName_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor("rowling");

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedAuthor"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    void testUnsubscribeFromAuthor_NullAuthorName_ResultsInBadRequest() throws Exception {
        int clientId = 1;
        UpdateClientSubscriptionAuthorCommand command = new UpdateClientSubscriptionAuthorCommand();
        command.setSubscribedAuthor(null);

        String requestBody = new ObjectMapper().writeValueAsString(command);

        mockMvc.perform(delete("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("subscribedAuthor"))
                .andExpect(jsonPath("$.violations[0].message").value("NULL_VALUE"));
    }

    @Test
    void testGetSubscribedCategories_ValidClientId_ReturnsListOfCategories() throws Exception {
        int clientId = 1;
        Set<String> mockCategories = new HashSet<>(Arrays.asList("Fiction", "Science"));
        client.setSubscribedCategories(mockCategories);
        clientRepository.save(client);

        mockMvc.perform(get("/api/v1/clients/" + clientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", isIn(mockCategories)))
                .andExpect(jsonPath("$[1]", isIn(mockCategories)));
    }

    @Test
    void testGetSubscribedCategories_InvalidClientId_ReturnsEmptyList() throws Exception {
        int invalidClientId = 9999;
        mockMvc.perform(get("/api/v1/clients/" + invalidClientId + "/subscriptions/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSubscribedAuthors_ValidClientId_ReturnsListOfAuthors() throws Exception {
        int clientId = 1;
        Set<String> mockAuthors = new HashSet<>(Arrays.asList("Rowling", "Shakespeare"));
        client.setSubscribedAuthors(mockAuthors);
        clientRepository.save(client);

        mockMvc.perform(get("/api/v1/clients/" + clientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", isIn(mockAuthors)))
                .andExpect(jsonPath("$[1]", isIn(mockAuthors)));
    }

    @Test
    void testGetSubscribedAuthors_InvalidClientId_ReturnsEmptyList() throws Exception {
        int invalidClientId = 9999;
        mockMvc.perform(get("/api/v1/clients/" + invalidClientId + "/subscriptions/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


}