import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public class Master implements iMaster {
    @Override
    public iReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException {
        return new iReducer[0];
    }

    @Override
    public void markMapperDone() throws RemoteException {

    }

    @Override
    public void receiveOutput(String key, int value) throws RemoteException {

    }
}
