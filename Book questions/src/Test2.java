/**
 * Created by ouren on 15-3-2016.
 */
public class Test2 implements Runnable {
    public static void main(String[] args) {
        new Test2();
    }
    public Test2() {
        Thread t = new Thread(this);
        Thread t2 = new Thread(this);
        t.start();
        t2.start();
    }
    public void run() {
        System.out.println("test");
    }
}