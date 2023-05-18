package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.CpxException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

public class ModelMBRP_2comp {
    public final Instance i;
    public final int M;
    public final int m;
    public final int N;
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final int[] K;
    public static IloCplex cplex;
    public static List<IloConstraint> constraints = new ArrayList<>();
    public final int[] A = new int[]{0, 1, 2, 3};  //there are 4 actions: a={0,1,2,3}
    public static IloNumVar[][][][] x;
    public static IloNumVar[][] y;
    public static IloNumVar[][][] z;
    public static IloNumVar[][] t;

    public ModelMBRP_2comp(Instance i) throws IloException {
        cplex = new IloCplex();
        this.i = i;
        M = i.M;
        m = i.m;
        N = i.N;
        I0 = i.I0;
        I1 = i.I1;
        I2 = i.I2;
        K = i.K;

        setVariables();
        setObjective();
        setConstraints();
        cplex.setParam(IloCplex.Param.DetTimeLimit, 60000);
        cplex.exportModel("model.lp");
//        cplex.setOut(null);
        cplex.solve();

        IloConstraint[] constrs = new IloConstraint[constraints.size()];
        double[] doubless = new double[constraints.size()];
        int count = 0;
        for (IloConstraint cons : constraints) {
            constrs[count] = cons;
            doubless[count] = count;
        }
        for (IloConstraint con : constrs) {
            try {
                IloCplex.ConflictStatus conflictStatus = cplex.getConflict(con);
                System.out.println(conflictStatus);
            } catch (CpxException e) {
            }
        }
        printSolution();
    }

    public void printSolution() throws IloException {
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts * N);

