import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * Created by duncan on 4/5/17.
 */

public class MapTask {

    public void processInput(int id, String input, iMaster theMaster) throws RemoteException, AlreadyBoundException {
        System.out.println("processing input: " + input);
        String[] messyWords = input.split("\\s+");

        HashMap<String, Integer> miniHist = new HashMap<>();

        for (int i = 0; i < messyWords.length; i++) {
            String word = messyWords[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
            System.out.println(word);
            if (!word.equals("")){
                miniHist.put(word, miniHist.getOrDefault(word, 0) + 1);
            }
        }
        String[] distinctWords = miniHist.keySet().toArray(new String[miniHist.size()]);

        Tuple<iReducerManager, Integer>[] t = theMaster.getReducers(distinctWords);
        // this semaphore will be used to verify that all reducers have received values
        // before we report that the mapper is done.
        Semaphore valuesReportedSemaphore = new Semaphore(0);

        try {
            for (int i = 0; i < distinctWords.length; i++) {
                final iReducerManager reducerManager = t[i].x;
                final int reducerID = t[i].y;
                final int value = miniHist.get(distinctWords[i]);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reducerManager.receiveValue(reducerID, value);
                            valuesReportedSemaphore.release();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            valuesReportedSemaphore.acquire(distinctWords.length);
//            theMaster.markMapperDone(name);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
