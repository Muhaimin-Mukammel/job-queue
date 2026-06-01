package lobby;

public class Lobby {
    volatile boolean running = true;
    private JobAdder jobAdder;

    public void start() {
        jobAdder = new JobAdder();
        jobAdder.init();
    }

    public void shutdown() {
        running = false;
        jobAdder.shutdown();
    }
}