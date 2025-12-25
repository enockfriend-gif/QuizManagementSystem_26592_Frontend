// Quick test for email validation
public class EmailTest {
    public static void main(String[] args) {
        String email = "friendeno123@gmail.com";
        String pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        boolean matches = email.matches(pattern);
        System.out.println("Email: " + email);
        System.out.println("Pattern: " + pattern);
        System.out.println("Matches: " + matches);
    }
}