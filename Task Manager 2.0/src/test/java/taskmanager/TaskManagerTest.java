package taskmanager;

import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.*;

public class TaskManagerTest {



    @Test
    public void testMain_createUser_ok() throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost/postgres";
        String dbUser = "postgres";
        String dbPassw = "postgres";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassw);
        String[] args = {"-createUser", "-fn='test'", "-ln='test'", "-un='test3'"};
        TaskManager.main(args);
        PreparedStatement getUser = conn.prepareStatement("SELECT * FROM task_manager.user WHERE username=?");
        getUser.setString(1, args[3].substring(5, args[3].length() - 1));
        ResultSet user = getUser.executeQuery();
        assertTrue(user.next());
    }

    @Test
    public void testMain_noParams_exceptionThrown() throws SQLException {
        String[] args = {};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Please specify one of the following entry args: -createUser, -showAllUsers, -addTask, -showTasks, -dueUntil, -missingTasks", e.getMessage());
        }
    }

    @Test
    public void testMain_noParamsAfterCreateUser_exceptionThrown() throws SQLException {
        String[] args = {"-createUser"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-createUser argument must be followed by 3 arguments: -fn=  -ln=  -un=  being first name, last name, username of the user!", e.getMessage());
        }
    }

    @Test
    public void testMain_wrongParamsAfterCreateUser_exceptionThrown() throws SQLException {
        String[] args = {"-createUser", "-fn='test'", "-fn='test'", "-fn='test'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-createUser argument must be followed by 3 arguments: -fn=  -ln=  -un=  being first name, last name, username of the user!", e.getMessage());
        }
    }

    @Test
    public void testMain_createUserUsernameTaken_exceptionThrown() throws SQLException {
        String[] args = {"-createUser", "-fn='test'", "-ln='test'", "-un='test'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Username already taken! Please choose another username!", e.getMessage());
        }
    }

    @Test
    public void testMain_showAllUsers_exceptionThrown() throws SQLException {
        String[] args = {"-showAllUsers", "-someOtherArgs"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("No args expected after: -showAllUsers", e.getMessage());
        }
    }

    @Test
    public void testMain_addTask_ok() throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost/postgres";
        String dbUser = "postgres";
        String dbPassw = "postgres";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassw);
        String[] args = {"-addTask", "-un='test'", "-tt=test test", "-td=test test"}; // username test has id 18
        TaskManager.main(args);
        PreparedStatement getTask = conn.prepareStatement("SELECT * FROM task_manager.task WHERE user_id=18");
        ResultSet tasks = getTask.executeQuery();
        assertTrue(tasks.next());
    }

    @Test
    public void testMain_addTask_notExistingUsernameParam_exceptionThrown() throws SQLException {
        String[] args = {"-addTask", "-un='testtesttest'", "-tt=test test", "-td=test test"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Please enter an existing user!", e.getMessage());
        }
    }

    @Test
    public void testMain_addTaskWithDueDate_ok() throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost/postgres";
        String dbUser = "postgres";
        String dbPassw = "postgres";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassw);
        String[] args = {"-addTask", "-un='test'", "-tt=test test", "-td=test test", "-dd='22/12/2021'"}; // username test has id 18
        TaskManager.main(args);
        PreparedStatement getTask = conn.prepareStatement("SELECT * FROM task_manager.task WHERE user_id=18");
        ResultSet tasks = getTask.executeQuery();
        assertTrue(tasks.next());
    }

    @Test
    public void testMain_addTask_noParamsAfter_exceptionThrown() throws SQLException {
        String[] args = {"-addTask"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Expected args [userName, taskTitle, taskDescription, dueDate] after: -addTask", e.getMessage());
        }
    }

    @Test
    public void testMain_addTask_wrongParamsAfter_exceptionThrown() throws SQLException {
        String[] args = {"-addTask", "-tt='test'", "-tt='test test'", "-tt='test test'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-addTask argument must be followed by 3 arguments: -un=  -tt=  -td=  or  -dd being username assigned to, task title, task description, due date!", e.getMessage());
        }
    }

    @Test
    public void testMain_addTask_tooManyParamsAfter_exceptionThrown() throws SQLException {
        String[] args = {"-addTask", "-tt='test'", "-tt='test test'", "-tt='test test'", "-tt='test test'", "-tt='test test'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Too many arguments are passed. If you are using Windows, try using  double quotes (\") for arguments instead of single quote (')", e.getMessage());
        }
    }

    @Test
    public void testMain_showTasks_noUsernameParam_exceptionThrown() throws SQLException {
        String[] args = {"-showTasks"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-showTask argument must be followed by -un= being username", e.getMessage());
        }
    }

    @Test
    public void testMain_showTasks_notExistingUsernameParam_exceptionThrown() throws SQLException {
        String[] args = {"-showTasks", "-un='testtesttest'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Please enter an existing user!", e.getMessage());
        }
    }

    @Test
    public void testMain_dueUntil_noParams_exceptionThrown() throws SQLException {
        String[] args = {"-dueUntil", "-un='test'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-dueUntil must be followed 2 arguments: -ud= and -dd= being username and due date", e.getMessage());
        }
    }

    @Test
    public void testMain_dueUntil_wrongParams_exceptionThrown() throws SQLException {
        String[] args = {"-dueUntil", "-tt='username'", "-dd='22/12/2021'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-dueUntil must be followed 2 arguments: -ud= and -dd= being username and due date", e.getMessage());
        }
    }

    @Test
    public void testMain_dueUntil_notExistingUsernameParam_exceptionThrown() throws SQLException {
        String[] args = {"-dueUntil", "-un='testtesttest'", "-dd='22/12/2021"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Please enter an existing user!", e.getMessage());
        }
    }

    @Test
    public void testMain_missingTasks_noParams_exceptionThrown() throws SQLException {
        String[] args = {"-missingTasks"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("-missingTask must be followed by -un= argument being username", e.getMessage());
        }
    }

    @Test
    public void testMain_missingTasks_notExistingUsernameParam_exceptionThrown() throws SQLException {
        String[] args = {"-missingTasks", "-un='testtesttest'"};
        try {
            TaskManager.main(args);
        } catch (IllegalArgumentException e) {
            assertEquals("Please enter an existing user!", e.getMessage());
        }
    }
}
