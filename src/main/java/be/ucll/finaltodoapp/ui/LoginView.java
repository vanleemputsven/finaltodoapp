package be.ucll.finaltodoapp.ui;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.logging.Logger;

@Route("login")
@PageTitle("Login | Todo")
@CssImport("./styles/todo-view.css")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private static final Logger logger = Logger.getLogger(LoginView.class.getName());
    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();

        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setAction("login");

        loginForm.setI18n(createLoginI18n());


        VerticalLayout loginFormContainer = new VerticalLayout();
        loginFormContainer.addClassName("login-form-container");
        loginFormContainer.setSizeUndefined();
        loginFormContainer.setAlignItems(Alignment.CENTER);

        Anchor registerLink = new Anchor("register", "No account yet? Register here.");
        registerLink.addClassName("login-link");

        loginFormContainer.add(new H1("TodoApp Login"), loginForm, registerLink);
        add(loginFormContainer);
    }

    private LoginI18n createLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setUsername("E-mail");
        i18n.getForm().setTitle("Log in");
        i18n.getForm().setSubmit("Log in");
        i18n.getForm().setPassword("Password");
        i18n.getErrorMessage().setTitle("Unable to log in");
        i18n.getErrorMessage().setMessage("Check that you have entered your email and password correctly.");
        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
            logger.warning("Login attempt failed.");
        }
    }
}