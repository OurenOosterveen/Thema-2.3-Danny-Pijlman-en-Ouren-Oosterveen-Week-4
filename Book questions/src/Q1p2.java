/**
 * Created by ouren on 17-3-2016.
 */
public class Q1p2 implements Runnable {
    private int doubleThis = 1;
    private static int nextThreadNmbr = 0;
    public static void main(String[] args)
    {
        new Q1p2();
    }

    public Q1p2()
    {
        Thread t1 = new Thread(this);
        Thread t2 = new Thread(this);
        Thread t3 = new Thread(this);
        Thread t4 = new Thread(this);

        t1.start();
        nextThreadNmbr++;
        t2.start();
        nextThreadNmbr++;
        t3.start();
        nextThreadNmbr++;
        t4.start();
    }

    public void run()
    {
        doubleThis++;
        if (doubleThis < nextThreadNmbr) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.print(doubleThis);
        System.out.println(doubleThis + ", ");
    }
}
