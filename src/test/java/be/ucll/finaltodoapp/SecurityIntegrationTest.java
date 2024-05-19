package be.ucll.finaltodoapp;

import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        String email = "test@example.com";
        String encodedPassword = new BCryptPasswordEncoder().encode("password");
        User user = new User(email, "First", "Last", encodedPassword);
        userRepository.save(user);
    }

    @Test
    public void testLoginWithValidUser() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "test@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testLoginWithInvalidUser() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "invalid@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }
}