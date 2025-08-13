import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateAdminHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Hash Length: " + hash.length());
        
        // Verify the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
    }
}