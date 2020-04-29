package ReadersAndWriters;

import base.ThreadBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class FairnessOne {
    // 引入serviceQueue
    // 让所有线程在开始工作之前
    // 都去请求一遍service

    static Semaphore readAccess = new Semaphore(1);
    static Semaphore resource = new Semaphore(1);
    static Semaphore serviceQueue = new Semaphore(1, true);

    static int readerNums = 5;
    static int writerNums = 5;

    static class Writer extends ThreadBase implements Runnable {

        static Semaphore mutex = new Semaphore(1);
        static int writers = 0;

        void write(){
            log("Making masterPiece..");
            sleep(1000);
            log("Done.");
        }

        @Override
        public void run() {
            while(true){
                try {
                    serviceQueue.acquire();
                    mutex.acquire();
                    if (writers == 0){
                        readAccess.acquire();
                    }
                    writers++;
                    mutex.release();
                    serviceQueue.release();

                    // 互斥写
                    resource.acquire();
                    write();
                    resource.release();

                    mutex.acquire();
                    writers--;
                    if(writers == 0){
                        // 最后一个写者释放读锁
                        readAccess.release();
                    }
                    mutex.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Reader extends ThreadBase implements Runnable {
        static int readers = 0;
        static Semaphore mutex = new Semaphore(1);
        void read() {
            log("reading..");
            sleep(500);
            log("Finished reading.");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    serviceQueue.acquire();
                    readAccess.acquire();
                    mutex.acquire();
                    if (readers == 0) {
                        // 第一个进行阅读的读者， 锁住resource
                        resource.acquire();
                    }
                    readers++;
                    mutex.release();
                    readAccess.release();
                    serviceQueue.release();

                    read();

                    mutex.acquire();
                    readers--;
                    if(readers == 0){
                        // 最后一个读者释放资源锁
                        resource.release();
                    }
                    mutex.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<>();
        for(int i=0;i<readerNums;i++){
            Thread t = new Thread(new Reader());
            t.start();
            threads.add(t);
        }

        for(int i=0;i<writerNums;i++){
            Thread t = new Thread(new Writer());
            t.start();
            threads.add(t);
        }


        for(Thread t : threads){
            t.join();
        }

        System.out.println("Main Thread exiting.");
    }

}
