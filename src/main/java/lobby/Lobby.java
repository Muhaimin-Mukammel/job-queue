package lobby;

public class Lobby {
    volatile boolean running = true;
    private JobAdder jobAdder;

    public void shutdown() {
        running = false;
        jobAdder.shutdown();
    }
}