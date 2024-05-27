package be.ucll.finaltodoapp.service;

import be.ucll.finaltodoapp.entity.Todo;
import be.ucll.finaltodoapp.entity.User;
import be.ucll.finaltodoapp.repository.TodoRepository;
import be.ucll.finaltodoapp.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CsvService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    @Autowired
    public CsvService(UserRepository userRepository, TodoRepository todoRepository) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
    }

    public void processCsvFile(MultipartFile file, String username) throws IOException, ParseException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());

            for (CSVRecord csvRecord : csvParser) {
                String email = csvRecord.get("email");
                String title = csvRecord.get("title");
                String comment = csvRecord.get("comment");
                boolean isCompleted = Boolean.parseBoolean(csvRecord.get("isCompleted"));
                String expiryDateStr = csvRecord.get("expiryDate");

                if (email.equals(username)) {
                    Todo todo = new Todo();
                    todo.setUser(user);
                    todo.setTitle(title);
                    todo.setComment(comment);
                    todo.setIsCompleted(isCompleted);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date expiryDate = formatter.parse(expiryDateStr);
                    todo.setExpiryDate(expiryDate);

                    todoRepository.save(todo);
                }
            }
        }
    }
}