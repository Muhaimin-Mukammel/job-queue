package lobby;

import database.Task;

// This is the object
public class Job {

    private final String taskid;
    private final Task task;

    public Job(String taskid, Task task){
        this.taskid = taskid;
        this.task = task;
    }

    public String getTaskid(){
        return this.taskid;
    }
    public Task getTask(){
        return this.task;
    }
}
