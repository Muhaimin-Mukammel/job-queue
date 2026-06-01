package worker;

import database.Database;
import database.Task;
import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable{
    private final BlockingQueue<Task> queue;
    private final Database database;

    public Worker(BlockingQueue<Task> queue, Database database){
        this.queue = queue;
        this.database = database;
    }
    @Override
    public void run(){
        while(queue.size() > 0){
            try {
                Task task = queue.take();
                database.setStatus("completed", task.getid());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
