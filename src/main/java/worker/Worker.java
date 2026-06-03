package worker;

import database.TaskGroup;
import database.Stateupgrader;
import database.Task;
import lobby.Job;

import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable{

    private final BlockingQueue<Job> queue;
    private final TaskGroup database;
    private final Task TASK;

    // Constructors
    public Worker(BlockingQueue<Job> queue, TaskGroup database, Task TASK){
        this.queue = queue;
        this.database = database;
        this.TASK = TASK;
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
                    stateupgrader.upgrade(database.getFile(), id ,TASK.getTaskno() ,"WORKING");
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
