package dashboard;

public class Log {
    public static synchronized void print(String msg) {
        System.out.println(msg);
    }
}
