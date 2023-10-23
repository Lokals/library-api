package pl.master.test.library.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.api")
@Getter
@Setter
public class LibraryApiProperties {

    private String baseUrl;

}
