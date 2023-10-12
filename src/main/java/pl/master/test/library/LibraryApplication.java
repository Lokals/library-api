package pl.master.test.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
public class LibraryApplication {


	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

}
//Mamy w systemie jakiś klientow, kazdy klient ma imie, nazwisko, email
//Mamy jakies ksiazki, kazda ksiazka ma autora, tytuł, kategorie.
//
//Klient moze subskrybuować ksiażki przez kategorie i/lub autora.
//co to znaczy?
//to znaczy ze jak pracownik doda jakiś horror, a klient subskrybuje horror to dostanie maila odnosnie tego ze zostal dodany nowy horror.
//albo jak subskrybuje Tolkiena, to jak pojawi sie nowa ksiazka tolkiena to tez dostanie emaila.
//
//Stworz api do:
//- rejestrowania klientow z potwierdzeniem adresu email.
//- dodawanie ksiazki
//- tworzenie/anulowanie subksyrbcji ksiazek.