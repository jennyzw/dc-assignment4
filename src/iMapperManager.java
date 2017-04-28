import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMapperManager extends Remote, Serializable {
    public int createMapTask () throws RemoteException;
    public void processInput (int id, String input, iMaster theMaster) throws RemoteException;
}
