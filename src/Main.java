import interfaces.RunnableImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();


        for(int i=0;i<5;i++){
            Thread t = new Thread(new RunnableImpl(i));
            threads.add(t);
            t.start();
        }
        for(Thread t: threads){
            t.join();
        }
    }
}
