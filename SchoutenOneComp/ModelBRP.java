package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * This class models the p-BRP model for one component. It returns the resulting policy in a file under the name fileName.
 * This class is extended by p-ARP and p-MBRP as these models are modifications on this model.
 *
 * @author 619034dr Donna van der Rijst
 */
public class ModelBRP {
    public final Instance i;
    public final int M;
    public final int m;
    public final int N;
    public final int[] I0;
    public final int[] I1;
    public static IloCplex cplex;
    public final int[] A = new int[]{0,1};
    public static IloNumVar[][][] x;
    public static IloNumVar[] y;

    /**
     * Constructor for the ModelBRP class
     * @param i Instance containing all important parameters
     * @param fileName to write output and found policy in.
     * @throws IloException as we work with CPLEX.
     * @throws IOException as we work with an input file. This might raise errors.
     */
    public ModelBRP(Instance i, String fileName) throws IloException, IOException {
        this.i = i;
        this.M = i.M;
        this.m = i.m;
        this.N = i.N;
        this.I0 = i.I0;
        this.I1 = i.I1;
        cplex = new IloCplex();

        setVariables();
        setObjective();
        setConstraints();

        printSolution();
        writePolicy(fileName);
    }

    /**
     * This method solves the model, prints the costs and X, and puts the CPLEX model in a .lp file.
     * @throws IloException as we are working with a CPLEX model.
     */
    public void printSolution() throws IloException {
        cplex.exportModel("model1CompBRP.lp");
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Costs over period N are " + averageCosts*N);

        printX();
    }

    /**
     * This method write the policy in .txt file called fileName. The policy is found by looking at where the
     * values of x(i0,i1,a) for a certain 'a' are larger than zero. If so, this action 'a' is chosen and is the optimal R_(i0,i1).
     * @param fileName policy is writting in this file.
     * @throws IOException file might not be found, or other issues around this file might arise.
     * @throws IloException as we are working with a CPLEX model.
     */
    public void writePolicy(String fileName) throws IOException, IloException {
        FileWriter writer = new FileWriter(new File(fileName), true);
        double yearlyCosts = cplex.getObjValue() * N;
        writer.write("\n\nThe class "+getClass().getSimpleName()+" is used.\n");
        writer.write("Yearly costs are " + yearlyCosts);
        writer.write("\n\ni0 i1 a");

        int[][] a_i0_i1 = new int[I0.length][I1.length];
        for (int i0 : I0) {
            for (int i1 : I1) {
                a_i0_i1[i0][i1] = 4;
                for (int a : getPossibleActions(i0, i1)) {
                    if (cplex.getValue(x[i0][i1][a]) > 0.00000000000) {
                        a_i0_i1[i0][i1] = a;
                    }
                }
            }
        }

//        System.out.println("rows i0, columns i1, actions: ");
//        for (int i0 : I0) {
//            for (int i1 : I1) {
//                if(a_i0_i1[i0][i1]!=4){System.out.print(a_i0_i1[i0][i1]+"\t");}
//                else{
//                    System.out.print("-\t");
//                }
//            }
//            System.out.println();
//        }

        for (int i0 : I0) {
            for (int i1 : I1) {
                if(a_i0_i1[i0][i1]!=4){writer.write("\n" + (i0+1) + " " + i1 + " " + a_i0_i1[i0][i1]);}
            }
        }
        writer.close();
    }

    /**
     * This method prints the values vor x(i0,i1,a). This method might be useful for retrieving insights.
     * @throws IloException as we are working with a CPLEX model.
     */
    public void printX() throws IloException {
        System.out.println("\n actions grid - rows are age, columns time.");
        for (int i0 : I0) {
            for(int i1 : I1) {
                int[] actions = getPossibleActions(i0,i1);
                boolean notPrinted = true;
                for(int a : actions){
                    if(cplex.getValue(x[i0][i1][a])>0.00000000){
                        System.out.printf("%10s", a);
                        notPrinted = false;
                    }
                }
                if(notPrinted){
                    System.out.printf("%10s", "-");
                }
            }
            System.out.println();
        }
    }

    /**
     * Set the constraints in our model. It is done like this, so it is easily overwritten by the classes ModelARP and ModelMBRP.
     * @throws IloException as we are working with a CPLEX model.
     */
    public void setConstraints() throws IloException {
        constraint10b();
        constraint10cd();
        constraint10e();
    }

