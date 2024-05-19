package be.ucll.finaltodoapp.ui;

import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.service.TodoService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@CssImport("./styles/todo-view.css")
@Route("todos")
public class TodoView extends VerticalLayout {

    private final TodoService todoService;
    private final Grid<Todo> todoGrid = new Grid<>(Todo.class);

    private final TextField titleField = new TextField("Title");
    private final TextField commentField = new TextField("Comment (Optional)");
    private final Checkbox completedCheckbox = new Checkbox("Completed");
    private final DateTimePicker expiryDateTimePicker = new DateTimePicker("Expiry Date and Time");
    private final Button saveButton = new Button("Save");
    private final Dialog todoDialog = new Dialog();
    private final Binder<Todo> binder = new Binder<>(Todo.class);
    private UUID editingTodoId;

    public TodoView(TodoService todoService) {
        this.todoService = todoService;
        setSizeFull();
        configureGrid();
        configureForm();
        VerticalLayout toolbar = getToolbar();
        add(toolbar, todoGrid);
        updateList();
    }

    private void configureGrid() {
        todoGrid.setSizeFull();
        todoGrid.setColumns("title", "comment", "isCompleted", "expiryDate");
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
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
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
                Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private VerticalLayout getToolbar() {
        Button addTodoButton = new Button("Add New Todo", click -> {
            editingTodoId = null;
            binder.readBean(new Todo());
            todoDialog.open();
        });
        addTodoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Span currentDateSpan = new Span("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        currentDateSpan.addClassNames("current-date");

        HorizontalLayout toolbar = new HorizontalLayout(currentDateSpan, addTodoButton);
        toolbar.addClassNames("toolbar");
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        toolbar.setSpacing(true);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return new VerticalLayout(toolbar);
    }

    private void updateList() {
        todoGrid.setItems(todoService.findAllTodosForCurrentUser());
    }
}
