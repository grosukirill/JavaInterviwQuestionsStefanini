package taskmanager;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;

public class TaskManager {
    public static void main(String[] args) throws SQLException, IllegalArgumentException {
        String url = "jdbc:postgresql://localhost/postgres";
        String user = "postgres";
        String passw = "postgres";
        Connection conn = DriverManager.getConnection(url, user, passw);
        if (args.length == 0) {
            throw new IllegalArgumentException("Please specify one of the following entry args: -createUser, -showAllUsers, -addTask, -showTasks, -dueUntil, -missingTasks");
        }
        validateArgs(args);
        switch (args[0]) {
            case "-createUser":
                if (args.length < 2 || args.length > 5) {
                    throw new IllegalArgumentException("Expected args [firstName, lastName, userName] after: " + args[0]);
                }
                createUser(args, conn);
                break;
            case "-showAllUsers":
                if (args.length > 1) {
                    throw new IllegalArgumentException("No args expected after: " + args[0]);
                }
                showAllUsers(conn);
                break;
            case "-addTask":
                if (args.length < 2 || args.length > 5) {
                    throw new IllegalArgumentException("Expected args [userName, taskTitle, taskDescription, dueDate] after: " + args[0]);
                }
                if (args.length == 4) {
                    addTask(args, conn);
                } else {
                    addTaskWithDueDate(args, conn);
                }
                break;
            case "-showTasks":
                if (args.length != 2) {
                    throw new IllegalArgumentException("-showTask argument must be followed by -un= being username");
                }
                showTasksByUser(args, conn);
                break;
            case "-dueUntil":
                if (args.length > 3) {
                    throw new IllegalArgumentException("Expected only [userName, dueDate] args after " + args[0]);
                }
                dueUntil(args, conn);
                break;
            case "-missingTasks":
                if (args.length > 2) {
                    throw new IllegalArgumentException("Expected only [userName] arg after: " + args[0]);
                }
                missingTasks(args, conn);
                break;
            default:
                throw new IllegalArgumentException("Please specify one of the following entry args: -createUser, -showAllUsers, -addTask, -showTasks, -dueUntil, -missingTasks");
        }
    }

    private static void validateArgs(String[] args) {
        if (args.length > 5) {
            throw new IllegalArgumentException("Too many arguments are passed. If you are using Windows, try using  double quotes (\") for arguments instead of single quote (')");
        }
        if (args[0].equals("-createUser")) {
            if (args.length == 1 || !(args[1].startsWith("-fn=")) || !(args[2].startsWith("-ln=")) || !(args[3].startsWith("-un="))) {
                throw new IllegalArgumentException("-createUser argument must be followed by 3 arguments: -fn=  -ln=  -un=  being first name, last name, username of the user!");
            }
        } else if (args[0].equals("-addTask")) {
            if (args.length < 2 || args.length >= 6) {
                throw new IllegalArgumentException("Expected args [userName, taskTitle, taskDescription, dueDate] after: " + args[0]);
            }
            if (!(args[1].startsWith("-un=")) || !(args[2].startsWith("-tt=")) || !(args[3].startsWith("-td="))) {
                throw new IllegalArgumentException("-addTask argument must be followed by 3 arguments: -un=  -tt=  -td=  or  -dd being username assigned to, task title, task description, due date!");
            }
        } else if (args[0].equals("-showTasks")) {
            if (args.length != 2) {
                throw new IllegalArgumentException("-showTask argument must be followed by -un= being username");
            }
            if (!(args[1].startsWith("-un="))) {
                throw new IllegalArgumentException("-showTask must be followed by -un= argument being username that desired to view the tasks of!");
            }
        } else if (args[0].equals("-dueUntil")) {
            if (args.length != 3 || !(args[1].startsWith("-un=")) || !(args[2].startsWith("-dd="))) {
                throw new IllegalArgumentException("-dueUntil must be followed 2 arguments: -ud= and -dd= being username and due date");
            }
        } else if (args[0].equals("-missingTasks")) {
            if (args.length != 2 || !(args[1].startsWith("-un="))) {
                throw new IllegalArgumentException("-missingTask must be followed by -un= argument being username");
            }
        }
    }

    private static void createUser(String[] args, Connection conn) throws SQLException, IllegalArgumentException {
        String fullName = args[1].substring(5, args[1].length() - 1) + " " + args[2].substring(5, args[2].length() - 1);
        fullName = fullName.replaceAll("'", "");
        String userName = args[3].substring(5, args[3].length() - 1).replaceAll("'", "");
        User user = new User(fullName, userName);
        PreparedStatement readUsers = conn.prepareStatement("SELECT username FROM task_manager.user");
        ResultSet usernames = readUsers.executeQuery();
        while (usernames.next()) {
            if (userName.equals(usernames.getString(1))) {
                throw new IllegalArgumentException("Username already taken! Please choose another username!");
            }
        }
        PreparedStatement insertUser = conn.prepareStatement("INSERT INTO task_manager.user (name, username) VALUES (?, ?)");
        insertUser.setString(1, user.getName());
        insertUser.setString(2, user.getUsername());
        insertUser.executeUpdate();
    }

    private static void showAllUsers(Connection conn) throws SQLException {
        PreparedStatement readUsers = conn.prepareStatement("SELECT * FROM task_manager.user");
        ResultSet users = readUsers.executeQuery();
        while (users.next()) {
            PreparedStatement readNumberOfTasks = conn.prepareStatement("SELECT (title) FROM task_manager.task WHERE user_id=?");
            readNumberOfTasks.setInt(1, users.getInt(1));
            ResultSet tasks = readNumberOfTasks.executeQuery();
            int numberOfTasks = 0;
            while (tasks.next()) {
                numberOfTasks++;
            }
            System.out.println(users.getString(2) + " has " + numberOfTasks + " tasks");
        }
    }

