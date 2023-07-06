package SchoutenOneComp;
import Main.Instance;
import ilog.concert.IloException;

import java.io.File;
import java.io.IOException;

public class MainS1C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        ModelARP arp = new ModelARP(instance, "arpTest.txt");

        Policy policy = new Policy(new File("arpTest.txt"), instance);
        delta delta = new delta(policy, instance);

       

        System.out.println("x=17 " +delta.getdelta(0, 1, 17 )+"\n");
        System.out.println("x=18 " +delta.getdelta(0, 1, 18 )+"\n");
        System.out.println("x=19 " +delta.getdelta(0, 1, 19 )+"\n");
        System.out.println("x=20 " +delta.getdelta(0, 1, 20 )+"\n");
        System.out.println("x=21 " +delta.getdelta(0, 1, 21 )+"\n");
        System.out.println("x=22 " +delta.getdelta(0, 1, 22 )+"\n");
        System.out.println("x=23 " +delta.getdelta(0, 1, 23 )+"\n");

        System.out.println("\n\n\n\n\n\ni0=5");


        System.out.println("x=17 " +delta.getdelta(5, 1, 17 )+"\n");
        System.out.println("x=18 " +delta.getdelta(5, 1, 18 )+"\n");
        System.out.println("x=19 " +delta.getdelta(5, 1, 19 )+"\n");
        System.out.println("x=20 " +delta.getdelta(5, 1, 20 )+"\n");
        System.out.println("x=21 " +delta.getdelta(5, 1, 21 )+"\n");
        System.out.println("x=22 " +delta.getdelta(5, 1, 22 )+"\n");
        System.out.println("x=23 " +delta.getdelta(5, 1, 23 )+"\n");

    }
}
