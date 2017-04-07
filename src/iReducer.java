import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iReducer extends Remote, Serializable {
    public iReducer createReduceTask (String key, iMaster master) throws RemoteException, AlreadyBoundException;
    public void receiveValues (int value) throws RemoteException;
    public int terminate() throws RemoteException;

    public int getCount() throws RemoteException;
}