    private static void addTask(String[] args, Connection conn) throws IllegalArgumentException, SQLException {
        String userName = args[1].substring(5, args[1].length() - 1);
        validateUser(userName, conn);
        String taskTitle = args[2].substring(4);
        String taskDescription = args[3].substring(4);
        PreparedStatement getUserId = conn.prepareStatement("SELECT id FROM task_manager.user WHERE username=?");
        getUserId.setString(1, userName);
        ResultSet userId = getUserId.executeQuery();
        while (userId.next()) {
            PreparedStatement addTask = conn.prepareStatement("INSERT INTO task_manager.task (title, description, user_id) VALUES (?,?,?)");
            addTask.setString(1, taskTitle);
            addTask.setString(2, taskDescription);
            addTask.setInt(3, userId.getInt(1));
            addTask.executeUpdate();
        }
    }

    private static void addTaskWithDueDate(String[] args, Connection conn) throws IllegalArgumentException, SQLException {
        String userName = args[1].substring(5, args[1].length() - 1);
        validateUser(userName, conn);
        String taskTitle = args[2].substring(4);
        String taskDescription = args[3].substring(4);
        String dueDateStr = args[4].substring(5, args[4].length() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
        PreparedStatement getUserId = conn.prepareStatement("SELECT id FROM task_manager.user WHERE username=?");
        getUserId.setString(1, userName);
        ResultSet userId = getUserId.executeQuery();
        while (userId.next()) {
            PreparedStatement addTask = conn.prepareStatement("INSERT INTO task_manager.task (title, description, user_id, due_date) VALUES (?,?,?,?)");
            addTask.setString(1, taskTitle);
            addTask.setString(2, taskDescription);
            addTask.setInt(3, userId.getInt(1));
            addTask.setDate(4, Date.valueOf(dueDate));
            addTask.executeUpdate();
        }
    }

    private static void showTasksByUser(String[] args, Connection conn) throws IllegalArgumentException, SQLException {
        String username = args[1].substring(5, args[1].length() - 1);
        validateUser(username, conn);
        PreparedStatement getUserId = conn.prepareStatement("SELECT id FROM task_manager.user WHERE username=?");
        getUserId.setString(1, username);
        ResultSet userId = getUserId.executeQuery();
        while (userId.next()) {
            PreparedStatement getTasks = conn.prepareStatement("SELECT title, description, due_date FROM task_manager.task WHERE user_id=?");
            getTasks.setInt(1, userId.getInt(1));
            ResultSet tasks = getTasks.executeQuery();
            if (!tasks.next()) {
                System.out.println("This user has no assignments yet!");
            } else {
                while (tasks.next()) {
                    System.out.print(tasks.getString(1) + "\t");
                    System.out.print(tasks.getString(2) + "\t");
                    System.out.println(tasks.getString(3));
                }
            }
        }
    }

    private static void dueUntil(String[] args, Connection conn) throws SQLException, IllegalArgumentException {
        String username = args[1].substring(5, args[1].length() - 1);
        validateUser(username, conn);
        String dueDateStr = args[2].substring(5, args[2].length() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        PreparedStatement getUserId = conn.prepareStatement("SELECT id FROM task_manager.user WHERE username=?");
        getUserId.setString(1, username);
        ResultSet user = getUserId.executeQuery();
        while (user.next()) {
            PreparedStatement getTasks = conn.prepareStatement("SELECT title, description, due_date FROM task_manager.task WHERE user_id=? AND due_date BETWEEN ? AND ?");
            getTasks.setInt(1, user.getInt(1));
            getTasks.setDate(2, Date.valueOf(now));
            getTasks.setDate(3, Date.valueOf(dueDate));
            ResultSet tasks = getTasks.executeQuery();
            if (!tasks.next()) {
                System.out.println("This user has no assigned tasks!");
            }
            while (tasks.next()) {
                System.out.print(tasks.getString(1) + "\t");
                System.out.print(tasks.getString(2) + "\t");
                System.out.println(tasks.getDate(3));
            }
        }
    }

    private static void missingTasks(String[] args, Connection conn) throws SQLException, IllegalArgumentException {
        String username = args[1].substring(5, args[1].length() - 1);
        validateUser(username, conn);
        PreparedStatement getUserId = conn.prepareStatement("SELECT id FROM task_manager.user WHERE username=?");
        getUserId.setString(1, username);
        ResultSet user = getUserId.executeQuery();
        String now = LocalDate.now().toString();
        while (user.next()) {
            PreparedStatement getTasks = conn.prepareStatement("SELECT title, description, due_date FROM task_manager.task WHERE due_date <= ? AND user_id=?");
            getTasks.setDate(1, Date.valueOf(now));
            getTasks.setInt(2, user.getInt(1));
            ResultSet tasks = getTasks.executeQuery();
            if (!tasks.next()) {
                System.out.println("This user has no missing tasks!");
            }
            while (tasks.next()) {
                System.out.print(tasks.getString(1) + "\t");
                System.out.print(tasks.getString(2));
                System.out.println(" had to be done before " + tasks.getDate(3));
            }
        }
    }

    private static void validateUser(String userName, Connection conn) throws SQLException, IllegalArgumentException {
        PreparedStatement getUser = conn.prepareStatement("SELECT * FROM task_manager.user WHERE username=?");
        getUser.setString(1, userName);
        ResultSet user = getUser.executeQuery();
        if (!user.next()) {
            throw new IllegalArgumentException("Please enter an existing user!");
        }
    }
}
