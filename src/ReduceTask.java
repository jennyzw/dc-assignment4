import java.rmi.RemoteException;

/**
 * Created by duncan on 4/5/17.
 */
public class ReduceTask {

    private String key;
    public int count;

    public ReduceTask(String key) {
        this.key = key;
        this.count = 0;
    }

    public void receiveValues(int value) throws RemoteException {
        count += value;
    }

    public int terminate() throws RemoteException {

        return 0;
    }

}
