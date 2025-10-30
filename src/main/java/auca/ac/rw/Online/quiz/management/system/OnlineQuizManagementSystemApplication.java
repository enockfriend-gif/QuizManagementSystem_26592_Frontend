package auca.ac.rw.Online.quiz.management.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "auca.ac.rw.Online.quiz.management")
@EntityScan(basePackages = "auca.ac.rw.Online.quiz.management.model")
@EnableJpaRepositories(basePackages = "auca.ac.rw.Online.quiz.management.repository")
public class OnlineQuizManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineQuizManagementSystemApplication.class, args);
	}

}