        double sum = 0.0;
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0, i1, i2);
                    for (int a : actions) {
                        sum = sum + cplex.getValue(x[i0][i1][i2][a]) * cComponentOne(i0, i1, i2, a);
                    }
                }
            }
        }
        System.out.println("Yearly cost for only the first component are " + sum * N);
        printX();
        printYTZ();
        printActionGridComp1();

    }

    public void printActionGridComp1() throws IloException {
        System.out.println("\n actions grid - rows are age, columns time.");

        for (int i1 : I1) {
            for (int i0 : I0) {
                boolean notPrinted = true;
                List<Integer> list = new ArrayList<>();
                for (int i2 : I2) {
                    int[] actions = A(i0, i1, i2);
                    for (int a : actions) {
                        if (cplex.getValue(x[i0][i1][i2][a]) > 0.00000000) {
                            list.add(a);
                            notPrinted = false;
                        }
                    }
                }
                if (!notPrinted) {
                    System.out.printf("%10s", list.get(0));
                }
                if (notPrinted) {
                    System.out.printf("%10s", "-");
                }

            }
            System.out.println();
        }
    }

    public void printYTZ() throws IloException {
        for (int k : K) {
            System.out.println("\n\nk=" + k);

            System.out.println();
            System.out.printf("%8s", "i0");
            for (int i0 : I0) {
                System.out.printf("%8s", i0);
            }

            System.out.println();
            System.out.printf("%8s", "y");
            for (int i0 : I0) {
                System.out.printf("%8.0f", Math.abs(cplex.getValue(y[k][i0])));
            }
            System.out.println();
            System.out.printf("%8s", "t");
            for (int i0 : I0) {
                System.out.printf("%8.0f", Math.abs(cplex.getValue(t[k][i0])));
            }

            System.out.println();
            for (int ik : I1) {
                System.out.printf("%8s", "z_" + ik);
                for (int i0 : I0) {
                    System.out.printf("%8.0f", Math.abs(cplex.getValue(z[k][i0][ik])));
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    public void printX() throws IloException {
        System.out.println("\n actions grid - rows are age, columns time.");
        for (int i0 : I0) {
            System.out.println("\n\n i0=" + i0);
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0, i1, i2);
                    boolean notPrinted = true;
                    for (int a : actions) {
                        if (cplex.getValue(x[i0][i1][i2][a]) > 0.0) {
                            System.out.printf("%8s", a);
                            notPrinted = false;
                        }
                    }
                    if (notPrinted) {
                        System.out.printf("%8s", "-");
                    }
                }
                System.out.println();
            }
        }


        System.out.println("Average time spend in each age state for i2 :");
        for(int i2 : I2){
            double sum = 0.0;
            for(int i1 : I1){
                for(int i0 :I0){
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        sum = sum + cplex.getValue(x[i0][i1][i2][a]);
                    }
                }
            }
            System.out.println("i2="+i2+" = "+sum);
        }
    }


    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
        setConstraint9defg();
        setConstraint9hi();
        setConstraint9jk();
        setConstraint9lm(); //without this, it becomes ARP
    }

    public void setObjective() throws IloException {
        System.out.println("setting objective");

        IloNumExpr sum = cplex.constant(0.0);

        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0, i1, i2);
                    for (int a : actions) {
                        if(a==1 || a==3 || a==2) {
                            IloNumExpr prod = cplex.prod(x[i0][i1][i2][a], c(i0, i1, i2, a));
                            sum = cplex.sum(sum, prod);
                        }
                    }

                }
            }
        }
        cplex.addMinimize(sum);
    }

    /**
     * creates the variables  x[i0][i1][i2][a], y[k][i0],  z[k][i0][ik] and t[k][i0] for in our cplex model.
     *
     * @throws IloException
     */
    public void setVariables() throws IloException {
        // x[i0][i1][i2][a]
        x = new IloNumVar[I0.length][I1.length][I2.length][A.length];
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
//                    int[] actions = A(i0,i1,i2);
//                    for (int a : actions) {
                    for (int a : A) {
                        IloNumVar var = cplex.numVar(0.0, 1.0 / (m * N), "x(" + i0 + "," + i1 + "," + i2 + "," + a + ")"); //set the limit of x to 1/mN. This is actually positive infinity but this might save storage?
                        x[i0][i1][i2][a] = var;
                    }
                }
            }
        }

        // y[k][i0]
        y = new IloNumVar[K.length + 1][I0.length]; //we do 3 here, so that we can use k=1 and k=2. To keep this clear
        for (int k : K) {
            for (int i0 : I0) {
                IloNumVar var = cplex.intVar(0, 1, "y(" + k + "," + i0 + ")");
                y[k][i0] = var;
            }
        }

        // z[k][i0][ik]
        z = new IloNumVar[K.length + 1][I0.length][I1.length];
        for (int k : K) {
            for (int i0 : I0) {
                for (int ik : I1) {
                    IloNumVar var = cplex.intVar(0, 1, "z(" + k + "," + i0 + "," + ik + ")");
                    z[k][i0][ik] = var;
                }
            }
        }

        // t[k][i0]
        t = new IloNumVar[K.length + 1][I0.length]; //we do 3 here, so that we can use k=1 and k=2. To keep this clear
        for (int k : K) {
            for (int i0 : I0) {
                IloNumVar var = cplex.intVar(0, m * N , "t(" + k + "," + i0 + ")"); //m*N+1 seems like a reasonable upperbound.
                t[k][i0] = var;
            }
        }
    }

    /**
     * return the array int[] A. These are the action points that are possible, given the values of i0, i1, i2 which are the
     * time, age component 1 and age component 2 respectively.
     *
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @return possible action points
     */
    public int[] A(int i0, int i1, int i2) {
        int[] A;
        if ((i1 == 0 || i1 == M) & (i2 == 0 || i2 == M)) {
            A = new int[]{3};
        } else if ((i1 == 0 || i1 == M) & (i2 != 0 & i2 != M)) {
            A = new int[]{1, 3};
        } else if ((i2 == 0 || i2 == M) & (i1 != 0 & i1 != M)) {
            A = new int[]{2, 3};
        } else {
            A = new int[]{0, 1, 2, 3};
        }
//         System.out.println("for i1="+i1+", i2="+i2+" we find array"+ Arrays.toString(A));
        return A;
    }

    /**
     * the value for pi(). See the corresponding thesis-report to find this in an easier-to-read format.
     *
     * @param i0 time current
     * @param i1 age component 1
     * @param i2 age component 2
     * @param j0 time next step
     * @param j1 age component 1 next step
     * @param j2 age component 2 next step
     * @param a  action taken
     * @return value of pi
     */
    public double pi(int i0, int i1, int i2, int j0, int j1, int j2, int a) {
        double pi_value = 0.0;

        if (j0 != (i0 + 1) % (m * N)) {
        } else if (a == 0) {
            if ((j1 == (i1 + 1)) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * (1.0 - i.probCondX_x_k(i2, 2));
            } else if ((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * i.probCondX_x_k(i2, 2);
            } else if ((j1 == 0) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * (1.0 - i.probCondX_x_k(i2, 2));
            } else if ((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * i.probCondX_x_k(i2, 2);
            } else {
                pi_value = 0.0;
            }
        } else if (a == 1) {
            if ((j1 == 1) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(0, 1)) * (1.0 - i.probCondX_x_k(i2, 2));
            } else if ((j1 == 1) & (j2 == 0) & (i2 != 0) & (i2 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(0, 1)) * i.probCondX_x_k(i2, 2);
            } else if ((j1 == 0) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)) {
                pi_value = i.probCondX_x_k(0, 1) * (1.0 - i.probCondX_x_k(i2, 2));
            } else if ((j1 == 0) & (j2 == 0) & (i2 != 0) & (i2 != M)) {
                pi_value = i.probCondX_x_k(0, 1) * i.probCondX_x_k(i2, 2);
            } else {
                pi_value = 0.0;
            }
        } else if (a == 2) {
            if ((j1 == (i1 + 1)) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * i.probCondX_x_k(0, 2);
            } else if ((j1 == 0) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * i.probCondX_x_k(0, 2);
            } else {
                pi_value = 0.0;
            }
        } else if (a == 3) {
            if ((j1 == 1) & (j2 == 1)) {

                pi_value = (1.0 - i.probCondX_x_k(0, 1)) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == 1) & (j2 == 0)) {
                pi_value = (1.0 - i.probCondX_x_k(0, 1)) * i.probCondX_x_k(0, 2);
            } else if ((j1 == 0) & (j2 == 1)) {
                pi_value = i.probCondX_x_k(0, 1) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == 0) & (j2 == 0)) {
//                double one = i.probCondX_x_k(0, 1);
//                double two = i.probCondX_x_k(0, 2);
                pi_value = i.probCondX_x_k(0, 1) * i.probCondX_x_k(0, 2);
            } else {
                pi_value = 0.0;
            }
        } else {
            System.out.println("This value for a does not exist");
        }
        return pi_value;
    }

    public void piPrint() {
        //for printing
        double[][][][][][][] piPrint = new double[I0.length][I1.length][I2.length][I0.length][I1.length][I2.length][4];
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    for (int j1 : I1) {
                        for (int j2 : I2) {
                            for (int a : A) {
                                int j0 = i0 + 1;
                                piPrint[i0][i1][i2][j0][j1][j2][a] = pi(i0, i1, i2, j0, j1, j2, a);
                                System.out.println(piPrint[i0][i1][i2][j0][j1][j2][a]);
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Costs for a certain time in the year, for ages i1 and i2 for the two components, and for action a.
     *
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @param a  action taken
     * @return value of the costs
     */
    public double c(int i0, int i1, int i2, int a) {
        double c = 0.0;
        i0 = i0 % N;
        if (a == 0) {
            if (i2 == 0 || i1 == 0) {
                System.out.println("this can not be possible");
                System.exit(1);
            }
            c = 0.0;
        } else if (a == 1) {
            if (i2 == 0) {
                System.out.println("this can not be possible");
                System.exit(1);
            } else if (i1 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i1 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        } else if (a == 2) {
            if (i1 == 0) {
                System.out.println("this can not be possible");
                System.exit(1);
            }
            else if (i2 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i2 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        } else if (a == 3) {
            if (i1 != 0 & i2 != 0) {
                c = 2 * i.cPR_i[i0] + i.d;
            } else if (i1 == 0 & i2 != 0) {
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            } else if (i1 != 0 & i2 == 0) {
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            } else if (i1 == 0 & i2 == 0) {
                c = 2 * i.cCR_i[i0] + i.d;
            }
        } else {
            System.out.println("This value for a does not exist");
        }
        return c;
    }

    public double cComponentOne(int i0, int i1, int i2, int a) {
        double c = 0.0;
        i0 = i0 % N;
        if (a == 0) {
            if (i2 == 0 || i1 == 0) {
                System.out.println("this can not be possible");
                System.exit(1);
            }
            c = 0.0;
        } else if (a == 1) {
            if (i2 == 0) {
                System.out.println("this can not be possible");
                System.exit(1);
            } else if (i1 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i1 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        } else if (a == 2) {
            c = 0.0;
        } else if (a == 3) {
            if (i1 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i1 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        } else {
            System.out.println("This value for a does not exist");
        }
        return c;
    }

    public void setConstraint9b() throws IloException {
        for (int i0 : I0) {
//            System.out.println(i0);
            for (int i1 : I1) {
                for (int i2 : I2) {

                    IloNumExpr sum1 = cplex.constant(0.0);
                    int[] actions = A(i0, i1, i2);
                    for (int a : actions) {
                        sum1 = cplex.sum(sum1, x[i0][i1][i2][a]);
                    }

                    IloNumExpr sum2 = cplex.constant(0.0);
                    for (int j0 : I0) {
                        for (int j1 : I1) {
                            for (int j2 : I2) {
                                int[] actionsJ = A(j0, j1, j2);
                                for (int a : actionsJ) {
                                    {
                                        sum2 = cplex.sum(sum2, cplex.prod(pi(j0, j1, j2, i0, i1, i2, a), x[j0][j1][j2][a]));
                                    }
                                }
                            }
                        }
                    }
                    constraints.add(cplex.addEq(cplex.diff(sum1, sum2), 0.0, "9b," + i0 + "," + i1 + "," + i2));
                }
            }
        }
    }

    public void setConstraint9c() throws IloException {
        for (int i0 : I0) {
            IloNumExpr sum = cplex.constant(0.0);
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0, i1, i2);
                    for (int a : actions) {
                        sum = cplex.sum(sum, x[i0][i1][i2][a]);
                    }
                }
            }
            double fraction = 1.0 / (m * N);
            constraints.add(cplex.addEq(sum, fraction, "9c," + i0));
        }
    }

    public void setConstraint9defg() throws IloException {
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    //constraint 9d
                    if (i1 != 0) {
                        cplex.addLe(cplex.diff(x[i0][i1][i2][1], z[1][i0][i1]), 0.0, "9e" + i0 + "," + i1 + "," + i2 + ",k=" + 1 + "a=1");
                        cplex.addLe(cplex.diff(x[i0][i1][i2][3], z[1][i0][i1]), 0.0, "9g" + i0 + "," + i1 + "," + i2 + ",k=" + 1 + "a=3");
                    }
                    if (i2 != 0) {
                        cplex.addLe(cplex.diff(x[i0][i1][i2][2], z[2][i0][i2]), 0.0, "9e" + i0 + "," + i1 + "," + i2 + ",k=" + 2 + "a=1");
                        cplex.addLe(cplex.diff(x[i0][i1][i2][3], z[2][i0][i2]), 0.0, "9g" + i0 + "," + i1 + "," + i2 + ",k=" + 2 + "a=3");
                    }
                    cplex.addLe(cplex.sum(x[i0][i1][i2][0], z[1][i0][i1]), 1.0, "9d" + i0 + "," + i1 + "," + i2 + ",k=" + 1 + "a=0");
                    cplex.addLe(cplex.sum(x[i0][i1][i2][0], z[2][i0][i1]), 1.0, "9d" + i0 + "," + i1 + "," + i2 + ",k=" + 2 + "a=0");
//
//                        //constraint 9e en 9f
                    cplex.addLe(cplex.sum(x[i0][i1][i2][1], z[2][i0][i2]), 1.0, "9f" + i0 + "," + i1 + "," + i2 + ",k=" + 1 + "a=1");
                    cplex.addLe(cplex.sum(x[i0][i1][i2][2], z[1][i0][i1]), 1.0, "9f" + i0 + "," + i1 + "," + i2 + ",k=" + 2 + "a=1");
                }
            }
        }
    }

    public void setConstraint9hi() throws IloException {
        for (int i0 : I0) {
            for (int j0 : I0) {
                for (int k : K) {
                    if (j0 < i0) {
                        constraints.add(cplex.addLe(cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0]), cplex.prod(m * N, y[k][j0])),  m * N + i0, "9h"));
                    }
                    if (j0 > i0) {
                        constraints.add(cplex.addLe(cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0])), m * N + i0, "9i"));
                    }
                }
            }
        }
    }
    public void setConstraint9jk() throws IloException{
        for (int i0 : I0){
            for (int k : K) {
                for (int ik : I1) {
                    constraints.add(cplex.addLe(cplex.diff(z[k][i0][ik], y[k][i0]), 0.0, "9j"));
                    for (int jk : I1) {
                        if (jk > ik) {
                            constraints.add(cplex.addLe(cplex.diff(z[k][i0][ik], z[k][i0][jk]), 0.0, "9k"));
                        }
                    }
                }
            }
        }
    }

    public void setConstraint9lm() throws IloException{
        for(int k : K) {
            for (int i0 : I0) {
                for (int ik : I1) {
                    constraints.add(cplex.addLe(cplex.sum(cplex.prod(M, y[k][i0]), cplex.prod(-1.0 * M, z[k][i0][ik]), cplex.prod(-1.0, t[k][i0])), M - 1.0 - ik, "9l_1"));
                    constraints.add(cplex.addLe(cplex.sum(cplex.prod(M,z[k][i0][ik]),t[k][i0]),M+ik));
                }
            }
        }
    }
}