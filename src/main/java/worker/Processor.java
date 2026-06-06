package worker;

import database.Stateupgrader;
import lobby.Job;

import java.io.File;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Processor {

    private final BlockingQueue<Job> queue;
    private final File file;
    private final Stateupgrader stateupgrader;

    // Constructors
    public Processor(BlockingQueue<Job> queue, File file) throws Exception {
        this.queue = queue;
        this.file = file;

        this.stateupgrader = new Stateupgrader(file);
    }

    public void workProcessor(Job task,  File file, int id, int taskid) throws InterruptedException {

        try {
            stateupgrader.upgrade(id, taskid, "WORKING");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
            int min = 500;
            int max = 3000;

            int randomtime = min + new Random().nextInt(max - min);

        try{
            Thread.sleep(randomtime);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
    }
}
