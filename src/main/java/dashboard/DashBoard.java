package dashboard;

import database.TaskGroup;
import lobby.Job;
import worker.Information;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import worker.WorkerManager;

public class DashBoard {
    private final BlockingQueue<Job> queue;
    private final TaskGroup database;
    private final int runtimeinsecond;
    private final WorkerManager workerManager;

    private volatile boolean active = true;

    public DashBoard(BlockingQueue<Job> queue, TaskGroup database, WorkerManager workerManager, int Runtimeinsecond){
        this.queue = queue;
        this.database = database;
        this.workerManager = workerManager;
        this.runtimeinsecond = Runtimeinsecond;
    }

    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void Dashboard(){

        System.out.println("===== DASHBOARD =====");

        scheduler.scheduleAtFixedRate(() -> {

            if(!active) return;

            Log.print("Running Job : " +  workerManager.getworkingjobinfo());
            Log.print("Completed jobs : " +  workerManager.getdonejobinfo());
        },  0, 1, TimeUnit.SECONDS);



        scheduler.schedule(() ->{
            List<Information> informationList = workerManager.getinfo();
            int total = 0;
            for (Information info : informationList) {
                int work = info.getWork();
                total += work;
                String name = info.getName();
                Log.print(name + " " + work);
            }
            Log.print("Total Job done : " + total);
            active = false;
            scheduler.shutdown();
        }, runtimeinsecond, TimeUnit.SECONDS);


    }
}
