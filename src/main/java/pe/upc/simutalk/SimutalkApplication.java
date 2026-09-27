package pe.upc.simutalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SimutalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimutalkApplication.class, args);
    }
}
