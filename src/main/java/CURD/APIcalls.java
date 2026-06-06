package CURD;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.*;

import java.io.IOException;
import java.util.*;

public class APIcalls {
    private final TaskGroup database;
    DataManupulator manupulate;
    ObjectMapper objectMapper;
    Scanner sc;
    public APIcalls(TaskGroup database){
        this.database = database;

        this.manupulate = new DataManupulator(database);
        this.objectMapper = new ObjectMapper();
        this.sc = new Scanner(System.in);
    }


    public void getdata() throws IOException {
        System.out.println("Enter the ID of the User : ");
        String id = sc.nextLine();
        manupulate.load();
        LinkedHashMap<String, TaskGroup> data = manupulate.getData();

        TaskGroup taskGroup = data.get(id);
        String name = taskGroup.getName();
        List<Task> task = taskGroup.getTask();
        System.out.println("Name : " + name);
        for( Task tasks : task){
            int taskno = tasks.getTaskno();
            String tasktype = tasks.getType();
            String work = tasks.getWork();
            String status = tasks.getStatus();

            System.out.println("Task number : " + taskno);
            System.out.println("Task type : " + tasktype);
            System.out.println("Task : " + work);
            System.out.println("Status : " + status);
        }
    }
    public void postdata() throws  IOException{
        System.out.println("Enter the ID of the User : ");
        String id = sc.nextLine();
        manupulate.load();
        LinkedHashMap<String, TaskGroup> data = manupulate.getData();
        TaskGroup taskGroup = data.get(id);
        if(taskGroup == null){
            System.out.println("Enter the name of the user : ");
            String name = sc.nextLine();
            System.out.println("Enter number of tasks : ");
            int numberoftasks = sc.nextInt();
            sc.nextLine();
            List<Task> task = new ArrayList<>();

            for(int i = 1; i <= numberoftasks; i++) {
                System.out.println("Task : " + i);

                System.out.println("Enter type of the task : ");
                String type = sc.nextLine();
                System.out.println("Enter the task : ");
                String work = sc.nextLine();
                String status = "Database";

                task.add(new Task(i, type, work, status));
            }
            manupulate.write(id, name, task);
        }else{
            System.out.println("User with this ID already exists.......");
        }
    }
    public void patchdata() throws IOException {
        System.out.println("Enter the ID of the User : ");
        String id = sc.nextLine();

        manupulate.load();
        LinkedHashMap<String, TaskGroup> data = manupulate.getData();
        TaskGroup taskGroup = data.get(id);
        if (taskGroup == null) {
            System.out.println("User with this ID don't exist in the DataBase");
            return;
        }
        List<Task> task = taskGroup.getTask();

        System.out.println("---User info stored in DataBase---");
        String oldId = taskGroup.getId();

        System.out.println("Name : " + taskGroup.getName());
        for (Task tasks : task) {
            System.out.println("Task number : " + tasks.getTaskno());
            System.out.println("Task type : " + tasks.getType());
            System.out.println("Task : " + tasks.getWork());
            System.out.println("Status : " + tasks.getStatus());
        }

        System.out.println("---Update any info of User---");
        System.out.println("Note : Press enter to skip\n");

        System.out.println("Enter new ID : ");
        String newId = sc.nextLine();
        if (!newId.isEmpty()) {
            taskGroup.setId(newId);
        } else {
            newId = oldId;
        }
        System.out.println("Enter new name : ");
        String name = sc.nextLine();
        if (!name.isEmpty()) {
            taskGroup.setName(name);
        }
        for (Task tasks : task) {
            System.out.println("Enter new Task type : ");
            String tasktype = sc.nextLine();
            if (!tasktype.isEmpty()) {
                tasks.setType(tasktype);
            }
            System.out.println("Enter new Task : ");
            String work = sc.nextLine();
            if (!work.isEmpty()) {
                tasks.setWork(work);
            }
            tasks.setStatus("Database");
        }
        if (!oldId.equals(newId)) {
            data.remove(oldId);
        }
        data.put(newId, taskGroup);
        manupulate.writeAll(data);
    }
    public void deletedata() throws IOException {
        System.out.println("Enter the ID of the User to delete : ");
        String id = sc.nextLine();

        manupulate.load();
        LinkedHashMap<String, TaskGroup> data = manupulate.getData();

        TaskGroup taskGroup = data.get(id);

        if (taskGroup == null) {
            System.out.println("User with this ID does not exist in the Database");
            return;
        }

        System.out.println("---User found---");
        System.out.println("Name : " + taskGroup.getName());
        System.out.println("Are you sure you want to delete? (y/n)");

        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("y")) {
            data.remove(id);
            manupulate.writeAll(data);
            System.out.println("User deleted successfully");
        } else {
            System.out.println("Delete cancelled");
        }
    }
}
