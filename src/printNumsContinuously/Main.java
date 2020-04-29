package printNumsContinuously;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {
    static class PrintThread implements Runnable{

        private List<Integer> nums;
        public Semaphore self;
        public Semaphore other;

        public PrintThread(List<Integer> nums, Semaphore self, Semaphore other ){
            this.nums = nums;
            this.self = self;
            this.other = other;
        }

        void printNum(int num){
            System.out.println("Thread-" + Thread.currentThread().getId() + ": " + num);
        }

        @Override
        public void run() {
            if(nums != null){
                for(Integer num : nums){
                    try {
                        self.acquire();
                        printNum(num);
                        other.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> oddNums = new ArrayList<>();
        List<Integer> evenNums = new ArrayList<>();

        for(int i=1;i<=10;i++){
            if(i%2 == 0){
                evenNums.add(i);
            }else {
                oddNums.add(i);
            }
        }

        Semaphore s1 = new Semaphore(1), s2 = new Semaphore(0);


        PrintThread job1 = new PrintThread(oddNums, s1, s2);
        PrintThread job2 = new PrintThread(evenNums, s2, s1);

        PrintThread[] jobs = new PrintThread[]{job1, job2};
        List<Thread> threads = new ArrayList<>();
        for(PrintThread job: jobs){
            Thread t = new Thread(job);
            threads.add(t);
            t.start();
        }
        for(Thread t: threads){
            t.join();
        }
    }
}
