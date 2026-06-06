package worker;

import database.TaskGroup;
import database.Stateupgrader;
import database.Task;
import lobby.Job;
import java.util.concurrent.BlockingQueue;
import java.io.File;

public class Worker implements Runnable{
    private final BlockingQueue<Job> queue;
    private final TaskGroup database;
    private final String threadname;
    private final Stateupgrader stateupgrader;
    private final LiveInformation liveInformation;
    // Constructors
    public Worker(BlockingQueue<Job> queue, TaskGroup database, String threadname, LiveInformation liveInformation) throws Exception {
        this.queue = queue;
        this.database = database;
        this.threadname = threadname;
        this.liveInformation = liveInformation;

        this.stateupgrader = new Stateupgrader(database.getFile());
    }

    private int numberofworkdone = 0;

    @Override
    public void run() {
        Processor processor = null;
        try {
            processor = new Processor(queue, database.getFile());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (!Thread.currentThread().isInterrupted()) {

            try {
                    Job job = queue.take();

                    liveInformation.Jobworking();

                    int id = job.getTaskid();
                    Task task = job.getTask();
                    int taskid = task.getTaskno();
                    File file = database.getFile();

                try{
                    stateupgrader.upgrade(id ,taskid ,"WORKING");

                    processor.workProcessor(job, file, id, taskid);

                    numberofworkdone += 1;

                    stateupgrader.upgrade(id , taskid ,"COMPLETED");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    liveInformation.JobDone();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    public int getNumberofworkdone(){
        return this.numberofworkdone;
    }
    public String getNameofthethread(){
        return threadname;
    }
}
