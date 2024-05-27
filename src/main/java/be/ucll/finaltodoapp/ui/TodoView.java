package be.ucll.finaltodoapp.ui;

import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.service.TodoService;
import be.ucll.finaltodoapp.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CssImport("./styles/todo-view.css")
@Route("todos")
public class TodoView extends VerticalLayout {

    private final TodoService todoService;
    private final UserService userService;
    private final Grid<Todo> todoGrid = new Grid<>(Todo.class);
    private final TextField titleField = new TextField("Title");
    private final TextField commentField = new TextField("Comment (Optional)");
    private final Checkbox completedCheckbox = new Checkbox("Completed");
    private final DateTimePicker expiryDateTimePicker = new DateTimePicker("Expiry Date and Time");
    private final Button saveButton = new Button("Save");
    private final Dialog todoDialog = new Dialog();
    private final Binder<Todo> binder = new Binder<>(Todo.class);
    private final TextField searchField = new TextField();
    private UUID editingTodoId;

    public TodoView(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
        setSizeFull();
        configureGrid();
        configureForm();
        HorizontalLayout header = createHeader();
        add(header, todoGrid);
        updateList();
    }

    private void configureGrid() {
        todoGrid.setSizeFull();
        todoGrid.setColumns("title", "comment", "expiryDate");

        todoGrid.addColumn(new ComponentRenderer<>(todo -> {
            Icon icon;
            if (Boolean.TRUE.equals(todo.getIsCompleted())) {
                icon = new Icon(VaadinIcon.CHECK_CIRCLE);
                icon.setColor("green");
            } else {
                icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
                icon.setColor("red");
            }
            return icon;
        })).setHeader("Completed").setAutoWidth(true);

        todoGrid.addComponentColumn(this::createActions).setHeader("Actions");
        todoGrid.setItemDetailsRenderer(new ComponentRenderer<>(item -> {
            long daysUntilExpiry = LocalDate.now().until(
                    item.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    java.time.temporal.ChronoUnit.DAYS
            );
            return new Span(String.format("Expires in %d day%s",
                    daysUntilExpiry,
                    daysUntilExpiry == 1 ? "" : "s"
            ));
        }));

        todoGrid.setClassNameGenerator(item -> {
            LocalDate expiryDate = item.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (expiryDate.isBefore(LocalDate.now().plusDays(3))) {
                return "warning";
            }
            return null;
        });
    }

    private HorizontalLayout createActions(Todo item) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT), click -> {
            editingTodoId = item.getId();
            binder.readBean(item);
            todoDialog.open();
        });
        editButton.addClassName("action-button");
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
            todoService.deleteTodoForCurrentUser(item.getId());
            updateList();
            Notification.show("Todo deleted");
        });
        deleteButton.addClassName("action-button");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    private void configureForm() {
        HorizontalLayout formLayout = new HorizontalLayout(titleField, commentField, completedCheckbox, expiryDateTimePicker, saveButton);
        formLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        binder.forField(titleField)
                .withValidator(new StringLengthValidator("Title is required", 1, null))
                .bind(Todo::getTitle, Todo::setTitle);
        binder.forField(completedCheckbox)
                .bind(Todo::getIsCompleted, Todo::setIsCompleted);
        binder.forField(expiryDateTimePicker)
                .asRequired("Expiry date and time is required")
                .withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault()))
                .bind(Todo::getExpiryDate, Todo::setExpiryDate);
        binder.forField(commentField)
                .bind(Todo::getComment, Todo::setComment);
        todoDialog.add(formLayout);
        saveButton.addClickListener(e -> {
            if (binder.validate().isOk()) {
                Todo todo = new Todo();
                if (editingTodoId != null) {
                    todo.setId(editingTodoId);
                }
                binder.writeBeanIfValid(todo);
                todoService.saveTodoForCurrentUser(todo);
                updateList();
                todoDialog.close();
                editingTodoId = null;
            } else {
                Notification.show("Please fill all required fields", 3000, Notification.Position.BOTTOM_START);
            }
        });
    }

    private HorizontalLayout createHeader() {
        Span currentDateSpan = new Span("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        currentDateSpan.addClassName("current-date");

        Span userNameSpan;
        try {
            User user = userService.getCurrentUser();
            userNameSpan = new Span("Welcome, " + user.getFirstName() + "!");
            userNameSpan.addClassName("user-name");
        } catch (UsernameNotFoundException e) {
            userNameSpan = new Span("User: Unknown");
            userNameSpan.addClassName("user-name");
        }

        searchField.setPlaceholder("Search todos...");
        searchField.addClassName("search-field");
        searchField.addValueChangeListener(e -> updateList());

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/csv");
        upload.addClassName("upload");
        upload.addSucceededListener(event -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                MultipartFile multipartFile = new MockMultipartFile("file", event.getFileName(), "text/csv", bytes);
                String response = uploadCsvFile(multipartFile);
                Notification.show(response);
                updateList();
            } catch (IOException e) {
                Notification.show("Failed to read file: " + e.getMessage());
            }
        });

        Button addTodoButton = new Button("Add New Todo", new Icon(VaadinIcon.PLUS));
        addTodoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTodoButton.addClassName("add-todo-button");
        addTodoButton.addClickListener(click -> {
            editingTodoId = null;
            binder.readBean(new Todo());
            todoDialog.open();
        });

        Button logoutButton = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addClassName("logout-button");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addClickListener(click -> getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        HorizontalLayout leftSection = new HorizontalLayout(currentDateSpan, userNameSpan);
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);

        HorizontalLayout rightSection = new HorizontalLayout(searchField, upload, addTodoButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout(leftSection, rightSection);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        return toolbar;
    }

    private String uploadCsvFile(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String sessionId = VaadinSession.getCurrent().getSession().getId();
        headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + sessionId);

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", resource, MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, HttpEntity<?>> multipartRequest = builder.build();
        HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(multipartRequest, headers);

        String url = "http://localhost:8080/upload-csv";
        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    private static class MockMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public MockMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), content);
        }
    }

    private void updateList() {
        String searchTerm = searchField.getValue().trim();
        List<Todo> todos;
        if (searchTerm.isEmpty()) {
            todos = todoService.findAllTodosForCurrentUser().stream()
                    .sorted(Comparator.comparing(Todo::getExpiryDate))
                    .collect(Collectors.toList());
        } else {
            todos = todoService.findTodosByTitleContainingIgnoreCase(searchTerm).stream()
                    .sorted(Comparator.comparing(Todo::getExpiryDate))
                    .collect(Collectors.toList());
        }
        todoGrid.setItems(todos);
    }
}
