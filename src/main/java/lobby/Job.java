package lobby;

import database.Task;

// This is the object
public class Job {

    private final int taskid;
    private final Task task;

    public Job(int taskid, Task task){
        this.taskid = taskid;
        this.task = task;
    }

    public int getTaskid(){
        return this.taskid;
    }
    public Task getTask(){
        return this.task;
    }
}
