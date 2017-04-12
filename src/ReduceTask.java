import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by duncan on 4/5/17.
 */
public class ReduceTask implements iReducer {

    private iMaster master;
    private String key;
    public int count;

    public ReduceTask(boolean isManager) {

        if (isManager) {
            // export manager objects and bind to local rmi for master access
            try {
                Registry registry = LocateRegistry.getRegistry(1099);
                iReducer reduceManagerStub = (iReducer) UnicastRemoteObject.exportObject(this, 0);
                registry.rebind("reduceManager", reduceManagerStub);
                System.out.println("Reduce manager ready!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public ReduceTask(String key, iMaster master) {
        this.master = master;
        this.key = key;
        this.count = 0;
    }

    @Override
    public iReducer createReduceTask(String key, iMaster master) throws RemoteException, AlreadyBoundException {
//        System.out.println("creating new reduce task: " + key);
        return (iReducer) UnicastRemoteObject.exportObject(new ReduceTask(key, master), 0);
    }

    @Override
    public void receiveValues(int value) throws RemoteException {
//        System.out.println(key + ": " + value);
        count += value;
    }

    @Override
    public int terminate() throws RemoteException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    System.out.println(key + " count: " + count);
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
