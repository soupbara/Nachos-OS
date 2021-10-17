package nachos.threads;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */

    private Lock lock;
    //private Condition2 condSpeaker;
    //private Condition2 condListener;
    //private Message message;

    private static LinkedList<Message> speakerMessageList;
    private static LinkedList<Message> listenerMessageList;

    public Communicator() {
        lock = new Lock();
        //message = new Message();
        //condSpeaker = new Condition2(lock);
        //condListener = new Condition2(lock);
        speakerMessageList = new LinkedList<>();
        listenerMessageList = new LinkedList<>();
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param word the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();

        Message m;
        if (listenerMessageList.isEmpty()){
            m= new Message(word);
            speakerMessageList.add(m);
            m.cond.sleep();
        } else {
            m = listenerMessageList.poll();
            m.cond.wake();
        }
        m.setWord(word);

        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */

    public int listen() {
        lock.acquire();

        int word;

        Message m;

        if(speakerMessageList.isEmpty()){
            m = new Message();
            listenerMessageList.add(m);
            m.cond.sleep();
        } else {
            m = speakerMessageList.poll();
            m.cond.wake();
        }
        word = m.word;

        lock.release();
        return word;
    }

    private class Message {
        int word;
        Condition2 cond;

        public Message() {
            this.word = -1;
            cond = new Condition2(lock);
        }
        public Message(int word) {
            this.word = word;
            cond = new Condition2(lock);
        }

        public void setWord(int word) {
            this.word = word;
        }
    }
}
