import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMaster extends Remote, Serializable {
	public Tuple<iReducerManager, Integer>[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	public void markMapperDone(int id, iMapperManager manager) throws RemoteException;
	public void receiveOutput(String key,int value) throws RemoteException;
}
