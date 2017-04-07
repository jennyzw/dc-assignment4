import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by duncan on 4/3/17.
 */
public class Master implements iMaster {

    private iMaster masterStub;  // for passing out to map/reduce tasks

    private Queue<iMapper> mapManagers;  // stubs to one per worker machine
    private Queue<iReducer> reduceManagers;

    private HashMap<String, iReducer> reduceTasks;  // stubs to active reduce tasks
    private HashMap<String, iMapper> mapTasks;  // tracks active map tasks

    private HashMap<String, Integer> masterWordCount;

    private MapperNameGenerator nameGenerator;

    private class MapperNameGenerator {
        // used to generate mapper identification names
        private int i = 0;

        public String next() {
            i++;
            return String.format("map_%07d", i);
        }
    }


    public Master(String[] workerIPs) {
        mapManagers = new LinkedList<>();
        reduceManagers = new LinkedList<>();
        reduceTasks = new HashMap<>();
        mapTasks = new HashMap<>();
        masterWordCount = new HashMap<>();

        nameGenerator = new MapperNameGenerator();

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
            if (!reduceTasks.containsKey(keys[i])) {
                iReducer reduceManager = reduceManagers.poll();
                reduceManagers.offer(reduceManager);

                reduceTasks.put(keys[i], reduceManager.createReduceTask(keys[i], masterStub));
            }
            res[i] = reduceTasks.get(keys[i]);
        }
        return res;
    }

    @Override
    public void markMapperDone(String name) throws RemoteException {
        mapTasks.remove(name);

        if (mapTasks.isEmpty()) {
            for (iReducer reduceTask : reduceTasks.values()) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reduceTask.terminate();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
    }

    @Override
    public void receiveOutput(String key, int value) throws RemoteException {
        masterWordCount.put(key, value);

        reduceTasks.remove(key);

        if (reduceTasks.isEmpty()) {
            writeToFile();
        }
    }

    private void writeToFile() {
        ArrayList<String> outputLines = new ArrayList<>();
        outputLines.add("Word Count Results:");

        for (String key : masterWordCount.keySet()) {
            outputLines.add(key + ": " + masterWordCount.get(key));
        }

        String log = String.join("\n", outputLines);

        // make sure out/ dir exists
        File dir = new File("out");
        if (!dir.exists()) dir.mkdir();

        // create a new timestamped file
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String pathname = "out/word_count_" + timeStamp + ".txt";
        File logFile = new File(pathname);

        // write log to that file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(log);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Word count complete! Counts have been saved at " + pathname);
    }

    private void wordCountFile (String filepath) {
        Scanner reader = new Scanner(filepath);
        iMapper mapManager;
        iMapper mapTask;

        while (reader.hasNextLine()) {
            mapManager = mapManagers.poll();
            mapManagers.offer(mapManager);

            String mapperName = nameGenerator.next();

            try {
                mapTask = mapManager.createMapTask(mapperName);
                mapTasks.put(mapperName, mapTask);

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
