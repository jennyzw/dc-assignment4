import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * Created by duncan on 4/5/17.
 */

public class MapTask {

    String name;

    public MapTask(String name, boolean isManager) {
        this.name = name;
    }

    public void processInput(int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {
        System.out.println(name + " processing input: " + input);
        String[] messyWords = input.split("\\s+");
//        System.out.println(words.length);

        HashMap<String, Integer> miniHist = new HashMap<>();

        for (int i = 0; i < messyWords.length; i++) {
            String word = messyWords[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
            System.out.println(word);
            if (!word.equals("")){
                miniHist.put(word, miniHist.getOrDefault(word, 0) + 1);
            }
        }
        String[] distinctWords = miniHist.keySet().toArray(new String[miniHist.size()]);

        iReducerManager[] reducerManagers = theMaster.getReducerManagers(distinctWords);
        // this semaphore will be used to verify that all reducers have received values
        // before we report that the mapper is done.
        Semaphore valuesReportedSemaphore = new Semaphore(0);

        try {
            for (int i = 0; i < distinctWords.length; i++) {
                final iReducerManager reducerManager = reducerManagers[i];
                final int value = miniHist.get(distinctWords[i]);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reducer.receiveValues(value);
                            valuesReportedSemaphore.release();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            valuesReportedSemaphore.acquire(distinctWords.length);
            theMaster.markMapperDone(name);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MapTask m = new MapTask("mapManager", true);
    }
}
