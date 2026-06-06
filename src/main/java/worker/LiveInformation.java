package worker;

import java.util.concurrent.atomic.AtomicInteger;

public class LiveInformation {
    final AtomicInteger Livejobcounter = new AtomicInteger(0);
    final AtomicInteger Livedonejobcounter = new AtomicInteger(0);

    public void Jobworking(){
        Livejobcounter.incrementAndGet();
    }
    public void JobDone(){

        Livejobcounter.decrementAndGet();
        Livedonejobcounter.incrementAndGet();
    }

    public AtomicInteger getWorkingjob(){
        return this.Livejobcounter;
    }
    public AtomicInteger getDonejob(){
        return this.Livedonejobcounter;
    }
}
