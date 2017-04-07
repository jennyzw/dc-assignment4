import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMaster extends Remote,Serializable {
	// TODO: try changing this back to iReducer[]
	public iReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException;
	public void markMapperDone(String name) throws RemoteException;
	public void receiveOutput(String key,int value) throws RemoteException;
}
