package be.ucll.finaltodoapp.endpoint;


import be.ucll.finaltodoapp.service.TodoService;
import be.ucll.finaltodoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.text.ParseException;

@Endpoint
public class TodoEndpoint {

    private static final String NAMESPACE_URI = "http://finaltodoapp.ucll.be/todos";

    private final TodoService todoService;
    private final UserService userService;

    @Autowired
    public TodoEndpoint(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addTodoRequest")
    @ResponsePayload
    public AddTodoResponse addTodo(@RequestPayload AddTodoRequest request) throws ParseException {
        AddTodoResponse response = new AddTodoResponse();
        boolean userExists = userService.findUserByEmail(request.getEmail()).isPresent();

        if (userExists) {
            todoService.addTodoForUser(request.getEmail(), request.getTitle(), request.getComment(), request.getExpiryDate());
            response.setStatus("SUCCESS");
        } else {
            response.setStatus("USER_NOT_FOUND");
        }

        return response;
    }
}