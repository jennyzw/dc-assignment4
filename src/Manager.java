import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

/**
 * Created by duncan on 4/6/17.
 */
public class Manager implements iMapperManager, iReducerManager {

    public static void main(String[] args) {
        new MapTask("mapManager", true);
        new ReduceTask(true);
    }

    @Override
    public int createMapTask(String name) throws RemoteException, AlreadyBoundException {
        return 0;
    }

    @Override
    public void processInput(int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {

    }

    @Override
    public int createReduceTask(String key, iMaster master) throws RemoteException, AlreadyBoundException {
        return 0;
    }

    @Override
    public void receiveValues(int id, int value) throws RemoteException {

    }

    @Override
    public int terminate() throws RemoteException {
        return 0;
    }
}

//moving create things to manager
//don't do anything in the main