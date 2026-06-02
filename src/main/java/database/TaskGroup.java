package database;

import java.io.File;
import java.util.List;


public class TaskGroup {

    File file = new File("src\\main\\java\\database\\storage.json");

    public File getFile(){
        return this.file;
    }
    private int id;
    private String name;
    private List<Task> task;
    private String status;

    // Constructors
    public TaskGroup(){};
    public TaskGroup(int id, String name, List<Task> task , String status){
        this.id = id;
        this.name = name;
        this.task = task;
        this.status = status;
    }

    // Getters
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public List<Task> getTask(){
        return this.task;
    }
    public String getStatus(){
        return this.status;
    }


    // Setters
    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTask(List<Task> task){
        this.task = task;
    }
    public void setStatus(String status){
        this.status = status;
    }




}

