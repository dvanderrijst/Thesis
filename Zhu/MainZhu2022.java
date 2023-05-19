package Zhu;

import Main.Instance;
import ilog.concert.IloException;

public class MainZhu2022 {
    public static void main(String[] args) throws IloException {
        System.out.println("Hello donnie");
        Instance instance = new Instance();
        Omega omega = new Omega(instance);

        PHA pha = new PHA(omega, instance);

    }
}
