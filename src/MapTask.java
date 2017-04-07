import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by duncan on 4/5/17.
 */

public class MapTask implements iMapper {

    String name;

    public MapTask(String name, boolean isManager) {

        this.name = name;

        if (isManager) {
            try {
                Registry registry = LocateRegistry.getRegistry();
                iMapper mapManagerStub = (iMapper) UnicastRemoteObject.exportObject(this, 0);
                registry.rebind("mapManager", mapManagerStub);
                System.out.println("Map manager ready!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public iMapper createMapTask(String name) throws RemoteException, AlreadyBoundException {
        System.out.println("creating map task: " + name);
        return (iMapper) UnicastRemoteObject.exportObject(new MapTask(name, false), 0);
    }

    @Override
    public void processInput(String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {
        System.out.println("processing input: " + input);
        String [] words = input.split("\\s+");
        HashMap<String, Integer> miniHist = new HashMap<>();

        for (String word : words) {
            word = word.replace("[^a-zA-Z]", "").toLowerCase();
            miniHist.put(word, miniHist.getOrDefault(word, 0) + 1);
        }

        String[] distinctWords = miniHist.keySet().toArray(new String[miniHist.size()]);

        ArrayList<iReducer> reducers = theMaster.getReducers(distinctWords);

        for (int i = 0; i < distinctWords.length; i++) {
            final iReducer reducer = reducers.get(i);
            final int value = miniHist.get(distinctWords[i]);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        reducer.receiveValues(value);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    public static void main(String[] args) {
        MapTask m = new MapTask("mapManager", true);
    }
}
