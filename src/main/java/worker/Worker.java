package worker;

import database.TaskGroup;
import database.Stateupgrader;
import database.Task;
import lobby.Job;
import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable{

    private final BlockingQueue<Job> queue;
    private final TaskGroup database;

    // Constructors
    public Worker(BlockingQueue<Job> queue, TaskGroup database){
        this.queue = queue;
        this.database = database;
    }

    @Override
    public void run() {
        Processor processor = new Processor(queue);
        Stateupgrader stateupgrader = new Stateupgrader();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                    Job job = queue.take();

                    int id = job.getTaskid();
                    Task task = job.getTask();

                try{
                    stateupgrader.upgrade(database.getFile(), id ,task.getTaskno() ,"WORKING");

                    processor.workProcessor(job);

                    stateupgrader.upgrade(database.getFile(), id ,task.getTaskno() ,"COMPLETED");
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
