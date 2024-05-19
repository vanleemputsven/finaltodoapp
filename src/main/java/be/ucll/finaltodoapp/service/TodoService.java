package be.ucll.finaltodoapp.service;


import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserService userService;

    @Autowired
    public TodoService(TodoRepository todoRepository, UserService userService) {
        this.todoRepository = todoRepository;
        this.userService = userService;
    }

    public List<Todo> findAllTodosForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userService.findUserByEmail(username);
        return user.map(value -> todoRepository.findByUserId(value.getId())).orElseGet(List::of);
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
}