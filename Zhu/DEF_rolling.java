package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class DEF_rolling {
    public final Instance instance;
    public final String fileName;

    public DEF_rolling(Instance i, String fileName) throws IloException, IOException {
        this.instance = i;
        this.fileName = fileName;

        doTheRolling();
    }

    private void doTheRolling() throws IloException, IOException {
        int iterations = 0;
        try (FileWriter writer = new FileWriter(fileName, true)) {
            //initiliaze
            instance.setKesiandStartAge(new int[instance.n]);
            writer.write("\nStartAges="+Arrays.toString(instance.startAges));
            writer.write("\nKesi="+Arrays.toString(instance.kesi));
            Omega omega = new Omega(instance);
            DEF def = new DEF(instance, omega, "HELOOO.lp");
            def.setupAndSolve();
            int[] newStartAges = def.returnNewStartAges();
            writer.write("\nIteration number "+iterations);
            writer.write("\nx: "+Arrays.toString(def.get_x_i()));
            writer.write("\ncosts: "+def.getObjValue());
            writer.write("\ng: "+Arrays.toString(newStartAges));
            writer.flush();
            def.cleanup();

            while (iterations < 50) {

                instance.setKesiandStartAge(newStartAges);
                omega = new Omega(instance);
                def = new DEF(instance, omega, "HELOOO.lp");
                def.setupAndSolve();
                newStartAges = def.returnNewStartAges();
                writer.write("\nIteration number "+iterations);
                writer.write("\nx: "+Arrays.toString(def.get_x_i()));
                writer.write("\ncosts: "+def.getObjValue());
                writer.write("\ng: "+Arrays.toString(newStartAges));
                writer.flush();

                def.cleanup();

                iterations++;
            }
        }
        catch (IOException e) {
            System.err.println("An error occurred while writing the LP file: " + e.getMessage());
        }
    }
}
