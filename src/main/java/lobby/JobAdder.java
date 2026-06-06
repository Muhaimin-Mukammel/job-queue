package lobby;

import database.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobAdder implements Runnable{
    private final TaskGroup database;
    private BlockingQueue<Job> queue = new LinkedBlockingQueue<>();

    private volatile boolean isRunning = true;
    // Constructor
    public JobAdder(TaskGroup database){
        this.database = database;
    }

    private Thread thread;

    @Override
    public void run() {
        Stateupgrader stateupgrader;
        try {
            stateupgrader = new Stateupgrader(database.getFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DataManupulator manupulate = new DataManupulator(database);
        try {
            manupulate.load();
        }catch (IOException e){
            e.printStackTrace();
        }
        TreeMap<Integer, TaskGroup> data = manupulate.getData();
        for ( Map.Entry<Integer, TaskGroup> entry : data.entrySet()){
            if(isRunning == false){
                break;
            }
            int id = entry.getKey();
            TaskGroup group = entry.getValue();

            List<Task> tasks = group.getTask();


            if(tasks == null){
                continue;
            }

            for( Task t : tasks) {
                if (isRunning == false || Thread.currentThread().isInterrupted()) {
                    break;
                }
                if ("COMPLETED".equals(t.getStatus())) {
                    continue;
                }else {
                    try {
                        int taskid = t.getTaskno();
                        queue.put(new Job(id, t));
                        stateupgrader.upgrade(id, taskid, "Waiting");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    public BlockingQueue<Job> getQueue(){
        return this.queue;
    }
    public void shutdown(){
        isRunning = false ;
        queue.clear();
        if(thread != null){
            thread.interrupt();
        }
    }
}