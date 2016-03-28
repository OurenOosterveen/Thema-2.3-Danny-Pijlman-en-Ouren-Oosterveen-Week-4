/**
 * Created by Danny on 22/03/2016.
 */
public class Test2 implements Runnable {
    public static void main(String[] args) {
        new Test();
    }
    public Test2(){
        Thread t = new Thread(this);
        t.start();
        t.start();
    }
    public void run() {
        System.out.println("test");
    }
}