package base;
import static java.lang.System.out;

public class ThreadBase {

    protected void log(String msg){
        out.println(String.format("[Thread - %d]: %s", Thread.currentThread().getId(), msg));
    }

    protected void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
