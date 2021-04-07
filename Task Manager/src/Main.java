import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Please specify one of the following entry args: -createUser, -showAllUsers, -addTask, -showTasks");
        }
        try {
            validateArgs(args);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        File userDataSource = null;
        File taskDataSource = null;
        try {
            userDataSource = createFile("userDataSource.txt");
            taskDataSource = createFile("taskDataSource.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while creating a files!");
            e.printStackTrace();
            System.exit(0);
        }
        BufferedWriter userWriter = new BufferedWriter(new FileWriter(userDataSource.getName(), true));
        BufferedReader userReader = new BufferedReader(new FileReader(userDataSource.getName()));
        BufferedWriter taskWriter = new BufferedWriter(new FileWriter(taskDataSource.getName(), true));
        BufferedReader taskReader = new BufferedReader(new FileReader(taskDataSource.getName()));
        try {
            switch (args[0]) {
                case "-createUser":
                    if (args.length < 2 || args.length > 5) {
                        System.out.println("Expected args [firstName, lastName, userName] after: " + args[0]);
                        System.exit(0);
                    }
                    try {
                        createUser(args, userWriter, userReader);
                    } catch (IOException e) {
                        System.out.println("Couldn't create this user!");
                    }
                    break;
                case "-showAllUsers":
                    if (args.length > 1) {
                        System.out.println("No args expected after:  " + args[0]);
                    }
                    showAllUsers(userReader, taskReader);
                    break;
                case "-addTask":
                    if (args.length < 2 || args.length > 5) {
                        System.out.println("Expected args [userName, taskTitle, taskDescription] after: " + args[0]);
                    }
                    try {
                        addTask(args, taskWriter, userReader);
                    } catch (IOException e) {
                        System.out.println("Couldn't add this task!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "-showTasks":
                    if (args.length > 2) {
                        System.out.println("Expected only [userName] arg after: " + args[0]);
                    }
                    try {
                        showTasksByUser(args, userReader, taskReader);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                default:
                    System.out.println(("Please specify one of the following entry args: -createUser, -showAllUsers, -addTask, -showTasks"));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        userWriter.close();
        userReader.close();
        taskWriter.close();
        taskReader.close();
    }

    private static void validateArgs(String[] args) {
        if (args.length > 4) {
            for (String arg : args) {
                System.out.print(arg + "\t");
            }
            System.out.println();
            throw new IllegalArgumentException("Too many arguments are passed. If you are using Windows, try using \" double quotes for arguments instead of ' single quote");
        }
        if (args[0].equals("-createUser")) {
            if (!(args[1].startsWith("-fn=")) || !(args[2].startsWith("-ln=")) || !(args[3].startsWith("-un="))) {
                throw new IllegalArgumentException("-createUser argument must be followed by 3 arguments: -fn=  -ln=  -un=  being first name, last name, username of the user!");
            }
        } else if (args[0].equals("-addTask")) {
            if (!(args[1].startsWith("-un=")) || !(args[2].startsWith("-tt=")) || !(args[3].startsWith("-td="))) {
                throw new IllegalArgumentException("-addTask argument must be followed by 3 arguments: -un=  -tt=  -td=  being username assigned to, task title, task description!");
            }
        } else if (args[0].equals("-showTasks")) {
            if (!(args[1].startsWith("-un="))) {
                throw new IllegalArgumentException("-showTask must be followed by -un= argument being username that desired view the tasks of!");
            }
        }
    }

    private static File createFile(String name) throws IOException {
        File file = new File(name);
        if (file.createNewFile()) {
            System.out.println("File created: " + file.getName());
            return file;
        }
        return file;
    }
    
    private static void createUser(String[] args, BufferedWriter writer, BufferedReader reader) throws IOException {
        String firstName = args[1].substring(5, args[1].length() - 1);
        String lastName = args[2].substring(5, args[2].length() - 1);
        String userName = args[3].substring(5, args[3].length() - 1);
        String existingUser;
        boolean taken = false;
        if ((existingUser = reader.readLine()) == null) {
            User user = new User(firstName, lastName, userName);
            writer.write(user.toString() + "\n");
        } else {
            while (existingUser != null) {
                String[] existingUserProperties = existingUser.split("\\|");
                if (userName.equals(existingUserProperties[2])) {
                    System.out.println("Username already taken. Choose another username!");
                    taken = true;
                    break;
                }
                existingUser = reader.readLine();
            }
            if (!taken) {
                User user = new User(firstName, lastName, userName);
                writer.write(user.toString() + "\n");
            }
        }
    }


    private static void showAllUsers(BufferedReader userReader, BufferedReader taskReader) throws IOException {
        String existingUser;
        while ((existingUser = userReader.readLine()) != null) {
            String[] existingUserProperties = existingUser.split("\\|");
            int numberOfTasks = getNumberTasksByUser(existingUserProperties[2], taskReader);
            System.out.println(existingUserProperties[0] + " " + existingUserProperties[1] + " has " + numberOfTasks + " tasks.");
        }
    }

    private static void addTask(String[] args, BufferedWriter taskWriter, BufferedReader userReader) throws IOException, IllegalArgumentException {
        String userName = args[1].substring(5, args[2].length() - 1);
        validateUser(userName, userReader);
        String taskTitle = args[2].substring(4);
        String taskDescription = args[3].substring(4);
        Task task = new Task(userName, taskTitle, taskDescription);
        taskWriter.write(task + "\n");
    }

    private static void showTasksByUser(String[] args, BufferedReader userReader, BufferedReader taskReader) throws IOException, IllegalArgumentException {
        String username = args[1].substring(5, args[1].length() - 1);
        validateUser(username, userReader);
        List<Task> allTasks = getAllTasks(taskReader);
        List<Task> usersTasks = new ArrayList<>();
        for (Task task :
                allTasks) {
            if (task.userName.equals(username)) {
                usersTasks.add(task);
            }
        }
        if (usersTasks.isEmpty()) {
            System.out.println("This user has no assignments yet!");
        } else {
            for (Task task :
                    usersTasks) {
                System.out.println(task.printTask());
            }
        }
    }

    private static List<Task> getAllTasks(BufferedReader taskReader) throws IOException {
        List<Task> tasks = new ArrayList<>();
        String existingTaskRow;
        while ((existingTaskRow = taskReader.readLine()) != null) {
            String[] existingTaskProperties = existingTaskRow.split("\\|");
            tasks.add(new Task(existingTaskProperties[0], existingTaskProperties[1], existingTaskProperties[2]));
        }
        return tasks;
    }

    private static int getNumberTasksByUser(String username, BufferedReader taskReader) throws IOException {
        int count = 0;
        List<Task> allTasks = getAllTasks(taskReader);
        for (Task task :
                allTasks) {
            if (task.userName.equals(username)) {
                count++;
            }
        }
        return count;
    }

    private static void validateUser(String userName, BufferedReader userReader) throws IOException {
        boolean exists = false;
        String existingUser;
        while ((existingUser = userReader.readLine()) != null) {
            String[] existingUserProperties = existingUser.split("\\|");
            if (existingUserProperties[2].equals(userName)) {
                exists = true;
            }
        }
        if (!exists) {
            throw new IllegalArgumentException("Please enter an existing user!");
        }
    }
}
