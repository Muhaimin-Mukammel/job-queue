package worker;

import database.TaskGroup;
import database.Stateupgrader;
import database.Task;
import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable{

    private final BlockingQueue<Task> queue;
    private final TaskGroup database;

    // Constructors
    public Worker(BlockingQueue<Task> queue, TaskGroup database){
        this.queue = queue;
        this.database = database;
    }

    @Override
    public void run() {

        Processor processor = new Processor(queue);
        Stateupgrader stateupgrader = new Stateupgrader();

        while(true){
            try {
                Task task = queue.take();
                try{
                    stateupgrader.upgrade(database.getFile(), task.getid(), "WORKING");
                } catch ( Exception e){
                    e.printStackTrace();
                }
                processor.workProcessor(); // Processing work
                try{
                    stateupgrader.upgrade(database.getFile(), task.getid(), "COMPLETED");
                } catch ( Exception e){
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
