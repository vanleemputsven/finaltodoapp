package be.ucll.finaltodoapp.endpoint;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addTodoResponse", namespace = "http://finaltodoapp.ucll.be/todos")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddTodoResponse {

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String status;

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}