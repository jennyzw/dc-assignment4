import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/3/17.
 */
public class MapperReducer implements iMapper, iReducer{
    @Override
    public iReducer createReduceTask(String key, iMaster master) throws RemoteException, AlreadyBoundException {
        return null;
    }

    @Override
    public void receiveValues(int value) throws RemoteException {

    }

    @Override
    public int terminate() throws RemoteException {
        return 0;
    }

    @Override
    public iMapper createMapTask(String name) throws RemoteException, AlreadyBoundException {
        return null;
    }

    @Override
    public void processInput(String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {

    }
}
