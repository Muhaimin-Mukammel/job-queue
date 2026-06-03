package worker;

import database.Stateupgrader;
import database.Task;
import lobby.Job;

import java.util.concurrent.BlockingQueue;

public class Processor {

    private final BlockingQueue<Job> queue;

    // Constructors
    public Processor(BlockingQueue<Job> queue){
        this.queue = queue;
    }
    Stateupgrader stateupgrader = new Stateupgrader();
    public void workProcessor(Job task) throws InterruptedException {
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
    }
}
