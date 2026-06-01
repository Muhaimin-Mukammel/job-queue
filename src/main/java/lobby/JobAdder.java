package lobby;

import database.*;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

public class JobAdder {
    private final Database database;
    private final BlockingQueue<Task> queue;
    public JobAdder(Database database, BlockingQueue<Task> queue){
        this.database = database;
        this.queue = queue;
    }

    volatile boolean running = true;
    public void init(){
        DataManupulator manupulate = new DataManupulator(database);
        TreeMap<Integer, Database> data = manupulate.getData();
        int id = database.getId();
        int taskid = 1;
        while (data.containsKey(id) &&  running){
            Database db = data.get(id);
            if(db != null){
                taskid = 1;
                List<Task> TASK = database.getTask();
                while (taskid < TASK.size()){
                    queue.offer(TASK.get(taskid));
                    taskid++;
                }
                database.setStatus("Pending", id);
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


