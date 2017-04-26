import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMapperManager extends Remote, Serializable {
    public int createMapTask (String name) throws RemoteException, AlreadyBoundException;
    public void processInput (int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException;
}