    /**
     * Set the objective in our model.
     * @throws IloException as we are working with a CPLEX model.
     */
    private void setObjective() throws IloException {
        IloNumExpr sum = cplex.constant(0.0);

        for (int i0 : I0) {
            for (int i1 : I1) {
                int[] actions = getPossibleActions(i0,i1);
                for(int a : actions){
                    IloNumExpr prod = cplex.prod(x[i0][i1][a], getCost(i0,i1,a));
                    sum = cplex.sum(sum,prod);
                }
            }
        }
        cplex.addMinimize(sum);
    }

    /**
     * Set the variables in our model. It is done like this, so it is easily overwritten by the classes ModelARP and ModelMBRP.
     *  @throws IloException as we are working with a CPLEX model.
     */
    public void setVariables() throws IloException {
        setVarX();
        setVarY();
    }

    public void setVarY() throws IloException{
        y = new IloNumVar[I0.length];
        for (int i0 : I0) {
            IloNumVar var = cplex.intVar(0,1,"y("+i0+")");
            y[i0] = var;
        }
    }

    public void setVarX() throws IloException{
        x = new IloNumVar[I0.length][I1.length][A.length];
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int a : A){
                    IloNumVar var = cplex.numVar(0.0, 1.0 / (m * N), "x(" + i0 + "," + i1 + "," + a + ")"); //set the limit of x to 1/mN. This is actually positive infinity but this might save storage?
                    x[i0][i1][a] = var;
                }
            }
        }
    }

    /**
     * Not all actions are possible for certain i1 values. This method returns the possible actions.
     * @param i0 period in state i
     * @param i1 age of component in state i
     * @return possible actions, in int[] array.
     */
    public int[] getPossibleActions(int i0, int i1){
        int[] actions;
        if(i1 == 0 || i1 == M ){
            actions = new int[]{1};
        }
        else{
            actions = new int[]{0,1};
        }
        return actions;
    }

    /**
     * We are working with period dependent costs, and with CM and PM costs. This method return the
     * correct costs when inserting state i=(i0,i1) and action a.
     * @param i0 period in state i
     * @param i1 age of component in state i
     * @param a action done in state i
     * @return cost that will be incurred.
     */
    private double getCost(int i0, int i1, int a){
        double c = 0.0;
        i0 = i0%N;
        if(a==0){
            c = 0.0;
        }
        else if(a==1){
            if(i1!=0) {
                c = i.cPR_i_t[0][i0];
            }
            else {
                c = i.cCR_i_t[0][i0];
            }
        }
        return c;
    }

    public void constraint10e() throws IloException{
        for(int i0 : I0){
            IloNumExpr sum = cplex.constant(0.0);
            for(int i1 : I1){
                int[] action = getPossibleActions(i0,i1);
                for(int a : action){
                    sum = cplex.sum(sum,x[i0][i1][a]);
                }
            }
            cplex.addEq(sum, 1.0/(m*N), "10e,"+i0);
        }
    }
    public void constraint10cd() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                if(i1!=0 & i1!=M){
                    cplex.addLe(cplex.sum( x[i0][i1][0],y[i0]), 1.0,"10c,"+i0+","+i1);

                    cplex.addLe(cplex.diff(x[i0][i1][1],y[i0]), 0.0,"10d,"+i0+","+i1);
                }
            }
        }
    }
    public void constraint10b() throws IloException {
        for (int i0 : I0) {
            for (int i1 : I1) {

                IloNumExpr sum1 = cplex.constant(0.0);
                int[] actions = getPossibleActions(i0, i1);
                for (int a : actions) {
                    sum1 = cplex.sum(sum1, x[i0][i1][a]);
                }

                IloNumExpr sum2 = cplex.constant(0.0);
                for (int j0 : I0) {
                    for (int j1 : I1) {
                        int[] actionsJ = getPossibleActions(j0, j1);
                        for (int a : actionsJ) {
                            sum2 = cplex.sum(sum2, cplex.prod(i.piOneDim(j0, j1, i0, i1, a), x[j0][j1][a]));
                        }
                    }
                }
                cplex.addEq(cplex.diff(sum1, sum2), 0.0, "10b," + i0 + "," + i1);
            }
        }
    }

}
