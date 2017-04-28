import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by duncan on 4/5/17.
 */
public class ReduceTask {

    private iMaster master;
    private String key;
    public int count;

    public ReduceTask() {
        
    }

    public ReduceTask(String key, iMaster master) {
        this.master = master;
        this.key = key;
        this.count = 0;
    }

    public void receiveValues(int id, int value) throws RemoteException {
        count += value;
    }

    public int terminate() throws RemoteException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    System.out.println(key + " count: " + count);
                    System.out.println("reporting count to master: " + key + " = " + count);
                    master.receiveOutput(key, count);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return 0;
    }

    @Override
    public int getCount() throws RemoteException {
        return count;
    }


    public static void main(String[] args) {
        new ReduceTask(true);
    }
}
