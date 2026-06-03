package worker;

import database.Task;
import database.TaskGroup;
import lobby.Job;

import java.util.concurrent.BlockingQueue;


public class WorkerManager {

    private final BlockingQueue<Job> queue;
    private final TaskGroup database;
    private final int workercount;
    private final Task TASK;

    private final ThreadPool threadpool;

    // Constructor
    public WorkerManager(BlockingQueue<Job> queue, TaskGroup database, int workercount, Task TASK){
        this.queue = queue;
        this.database = database;
        this.workercount = workercount;
        this.TASK = TASK;

        this.threadpool = new ThreadPool(queue, database, TASK);
    }

    public void startWorkerManager(){
        threadpool.startThreadpool(workercount);
    }

    public void shutDownworkers(){
        threadpool.shutdownThreadpool();
    }

}
