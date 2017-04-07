/**
 * Created by duncan on 4/6/17.
 */
public class MapReduceManagers {
    public static void main(String[] args) {
        new MapTask("mapManager", true);
        new ReduceTask(true);
    }
}
