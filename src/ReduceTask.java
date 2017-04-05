import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by duncan on 4/5/17.
 */
public class ReduceTask implements iReducer {

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

    @Override
    public iReducer createReduceTask(String key, iMaster master) throws RemoteException, AlreadyBoundException {
        return null;
    }

    @Override
    public void receiveValues(int value) throws RemoteException {

    }

    @Override
    public int terminate() throws RemoteException {
        return 0;
    }

}
