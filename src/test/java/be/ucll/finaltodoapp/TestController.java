package be.ucll.finaltodoapp;

import be.ucll.finaltodoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping("/test-load-user")
    public String testLoadUser(@RequestParam String email) {
        try {
            userService.loadUserByUsername(email);
            return "User loaded successfully";
        } catch (Exception e) {
            return "Failed to load user: " + e.getMessage();
        }
    }
}