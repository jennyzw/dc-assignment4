import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public interface iMapper extends Remote {
    // TODO: what is this method for?
    public iMapper createMapTask (String name) throws RemoteException, AlreadyBoundException;

    public void processInput (String input, iMaster theMaster) throws RemoteException, AlreadyBoundException;
}
