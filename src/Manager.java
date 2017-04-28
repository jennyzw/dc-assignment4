import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by duncan on 4/6/17.
 */
public class Manager implements iMapperManager, iReducerManager {

    HashMap<Integer, MapTask> mappers;
    HashMap<Integer, ReduceTask> reducers;
    Integer nextMapperID;
    Integer nextReducerID;

    public Manager() {
        mappers = new HashMap<>();
        reducers = new HashMap<>();
        nextMapperID = 0;
        nextReducerID = 0;
    }

    @Override
    public int createMapTask() throws RemoteException, AlreadyBoundException {
        MapTask mapper = new MapTask();
        mappers.put(nextMapperID, mapper);
        nextMapperID++;
        return mappers.get(mapper);
    }

    @Override
    public void processInput(int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {
       MapTask mapper = mappers.get(id);
       mapper.processInput(id, input, theMaster);
    }

    @Override
    public int createReduceTask(String key) throws RemoteException, AlreadyBoundException {
        ReduceTask reducer = new ReduceTask(key);
        reducer.put(nextReducerID, reducer);
        nextReducerID++;
        return reducers.get(reducer);
    }

    @Override
    public void receiveValue(int id, int value) throws RemoteException {
        ReduceTask reducer = reducers.get(id);
        reducer.receiveValues(value);
    }

    @Override
    public int terminate() throws RemoteException {

    }
}