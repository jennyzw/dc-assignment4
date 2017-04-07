import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Created by duncan on 4/3/17.
 */
public interface iMaster extends Remote {
	public iReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	public void markMapperDone(String name) throws RemoteException;
	public void receiveOutput(String key,int value) throws RemoteException;
}
