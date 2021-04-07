package taskmanager;

import java.time.LocalDate;

public class Task {
    int userId;
    String taskTitle;
    String taskDescription;
    LocalDate dueDate;

    public Task(int userId, String taskTitle, String taskDescription, LocalDate dueDate) {
        this.userId = userId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
    }

    public int getUserId() {
        return userId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
