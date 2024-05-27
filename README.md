# TodoApp

TodoApp is a web application for managing individual tasks. It features user registration, login, task management, CSV file uploads, SOAP web services, and ActiveMQ/JMS integration.

## Features

- User registration and login
- Task management (add, edit, delete, sort by expiry date)
- CSV file upload for tasks
- SOAP web services for task management
- ActiveMQ/JMS integration for task creation via messages

## Installation

### Requirements

- Java 17+
- Maven
- MySQL
- ActiveMQ

### Steps

1. Clone the repository:
    ```sh
    git clone https://github.com/vanleemputsven/finaltodoapp.git
    cd finaltodoapp
    ```
2. Set up MySQL and configure `application.properties`.
3. Build the project:
    ```sh
    mvn clean install
    ```
4. Run the application:
    ```sh
    mvn spring-boot:run
    ```
5. Open `http://localhost:8080` in your browser.

## Usage

- Register and log in.
- Manage tasks (add, edit, delete).
- Upload CSV files for tasks.
- Use SoapUI for SOAP web services.
- Configure ActiveMQ and send messages to the queue.
