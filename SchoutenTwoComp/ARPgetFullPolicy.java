package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class ARPgetFullPolicy {
    public final Instance instance;
    public final int[][][] actions;
    public static boolean actionsNotFulfilled;
    public final String folderName;

    public ARPgetFullPolicy(Instance instance, String folderName) throws IOException, IloException {
        this.instance = instance;
        this.folderName = folderName;
        actionsNotFulfilled = true;

        ModelARP ARP2comp = new ModelARP(instance, "removeMe.txt");
        ARP2comp.doStart();
        actions = ARP2comp.actions;
        printActionGrid();

        while(actionsNotFulfilled){
            String fileName2 = folderName+"/policies_manipulated.txt";
            instance.writeInfo(fileName2);

            ModelARP_warmstart getNewPolicy = new ModelARP_warmstart(instance, fileName2, ARP2comp.actions);
            getNewPolicy.doStart();
            int[][][] newActions = getNewPolicy.actions;

            fillActions(newActions);
            printActionGrid();

            String fileNameFinal = folderName+"/policies_true.txt";
            try (FileWriter writer = new FileWriter(fileNameFinal, true)) {
                writer.write("i1 i2 i0 a\n");
                for (int i0 : instance.I0) {
                    for (int i1 : instance.I1) {
                        for (int i2 : instance.I2) {
                            if(actions[i0][i1][i2]!=4){writer.write(i1+" "+i2+" "+(i0+1)+" "+actions[i0][i1][i2]+"\n");}
                        }
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            instance.writeInfo(fileNameFinal);

        }

        String fileNameFinal = folderName+"/policies_trueFinal.txt";
        try (FileWriter writer = new FileWriter(fileNameFinal, true)) {
            writer.write("i1 i2 i0 a\n");
            for (int i0 : instance.I0) {
                for (int i1 : instance.I1) {
                    for (int i2 : instance.I2) {
                        if(actions[i0][i1][i2]!=4){writer.write(i1+" "+i2+" "+(i0+1)+" "+actions[i0][i1][i2]+"\n");}
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.writeInfo(fileNameFinal);
    }

    private void fillActions(int[][][] newActions) {
        actionsNotFulfilled = false;
        for (int i0 : instance.I0) {
            for (int i1 : instance.I1) {
                for (int i2 : instance.I2) {
                    if(actions[i0][i1][i2]==4){
                        actions[i0][i1][i2] = newActions[i0][i1][i2];
                        if(newActions[i0][i1][i2] == 4 && i1 !=0 && i2!=0){
                            actionsNotFulfilled = true;
                        }
                    }
                }
            }
        }
    }

    private void printActionGrid(){
        int i0 = 0;
            for (int i1 : instance.I1) {
                for (int i2 : instance.I2) {
                    if (actions[i0][i1][i2]!=4) {
                        System.out.printf("%8s", actions[i0][i1][i2]);
                    }
                    else{
                        System.out.printf("%8s", "-");
                    }
                }
                System.out.println();
            }
            System.out.println("\n\nt="+(i0)+"\n");

    }
}
