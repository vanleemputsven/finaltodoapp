package be.ucll.finaltodoapp.service;


import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.repository.TodoRepository;
import be.ucll.finaltodoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserService userService;
    private final UserRepository userRepository;


    @Autowired
    public TodoService(TodoRepository todoRepository, UserService userService, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public List<Todo> findAllTodosForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userService.findUserByEmail(username);
        if (user.isPresent()) {
            return todoRepository.findByUserId(user.get().getId());
        }
        return List.of();
    }

    public Optional<Todo> findTodoByIdForCurrentUser(UUID id) {
        return findAllTodosForCurrentUser().stream().filter(todo -> todo.getId().equals(id)).findFirst();
    }

    public Todo saveTodoForCurrentUser(Todo todo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    public void deleteTodoForCurrentUser(UUID id) {
        findTodoByIdForCurrentUser(id).ifPresent(todoRepository::delete);
    }

    public List<Todo> findTodosByTitleContainingIgnoreCase(String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userService.findUserByEmail(username);
        if (user.isPresent()) {
            return todoRepository.findByUserIdAndTitleContainingIgnoreCase(user.get().getId(), title);
        }
        return List.of();
    }

    public void addTodoForUser(String email, String title, String comment, String expiryDateStr) throws ParseException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Todo todo = new Todo();
        todo.setUser(user);
        todo.setTitle(title);
        todo.setComment(comment);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date expiryDate = formatter.parse(expiryDateStr);
        todo.setExpiryDate(expiryDate);

        todoRepository.save(todo);
    }
}