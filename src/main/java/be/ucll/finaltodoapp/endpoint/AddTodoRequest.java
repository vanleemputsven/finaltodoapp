package be.ucll.finaltodoapp.endpoint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addTodoRequest", namespace = "http://finaltodoapp.ucll.be/todos")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddTodoRequest {

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String email;

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String title;

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String comment;

    @XmlElement(namespace = "http://finaltodoapp.ucll.be/todos")
    private String expiryDate;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
