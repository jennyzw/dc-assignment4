import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by duncan on 4/6/17.
 */
public class Manager implements iMapperManager, iReducerManager {

    HashMap<Integer, ReduceTask> mappers;
    HashMap<Integer, ReduceTask> reducers;

    public Manager() {
        mappers = new HashMap<>();
        reducers = new HashMap<>();
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
    public void receiveValue(int id, int value) throws RemoteException {

    }

    @Override
    public int terminate() throws RemoteException {
        return 0;
    }
}

//moving create things to manager
//don't do anything in the main