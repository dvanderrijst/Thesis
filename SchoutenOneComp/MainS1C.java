package SchoutenOneComp;
import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class MainS1C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();
        ModelARP arp = new ModelARP(instance);

    }
}
