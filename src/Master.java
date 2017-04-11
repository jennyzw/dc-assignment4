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

    private Queue<iMapper> mapManagers;  // stubs to one per worker machine
    private Queue<iReducer> reduceManagers;

    private HashMap<String, iReducer> reduceTasks;  // stubs to active reduce tasks
    private Semaphore reduceTasksMutex;
    private HashMap<String, iMapper> mapTasks;  // tracks active map tasks
    private Semaphore mapTasksMutex;

    private HashMap<String, Integer> masterWordCount;  // for collecting reducer values
    private Semaphore masterWordCountMutex;

    private MapperNameGenerator nameGenerator;  // for uniquely naming mappers

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
        reduceTasksMutex = new Semaphore(1);
        mapTasks = new HashMap<>();
        mapTasksMutex = new Semaphore(1);
        masterWordCount = new HashMap<>();
        masterWordCountMutex = new Semaphore(1);

        nameGenerator = new MapperNameGenerator();

        try {

            // export self
            masterStub = (iMaster) UnicastRemoteObject.exportObject(this, 0);

            // locate stubs to all worker mapper and reducer managers
            Registry registry;
            for (String ip: workerIPs) {
                registry = LocateRegistry.getRegistry(ip, 1099);

                iMapper mapManager = (iMapper) registry.lookup("mapManager");
                mapManagers.offer(mapManager);
                System.out.println("found map manager at ip " + ip + " : " + mapManager);

                iReducer reduceManager = (iReducer) registry.lookup("reduceManager");
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
    public iReducer[] getReducers(String[] keys) throws RemoteException, AlreadyBoundException {
        iReducer[] res = new iReducer[keys.length];

        try {
            reduceTasksMutex.acquire();
            for (int i = 0; i < keys.length; i++) {
                if (!reduceTasks.containsKey(keys[i])) {
                    iReducer reduceManager = reduceManagers.poll();
                    reduceManagers.offer(reduceManager);

                    reduceTasks.put(keys[i], reduceManager.createReduceTask(keys[i], masterStub));
                }
                res[i] = reduceTasks.get(keys[i]);
            }
            reduceTasksMutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public void markMapperDone(String name) throws RemoteException {
        try {
            mapTasksMutex.acquire();
            mapTasks.remove(name);
            System.out.println("mapper " + name + " is done");

            if (mapTasks.isEmpty()) {
                System.out.println("reached here");
                reduceTasksMutex.acquire();
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
                    }).start();

                }
                reduceTasksMutex.release();
            }
            mapTasksMutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveOutput(String key, int value) throws RemoteException {
        System.out.println("output: " + key + " - " + value);
        try {
            masterWordCountMutex.acquire();
            masterWordCount.put(key, value);
            masterWordCountMutex.release();

            reduceTasksMutex.acquire();
            reduceTasks.remove(key);

            if (reduceTasks.isEmpty()) {
                writeToFile();
            }
            reduceTasksMutex.release();

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
        assert(target.exists());

        System.out.println("found " + filepath + ", commencing count...");

        try {
            Scanner reader = new Scanner(target);
            iMapper mapManager;

            while (reader.hasNextLine()) {
                mapManager = mapManagers.poll();
                mapManagers.offer(mapManager);

                String mapperName = nameGenerator.next();
                final String line = reader.nextLine();
                System.out.println(line);

                try {
                    final iMapper mapTask = mapManager.createMapTask(mapperName);
                    mapTasks.put(mapperName, mapTask);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mapTask.processInput(line, masterStub);
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
                "127.0.0.1",
        };

        System.out.println("Initializing and connecting master");
        Master master = new Master(workerIPs);


        System.out.println("type 'start' to commence MapReduce");
        Scanner scan = new Scanner(System.in);
        while (true) {
            // TODO change this back
//            if (scan.nextLine().equals("start")) {
            if (true) {
                master.wordCountFile("dummy.text");
                break;
            } else {
                System.out.println("didn't catch that, try 'start'...");
            }
        }

    }

}
