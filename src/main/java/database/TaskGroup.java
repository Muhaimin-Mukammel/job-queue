package database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.List;


public class TaskGroup {
    // Source of truth
    File file = new File("src\\main\\java\\database\\storage.json");
    @JsonIgnore
    public File getFile(){
        return this.file;
    }

    private String id;
    private String name;
    private List<Task> task;

    // Constructors
    public TaskGroup(){};
    public TaskGroup(String id, String name, List<Task> task){
        this.id = id;
        this.name = name;
        this.task = task;
    }

    // Getters
    public String getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public List<Task> getTask(){
        return this.task;
    }

    // Setters
    public void setId(String id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTask(List<Task> task){
        this.task = task;
    }

}

