package interfaces;

public class RunnableImpl implements Runnable {

    private int no;
    public RunnableImpl(int no){
        this.no = no;
    }

    void printSelf(){
        System.out.println("Hello, this is " + no + " on Thread " + Thread.currentThread().getId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        printSelf();
    }
}
