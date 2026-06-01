package lobby;

import database.*;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

public class JobAdder {
    private final TaskGroup database;
    private final BlockingQueue<Task> queue;
    public JobAdder(TaskGroup database, BlockingQueue<Task> queue){
        this.database = database;
        this.queue = queue;
    }

    volatile boolean running = true;
    public void init() throws IOException {
        DataManupulator manupulate = new DataManupulator(database);
        manupulate.load();
        TreeMap<Integer, TaskGroup> data = manupulate.getData();
        int id = database.getId();
        int taskid = 0;
        while (data.containsKey(id) &&  running){
            TaskGroup db = data.get(id);
            if(db != null){
                taskid = 0;
                List<Task> TASK = database.getTask();
                while (taskid < TASK.size()){
                    queue.offer(TASK.get(taskid));
                    taskid++;
                }
                database.setStatus("PENDING");
            }else{
                System.out.println("task is null in JobAdder ( line 19 )");
            }
            id++;
        }
    }
    public BlockingQueue<Task> getQueue(){
        return this.queue;
    }
    public void shutdown(){
        running = false;
    }
}


