package auth_service.revakh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RevakhApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevakhApplication.class, args);
	}

}
