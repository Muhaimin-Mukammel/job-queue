package worker;

import database.Task;
import java.util.concurrent.BlockingQueue;

public class Processor {

    private final BlockingQueue<Task> queue;

    // Constructors
    public Processor(BlockingQueue<Task> queue){
        this.queue = queue;
    }

    public void workProcessor(Task task) throws InterruptedException {
            task.setWork("COMPLETED");
            Thread.sleep(100);
    }
}
