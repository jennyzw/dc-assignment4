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
    Semaphore mapTasksMutex;
    Semaphore reduceTasksMutex;

    public Manager() {
        mappers = new HashMap<>();
        reducers = new HashMap<>();
        nextMapperID = 0;
        nextReducerID = 0;
        mapTasksMutex = new Sempahore(1);
        reduceTasksMutex = new Semaphore(1);
    }

    @Override
    public int createMapTask() throws RemoteException, AlreadyBoundException {
        mapTasksMutex.aquire();
        nextMapperID++;
        MapTask mapper = new MapTask();
        mappers.put(nextMapperID, mapper);
        mapTasksMutex.release();
        return nextMapperID;
    }

    @Override
    public void processInput(int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {
       MapTask mapper = mappers.get(id);
       mapper.processInput(id, input, theMaster);
    }

    @Override
    public int createReduceTask(String key) throws RemoteException, AlreadyBoundException {
        reduceTasksMutex.acquire();
        nextReducerID++;
        ReduceTask reducer = new ReduceTask(key);
        reducer.put(nextReducerID, reducer);
        reduceTasksMutex.release();
        return nextReducerID;
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