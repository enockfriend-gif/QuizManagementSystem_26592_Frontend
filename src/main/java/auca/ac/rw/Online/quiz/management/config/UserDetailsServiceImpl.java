package auca.ac.rw.Online.quiz.management.config;

import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailIgnoreCase(usernameOrEmail);
        }

        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String role = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role))
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}

