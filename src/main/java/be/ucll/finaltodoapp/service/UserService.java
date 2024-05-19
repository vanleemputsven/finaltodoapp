package be.ucll.finaltodoapp.service;

import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public User saveUser(User user) {
        logger.info("Saving user with email: " + user.getEmail());
        String originalPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("Original password: " + originalPassword);
        logger.info("Encoded password: " + user.getPassword());
        user.setEmail(user.getEmail().toLowerCase());
        User savedUser = userRepository.save(user);
        logger.info("User saved with ID: " + savedUser.getId());
        return savedUser;
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Loading user by email: " + email);
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> {
                    logger.warning("User not found with email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
        logger.info("User found: " + user.getEmail() + ", password: " + user.getPassword());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}