import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMaster extends Remote,Serializable {
	public Tuple<iReduceManager[], int[]> getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	public void markMapperDone(int id, iMapperManager manager) throws RemoteException;
	public void receiveOutput(String key,int value) throws RemoteException;
}
