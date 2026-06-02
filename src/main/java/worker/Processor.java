package worker;

import database.Task;
import java.util.concurrent.BlockingQueue;

public class Processor {

    private final BlockingQueue<Task> queue;

    // Constructors
    public Processor(BlockingQueue<Task> queue){
        this.queue = queue;
    }

    public void workProcessor() throws InterruptedException {
        Task task = queue.take();
            task.setWork("COMPLETED");
    }
}
