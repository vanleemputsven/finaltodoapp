package be.ucll.finaltodoapp.ui;

import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;


@Route("register")
@PageTitle("Register | Todo")
@CssImport("./styles/todo-view.css")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserService userService) {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout formContainer = new VerticalLayout();
        formContainer.addClassName("login-form-container");
        formContainer.setWidth("auto");
        formContainer.setAlignItems(Alignment.CENTER);
        formContainer.getStyle().set("padding", "30px 55px");

        H1 header = new H1("Todo Register");

        TextField firstName = new TextField("First Name");
        firstName.setWidth("300px");
        TextField lastName = new TextField("Last Name");
        lastName.setWidth("300px");
        EmailField email = new EmailField("Email");
        email.setWidth("300px");
        PasswordField password = new PasswordField("Password");
        password.setWidth("300px");

        Button registerButton = new Button("Register");
        registerButton.setWidth("300px");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> {
            if (validateInput(firstName, lastName, email, password)) {
                User newUser = new User(email.getValue().trim().toLowerCase(), firstName.getValue(), lastName.getValue(), password.getValue());
                userService.saveUser(newUser);
                Notification.show("User registered successfully!");
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                Notification.show("Please fill all fields correctly.");
            }
        });

        Anchor loginLink = new Anchor("login", "Already have an account? Log in here.");
        loginLink.addClassName("login-link");

        formContainer.add(header, firstName, lastName, email, password, registerButton, loginLink);
        add(formContainer);
    }

    private boolean validateInput(TextField firstName, TextField lastName, EmailField email, PasswordField password) {
        return !firstName.isEmpty() && !lastName.isEmpty() && !email.isInvalid() && !password.isEmpty();
    }
}