package worker;

import database.TaskGroup;
import lobby.Job;

import java.util.concurrent.BlockingQueue;


public class WorkerManager {

    private final BlockingQueue<Job> queue;
    private final TaskGroup database;
    private final int workercount;

    private final ThreadPool threadpool;

    // Constructor
    public WorkerManager(BlockingQueue<Job> queue, TaskGroup database, int workercount){
        this.queue = queue;
        this.database = database;
        this.workercount = workercount;

        this.threadpool = new ThreadPool(queue, database);
    }

    public void startWorkerManager(){
        threadpool.startThreadpool(workercount);
    }

    public void shutDownworkers(){
        threadpool.shutdownThreadpool();
    }

}
