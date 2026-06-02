package lobby;

public class Lobby {
    // Volatile make the changes visible to all the threads immediately
    volatile boolean running = true;
    private JobAdder jobAdder;

    public void shutdown() {
        running = false;
        jobAdder.shutdown();
    }
}