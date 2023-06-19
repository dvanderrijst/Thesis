package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MainS2C {
    public static void main(String[] args) throws IloException, IOException {

        Instance instance = new Instance();
        String fileName = "policySchoutenARP.txt";
        writeInfo(instance, fileName);

        //Schouten
        ModelARP_2Comp ARP2comp = new ModelARP_2Comp(instance, fileName);

//        ModelBRP_2Comp modelarp2 = new ModelBRP_2Comp(instance, fileName);
//        ModelMBRP_2Comp modelarp2 = new ModelMBRP_2Comp(instance, fileName);
    }

    private static void writeInfo(Instance instance, String fileName) {
        //document what is done
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("\nm="+instance.m);
            writer.write("\nN="+instance.N);
            writer.write("\nT=mN="+instance.T);
            writer.write("\nn="+instance.n);
            writer.write("\nq="+instance.q);
            writer.write("\nlengthOmega="+instance.lengthOmega);
            writer.write("\nd="+instance.d);
            writer.write("\nCR_average_i="+ Arrays.toString(instance.CR_average));
            writer.write("\nPR_average_i="+ Arrays.toString(instance.PR_average));
            writer.write("\nalpha="+ Arrays.toString(instance.alpha));
            writer.write("\nbeta="+ Arrays.toString(instance.beta));
            writer.write("\ndelta="+instance.delta);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
