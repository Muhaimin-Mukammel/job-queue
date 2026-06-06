package worker;

import database.TaskGroup;
import lobby.Job;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


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
    public void startWorkerManager() throws Exception {
        threadpool.startThreadpool(workercount);
    }

    public void shutDownworkers(){
        threadpool.shutdownThreadpool();
    }

    public List<Information> getinfo(){
        return threadpool.info();
    }

    public AtomicInteger getworkingjobinfo(){
        LiveInformation liveInformation =  threadpool.getLiveInformation();
        AtomicInteger workingjob = liveInformation.getWorkingjob();
        return workingjob;
    }
    public AtomicInteger getdonejobinfo(){
        LiveInformation liveInformation = threadpool.getLiveInformation();
        AtomicInteger donejob = liveInformation.getDonejob();
        return donejob;
    }
    public void clearworker(){
        threadpool.clearWorkers();
    }
}

