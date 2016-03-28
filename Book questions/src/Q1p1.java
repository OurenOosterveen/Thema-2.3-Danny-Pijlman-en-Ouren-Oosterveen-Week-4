/**
 * Created by ouren on 17-3-2016.
 */
public class Q1p1 implements Runnable {
    private int doubleThis = 1;
    public static void main(String[] args)
    {
        new Q1p1();
    }

    public Q1p1()
    {
        Thread t1 = new Thread(this);
        Thread t2 = new Thread(this);
        Thread t3 = new Thread(this);
        Thread t4 = new Thread(this);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }

    public synchronized void run()
    {
        System.out.print(doubleThis);
        System.out.println(doubleThis + ", ");
        doubleThis++;
    }
}
