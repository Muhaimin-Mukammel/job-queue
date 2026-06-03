package database;

public class Task{

    private int taskno;
    private String type;
    private String work;
    private String status;

    // Constructors
    public Task (){};
    public Task(int taskno, String type, String work, String status){
        this.taskno = taskno;
        this.type = type;
        this.work = work;
        this.status = status;
    }

    // Getters
    public int getTaskno(){
        return this.taskno;
    }
    public String getType(){
        return this.type;
    }
    public String getWork(){
        return this.work;
    }
    public String getStatus(){
        return this.status;
    }

    // Setters
    public void setTaskno(int taskno){
        this.taskno = taskno;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setWork(String work){
        this.work = work;
    }
    public void setStatus(String status){
        this.status = status;
    }
}
