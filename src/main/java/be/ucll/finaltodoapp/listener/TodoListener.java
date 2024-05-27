package be.ucll.finaltodoapp.listener;

import be.ucll.finaltodoapp.service.TodoService;
import be.ucll.finaltodoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class TodoListener {

    private final TodoService todoService;
    private final UserService userService;

    @Autowired
    public TodoListener(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @JmsListener(destination = "todo.queue")
    public void receiveMessage(String message) throws ParseException {
        String[] parts = message.split(",");
        if (parts.length == 4) {
            String email = parts[0];
            String title = parts[1];
            String comment = parts[2];
            String expiryDateStr = parts[3];
            todoService.addTodoForUser(email, title, comment, expiryDateStr);
        }
    }
}