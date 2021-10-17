package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {


    private static PriorityQueue<AlarmThread> alarmPriorityQueue;
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        alarmPriorityQueue = new PriorityQueue<>();

        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt();
            }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {

        long currentTime = Machine.timer().getTime();

        AlarmThread temp = alarmPriorityQueue.peek();

        while (temp != null && currentTime >= temp.getWakeTime())
        {
            alarmPriorityQueue.remove(temp);
            temp.lock.acquire();

            boolean intStatus = Machine.interrupt().disable();
            temp.condition.wake();
            Machine.interrupt().restore(intStatus);

            temp.lock.release();

            temp = alarmPriorityQueue.peek();
        }
        KThread.currentThread().yield();
}

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param    x    the minimum number of clock ticks to wait.
     * @see    nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        long wakeTime = Machine.timer().getTime() + x;

        //boolean intStatus = Machine.interrupt().disable();
        if(wakeTime > Machine.timer().getTime()) {
            //priority queue; priority = waketime
            AlarmThread alarmThread = new AlarmThread(KThread.currentThread(), wakeTime);
            alarmPriorityQueue.add(alarmThread);
            alarmThread.lock.acquire();


            alarmThread.condition.sleep();



            alarmThread.lock.release();
        }
        //Machine.interrupt().restore(intStatus);
    }

    private class AlarmThread implements Comparable<AlarmThread>
    {
        private KThread thread;
        private long wakeTime;
        private Lock lock;
        private Condition2 condition; //every thread have a condition variable

        public AlarmThread(KThread thread, long wakeTime){
            this.thread = thread;
            this.wakeTime = wakeTime;
            lock = new Lock();
            condition = new Condition2(lock);
        }

        public KThread getThread() {
            return thread;
        }

        public long getWakeTime() {
            return wakeTime;
        }

        @Override
        public int compareTo(AlarmThread other) {
            if (this.wakeTime < other.wakeTime) {
                return -1;
            }
            if (this.wakeTime == other.wakeTime) {
                return 0;
            }
            return 1;
        }
    }
}
