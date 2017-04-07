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
    private int count;

    public ReduceTask(boolean isManager) {

        if (isManager) {
            // export manager objects and bind to local rmi for master access
            try {
                Registry registry = LocateRegistry.getRegistry();
                iReducer reduceManagerStub = (iReducer) UnicastRemoteObject.exportObject(this);
                registry.rebind("reduceManager", reduceManagerStub);
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
        return new ReduceTask(key, master);
    }

    @Override
    public void receiveValues(int value) throws RemoteException {
        count += value;
    }

    @Override
    public int terminate() throws RemoteException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    master.receiveOutput(key, count);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        return 0;
    }

}
