import java.io.Serializable;

public class User implements Serializable {
    public String firstName;
    public String lastName;
    public String username;

    public User(String firstName, String lastName, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    @Override
    public String toString() {
        return firstName + "|" + lastName + "|" + username;
    }
}
