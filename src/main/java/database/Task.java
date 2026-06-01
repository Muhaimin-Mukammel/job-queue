package database;

public class Task{
    private int taskno;
    private String type;
    private String work;

    public Task (){};
    public Task(int taskno, String type, String work){
        this.taskno = taskno;
        this.type = type;
        this.work = work;
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
}
