package server;

import lobby.Lobby;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Orchestrator {

    private Lobby lobby = new Lobby();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(int RunTimeInSecond) {
        scheduler.schedule(() -> {
            lobby.shutdown();
        }, RunTimeInSecond, TimeUnit.SECONDS);
    }
}
