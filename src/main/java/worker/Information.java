package worker;


public class Information{
    private final int work;
    private final String name;

    public Information(int work, String name){
        this.work = work;
        this.name = name;
    }

    public int getWork(){
        return this.work;
    }
    public String getName(){
        return this.name;
    }


}
