import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Created by duncan on 4/3/17.
 */
public class Master implements iMaster {

    private iMaster masterStub;  // for passing out to map/reduce tasks

    private Queue<iMapper> mapManagers;  // stubs to one per worker machine
    private Queue<iReducer> reduceManagers;

    private HashMap<String, iReducer> reducers;  // stubs to active reduce tasks
    private int activeMappers;  // tracks how many map tasks are active


    public Master(String[] workerIPs) {
        mapManagers = new LinkedList<>();
        reduceManagers = new LinkedList<>();
        reducers = new HashMap<>();
        activeMappers = 0;

        try {

            // export self
            masterStub = (iMaster) UnicastRemoteObject.exportObject(this, 0);

            // locate stubs to all worker mapper and reducer managers
            Registry registry;
            for (String ip: workerIPs) {
                registry = LocateRegistry.getRegistry(ip);
                mapManagers.offer((iMapper) registry.lookup("mapManager"));
                reduceManagers.offer((iReducer) registry.lookup("reduceManager"));
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public iReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException {
        iReducer[] res = new iReducer[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (!reducers.containsKey(keys[i])) {
                iReducer reduceManager = reduceManagers.poll();
                reduceManagers.offer(reduceManager);

                reducers.put(keys[i], reduceManager.createReduceTask(keys[i], masterStub));
            }
            res[i] = reducers.get(keys[i]);
        }
        return res;
    }

    @Override
    public void markMapperDone() throws RemoteException {

    }

    @Override
    public void receiveOutput(String key, int value) throws RemoteException {

    }

    private void wordCountFile(String filepath) {
        Scanner reader = new Scanner(filepath);
        iMapper mapManager;
        iMapper mapTask;

        while (reader.hasNextLine()) {
            mapManager = mapManagers.poll();
            mapManagers.offer(mapManager);

            try {
                mapTask = mapManager.createMapTask("map task name");  // TODO: why the name?
                activeMappers++;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mapTask.processInput(reader.nextLine(), masterStub);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (AlreadyBoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (RemoteException | AlreadyBoundException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {

        String[] workerIPs = new String[] {
                "52.27.10.195",
                "52.10.38.99",
                "34.209.22.154"
        };

        System.out.println("Initializing and connecting master");
        Master master = new Master(workerIPs);


        System.out.println("type 'start' to commence MapReduce");
        Scanner scan = new Scanner(System.in);
        while (true) {
            if (scan.nextLine().equals("start")) {
                master.wordCountFile("path_goes_here");
            } else {
                System.out.println("didn't catch that, try 'start'...");
            }
        }

    }

}
