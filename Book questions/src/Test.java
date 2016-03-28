/**
 * Created by ouren on 15-3-2016.
 */
public class Test implements Runnable {
    public static void main(String[] args) {
        new Test();
    }
    public Test() {
        //Thread t = new Thread(this);
        new Thread(this).start();
    }

    public void run(){
        System.out.println("test");
    }
}
