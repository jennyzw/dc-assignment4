import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by duncan on 4/3/17.
 */
public class Master implements iMaster {

    private iMaster masterStub;  // for passing out to map/reduce tasks

    private Queue<iMapperManager> mapManagers;  // stubs to one per worker machine
    private Queue<iReducerManager> reduceManagers;
    private HashMap<String, Tuple<iReducerManager, Integer>> reducerLocations;

    private HashMap<String, Integer> masterWordCount;  // for collecting reducer values
    private Semaphore masterWordCountMutex;


    public Master(String[] workerIPs, int numMappers, int numReducers) {
        mapManagers = new LinkedList<>();
        reduceManagers = new LinkedList<>();
        reducerLocations = new HashMap<>();
        masterWordCount = new HashMap<>();
        masterWordCountMutex = new Semaphore(1);

        try {
            // export self
            masterStub = (iMaster) UnicastRemoteObject.exportObject(this, 0);

            // locate stubs to all worker mapper and reducer managers
            Registry registry;
            for (String ip: workerIPs) {
                registry = LocateRegistry.getRegistry(ip, 1099);

                iMapperManager mapManager = (iMapperManager) registry.lookup("manager");
                mapManagers.offer(mapManager);
                System.out.println("found map manager at ip " + ip + " : " + mapManager);

                iReducerManager reduceManager = (iReducerManager) registry.lookup("manager");
                reduceManagers.offer(reduceManager);
                System.out.println("found reduce manager at ip " + ip + " : " + reduceManager);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple<iReducerManager, Integer>[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException {
        Tuple<iReducerManager, Integer>[] res = new Tuple[keys.length];

        for (int i = 0; i < keys.length; i++) {
            if (!reducerLocations.containsKey(keys[i])) {
                iReducerManager reduceManager = reduceManagers.poll();
                reduceManagers.offer(reduceManager);

                int internalLoc = reduceManager.createReduceTask(keys[i]);
                reducerLocations.put(keys[i], new Tuple<>(reduceManager, internalLoc));
            }
            res[i] = reducerLocations.get(keys[i]);
        }

        return res;
    }

    @Override
    public void markMapperDone(int id, iMapperManager manager) throws RemoteException {
//        try {
//            mapTasksMutex.acquire();
//            mapTasks.remove(name);
//
//            if (mapTasks.isEmpty()) {
//                reduceTasksMutex.acquire();
//                for (iReducer reduceTask : reduceTasks.values()) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                reduceTask.terminate();
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//
//                }
//                reduceTasksMutex.release();
//            }
//            mapTasksMutex.release();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void receiveOutput(String key, int value) throws RemoteException {
        try {
            masterWordCountMutex.acquire();
            masterWordCount.put(key, value);
            masterWordCountMutex.release();

            // TODO write to file?

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile() {
        ArrayList<String> outputLines = new ArrayList<>();
        outputLines.add("Word Count Results:");

        for (String key : masterWordCount.keySet()) {
            outputLines.add(key + ": " + masterWordCount.get(key));
        }

        String log = String.join("\r\n", outputLines);

        // make sure out/ dir exists
        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        // create a new timestamped file
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String pathname = "data/word_count_" + timeStamp + ".txt";
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

    public void wordCountFile (String filepath) {

        // make sure the target file exists
        File target = new File(filepath);
        if(!target.exists()) {
            System.out.println("file " + filepath + " not found, please respecify in src/Master.main");
            return;
        } else {
            System.out.println("found " + filepath + ", commencing count...");
        }

        try {
            Scanner reader = new Scanner(target);
            iMapperManager mapManager;

            while (reader.hasNextLine()) {
                mapManager = mapManagers.poll();
                mapManagers.offer(mapManager);

                final int internalID = mapManager.createMapTask();

                final String line = reader.nextLine();

                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mapManager.processInput(internalID, line);
                            } catch (RemoteException | AlreadyBoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                } catch (RemoteException | AlreadyBoundException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("all lines assigned to mappers");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        String[] workerIPs = new String[] {
                // enter ip's of worker machines here
                "172.31.31.88",
                "172.31.17.196"
        };

        System.out.println("Initializing and connecting master");
        Master master = new Master(workerIPs, 1, 1); //even distribution between machines used for mapping and reducing


        System.out.println("type 'start' to commence MapReduce");
        Scanner scan = new Scanner(System.in);
        while (true) {
            // TODO change this back
//            if (scan.nextLine().equals("start")) {
            if (true) {
                master.wordCountFile(
//                        "dummy.txt"
                        "mission_earth.txt"
                );
                break;
            } else {
                System.out.println("didn't catch that, try 'start'...");
            }
        }

    }

}

