package server;

import database.TaskGroup;
import lobby.Job;
import lobby.JobAdder;
import worker.WorkerManager;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import dashboard.DashBoard;

public class Orchestrator {

    private final TaskGroup database;
    private final int workercount;

    public Orchestrator(TaskGroup database, int workercount){
        this.database = database;
        this.workercount = workercount;
    }

    // Scheduled Executor Service
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(int RunTimeInSecond) throws Exception {

        JobAdder jobAdder = new JobAdder(database);
        BlockingQueue<Job> queue = jobAdder.getQueue();
        WorkerManager workerManager = new WorkerManager(queue, database, workercount);
        workerManager.startWorkerManager();

        Thread Ja = new Thread(jobAdder);
        Ja.start();

        DashBoard dashBoard = new DashBoard(queue, database, workerManager, RunTimeInSecond);
        dashBoard.Dashboard();

        scheduler.schedule(() -> {
            jobAdder.shutdown();
            workerManager.shutDownworkers();
            workerManager.clearworker();
            scheduler.shutdown();
        }, RunTimeInSecond, TimeUnit.SECONDS);

        Ja.join();
    }
}
