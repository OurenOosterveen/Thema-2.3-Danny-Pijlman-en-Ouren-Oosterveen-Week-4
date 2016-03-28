/**
 * Created by Danny on 22/03/2016.
 */
public class Test implements Runnable{
    public static void main(String[] args){
        Thread test = new Thread();
        new Test();
    }
    public Test(){
        Thread task = new Thread(this);
        task.start();
    }
    public void run(){
        System.out.println("test");
    }
}