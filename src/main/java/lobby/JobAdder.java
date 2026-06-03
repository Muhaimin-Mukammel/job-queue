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

    public JobAdder(TaskGroup database){
        this.database = database;
    }

    @Override
    public void run() {
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

            int groupid = entry.getKey();
            TaskGroup group = entry.getValue();

            List<Task> tasks = group.getTask();

            if(tasks == null){
                continue;
            }

            for( Task t : tasks){
                if(isRunning == false || Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    queue.put(new Job(groupid, t));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    public BlockingQueue<Job> getQueue(){
        return this.queue;
    }
    private Thread thread;
    public void setThread(Thread thread){
        this.thread  = thread;
    }
    public void shutdown(){
        isRunning = false ;

        if(thread != null){
            thread.interrupt();
        }
    }
}


