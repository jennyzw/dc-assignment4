import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iReducerManager extends Remote, Serializable {
    public int createReduceTask (String key) throws RemoteException;
    public void receiveValue (int id, int value) throws RemoteException;
    public int terminate() throws RemoteException;

    //public int getCount(int id) throws RemoteException;
}
