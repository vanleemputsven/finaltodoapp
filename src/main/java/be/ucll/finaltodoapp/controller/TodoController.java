package be.ucll.finaltodoapp.controller;

import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoService todoService;
    private final JmsTemplate jmsTemplate;

    @Autowired
    public TodoController(TodoService todoService, JmsTemplate jmsTemplate) {
        this.todoService = todoService;
        this.jmsTemplate = jmsTemplate;
    }

    @GetMapping
    public List<Todo> getAllTodos() {
        return todoService.findAllTodosForCurrentUser();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable UUID id) {
        return todoService.findTodoByIdForCurrentUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        return todoService.saveTodoForCurrentUser(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable UUID id, @RequestBody Todo todoDetails) {
        return todoService.findTodoByIdForCurrentUser(id)
                .map(existingTodo -> {
                    existingTodo.setTitle(todoDetails.getTitle());
                    existingTodo.setComment(todoDetails.getComment());
                    existingTodo.setIsCompleted(todoDetails.getIsCompleted());
                    existingTodo.setExpiryDate(todoDetails.getExpiryDate());
                    return ResponseEntity.ok(todoService.saveTodoForCurrentUser(existingTodo));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID id) {
        return todoService.findTodoByIdForCurrentUser(id)
                .map(todo -> {
                    todoService.deleteTodoForCurrentUser(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/sendTodo")
    public ResponseEntity<String> sendTodoToQueue(
            @RequestParam String email,
            @RequestParam String title,
            @RequestParam String comment,
            @RequestParam String expiryDate) {
        String message = String.join(",", email, title, comment, expiryDate);
        jmsTemplate.convertAndSend("todo.queue", message);
        return ResponseEntity.ok("Message sent to the queue!");
    }
}