package ReadersAndWriters;

import base.ThreadBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class WriterFirst {
    static Semaphore readAccess = new Semaphore(1);
    static Semaphore resource = new Semaphore(1);



    static int readerNums = 5;
    static int writerNums = 2;

    static class Writer extends ThreadBase implements Runnable {

        static int writers = 0;
        static Semaphore mutex = new Semaphore(1);

        void write(){
            log("Making MasterPiece");
            sleep(1000);
            log("Done.");
        }

        @Override
        public void run() {
            while(true){
                try {
                    mutex.acquire();
                    if (writers == 0){
                        // 在这里，当写者一获得读锁
                        // 就能够长期占有，这么做是写者优先的
                        readAccess.acquire();
                    }
                    writers++;
                    mutex.release();

                    // 互斥地写
                    resource.acquire();
                    write();
                    resource.release();

                    mutex.acquire();
                    writers--;
                    if (writers == 0){
                        // 最后一个写者释放可读锁
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
        // 读者对resource的读操作不互斥

        static int readers = 0;
        static Semaphore mutex = new Semaphore(1);

        void incrReaders() throws InterruptedException {
            // 让每个读者都先去尝试获取读锁，然后释放
            // 能够增加写者获得读锁的概率
            readAccess.acquire();
            mutex.acquire();
            if (readers == 0) {
                // 第一个进行阅读的读者将资源锁住即可
                resource.acquire();
            }
            readers++;
            mutex.release();
            readAccess.release();
        }

        void decreaseReaders() throws InterruptedException {
            mutex.acquire();
            readers--;
            if (readers == 0) {
                // 最后一个读者，将读写锁释放
                resource.release();
            }
            mutex.release();
        }


        void read() {
            log("reading..");
            sleep(500);
            log("Finished reading.");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    incrReaders();
                    read();
                    decreaseReaders();
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

        for (int i=0;i<writerNums;i++){
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
