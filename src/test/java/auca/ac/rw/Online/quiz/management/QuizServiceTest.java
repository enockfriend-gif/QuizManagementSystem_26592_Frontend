package auca.ac.rw.Online.quiz.management;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.system.OnlineQuizManagementSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OnlineQuizManagementSystemApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class QuizServiceTest {

    @Test
    public void testQuizCreation() {
        Quiz quiz = new Quiz();
        quiz.setTitle("Test Quiz");
        assertNotNull(quiz);
        assertEquals("Test Quiz", quiz.getTitle());
    }

    @Test
    public void testQuizValidation() {
        Quiz quiz = new Quiz();
        quiz.setTitle("Valid Quiz");
        quiz.setDurationMinutes(60);
        assertTrue(quiz.getDurationMinutes() > 0);
    }
}