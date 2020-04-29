package dinnerForPhilosophers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import static java.lang.System.out;

public class Main {

    // 需要用餐的哲学家数量
    static int philosopherCount = 5;

    static Philosopher[] philosophers = new Philosopher[philosopherCount];
    static Semaphore[] signals = new Semaphore[philosopherCount];

    // 哲学家的状态
    static final int EATING = 1;
    static final int THINKING = 2;
    static final int HUNGRY = 3;

    static int[] states = new int[philosopherCount];
    static Semaphore mutex = new Semaphore(1);


    static class Philosopher implements Runnable {
        int no;

        public Philosopher(int no){
            this.no = no;
        }

        String getLogPrefix(){
            return "[Philosopher " + this.no + "]:";
        }

        void eat() throws InterruptedException {
            String prefix = getLogPrefix();

            out.println(prefix + "Dinner is ready now, start eating..");
            Thread.sleep(3000);
            out.println(prefix + "Thanks for the dinner, I really enjoyed it.");
        }

        void think() throws InterruptedException {
            String prefix = getLogPrefix();
            out.println(prefix + "thinking..");
            Thread.sleep(5000);
            out.println(prefix + "I feel a little hungry..Is dinner ready?");

        }

        static int getLeftNo(int no){
            return (no + philosopherCount - 1) % philosopherCount;
        }

        static int getRightNo(int no){
            return (no + 1) % philosopherCount;
        }

        static void checkState(final int no){

            int left = getLeftNo(no), right = getRightNo(no);

            if (states[no] == HUNGRY && states[left] != EATING && states[right] != EATING){
                // 如果当前是在 THINKING，就不要拿筷子了
                // 将自己的状态设置成 EATING，可以同时阻塞左、右邻居
                // 可以视为是同时拿起左、右两根筷子
                states[no] = EATING;
                signals[no].release();
            }
        }

        @Override
        public void run() {
            while(true) {
                try {
                    int self = this.no;
                    int left = getLeftNo(self), right = getRightNo(self);
                    think();
                    // 思考完毕，觉得饿了
                    mutex.acquire();
                    states[self] = HUNGRY;
                    checkState(self);
                    mutex.release();
                    // 没有拿到signal的饥饿的哲学家会阻塞在这里
                    signals[self].acquire();
                    eat();

                    mutex.acquire();
                    states[self] = THINKING;
                    mutex.release();

                    // 让左边和右边的哲学家检查一下是否可以吃饭了
                    checkState(left);
                    checkState(right);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    static void init() {
        for(int i = 0; i< philosopherCount; i++){
            philosophers[i] = new Philosopher(i);
            signals[i] = new Semaphore(0);
        }

        // 一开始所有哲学家都在思考
        Arrays.fill(states, THINKING);
    }

    public static void main(String[] args) throws InterruptedException {

        init();
        List<Thread> threads = new ArrayList<>();
        for(Philosopher philosopher : philosophers){
            Thread t = new Thread(philosopher);
            t.start();
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.join();
        }


    }

}
