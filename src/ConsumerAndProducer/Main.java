package ConsumerAndProducer;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {

    // 缓冲区，以队列的方式进行消费和生产
    static int[] buffer = new int[10];
    static int expectedProducts = 20;

    static int producerNum = 3;
    static int consumerNum = 5;

    static Semaphore empty = new Semaphore(buffer.length);
    static Semaphore full = new Semaphore(0);
    static Semaphore mutex = new Semaphore(1, true);

    static volatile boolean isEnd = false;

    static class Consumer implements Runnable{

        static int whichToConsume = 0; // 指示从缓冲区中取出哪一个产品

        void log(int prod){
            System.out.println("Consumer Thread-" + Thread.currentThread().getId()
                    + ": " + "consumed " + prod);
        }

        void end() {
            System.out.println("Consumer " + Thread.currentThread().getId() + " ended.");
        }

        @Override
        public void run() {
            while(true) {
                try {
                    full.acquire();
                    mutex.acquire();

                    if(whichToConsume == expectedProducts){
                        // 消费者退出，同时唤醒其他消费者
                        mutex.release();
                        full.release();
                        end();
                        return ;
                    }

                    int i = whichToConsume % buffer.length;
                    log(buffer[i]);
                    whichToConsume++;

                    if(whichToConsume == expectedProducts){
                        // 消费完最后一个产品，提供一个full唤醒其他线程
                        full.release();
                    }
                    empty.release();
                    mutex.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Producer implements Runnable{

        static int totalProducts = 0;
        static int placeToProd = 0; // 指示从缓冲区中哪一个位置放入产品

        void log(int prod){
            System.out.println("Producer Thread-" + Thread.currentThread().getId()
                    + ": " + "produced " + prod);
        }

        boolean checkBeforeProduce(){
            if (totalProducts == expectedProducts){
                if(!isEnd){
                    isEnd = true;
                }
                return false;
            }else {
                return true;
            }
        }

        void end() {
            System.out.println("Producer-" + Thread.currentThread().getId() + " ended.");
        }

        @Override
        public void run() {
            while(true){

                try {
                    empty.acquire();
                    mutex.acquire();
                    boolean ok = checkBeforeProduce();
                    if(!ok){
                        empty.release();
                        mutex.release();
                        end();
                        return ;
                    }
                    buffer[placeToProd%buffer.length] = placeToProd;
                    log(placeToProd);
                    placeToProd++;
                    totalProducts++;
                    mutex.release();
                    full.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for(int i=0;i<producerNum;i++){
            Thread t = new Thread(new Producer());
            t.start();
            threads.add(t);
        }

        for(int i=0;i<consumerNum;i++){
            Thread t = new Thread(new Consumer());
            t.start();
            threads.add(t);
        }

        for(Thread t: threads){
            t.join();
        }

        System.out.println("Main Thread exit.");
    }

}
