package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.stream.IntStream;

public class ModelMBRP extends ModelBRP{
    private static IloNumVar[][] z;
    private static IloNumVar[] t;

    public ModelMBRP(Instance i) throws IloException {
        super(i);
    }

    @Override
    public void setVariables() throws IloException {
        setVarX();
        setVarY();
        setVarZ();
        setVarT();
    }

    @Override
    public void setConstraints() throws IloException{
        constraint10b(); //12b
//        constraint10cd(); //this is actually not in the paper but x needs to be connected to y or z so lets see what happens if we take these.
        constraint10e(); //12c

        constraint12de();
        constraint12fg();
        constraint12hi();

        constraintCremers();
    }

    private void constraintCremers() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.sum(x[i0][i1][0], z[i0][i1]), 1.0);
                if(i1 != 0 & i1 != M){
                    cplex.addLe(cplex.diff(x[i0][i1][1], z[i0][i1]), 0.0);
                }
            }
        }
    }

    @Override
    public void printSolution() throws IloException {
        cplex.exportModel("model1CompMBRP.lp");
//        cplex.setOut(null);
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        for(int i0 : I0){
            System.out.printf("%10.0f", cplex.getValue(y[i0]));
        }

        printX();

        System.out.println("\n z values:");
        for(int i1 : I1) {
            for (int i0 : I0) {
                System.out.printf("%10.0f", Math.abs(cplex.getValue(z[i0][i1])));
            }
            System.out.println();
        }

        System.out.println("\n t values:");
        for (int i0 : I0) {
            System.out.printf("%10.0f", cplex.getValue(t[i0]));}
    }

    private void constraint12hi() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.sum(cplex.prod(M,y[i0]),cplex.prod(-1.0*M,z[i0][i1]), cplex.prod(-1.0,t[i0])), M-1-i1);
                cplex.addLe(cplex.sum(cplex.prod(M,z[i0][i1]),t[i0]),M+i1);
            }
        }
    }

    private void constraint12fg() throws IloException{
        for(int i0 : I0){
            for(int j0 : I0){
                if(j0<i0){
                    cplex.addLe(cplex.sum(t[i0],cplex.prod(j0,y[j0]),cplex.prod(m*N,y[j0])),m*N + i0);
                }
            }
        }
        for(int i0 : I0){
            for(int j0 : I0){
                if(j0>i0){
                    cplex.addLe(cplex.sum(t[i0],cplex.prod(j0,y[j0])), m*N+i0);
                }
            }
        }
    }

    private void constraint12de() throws IloException{
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.diff(z[i0][i1],y[i0]),0.0, "12d("+i0+","+i1+")");

                for(int j1 : I1){
                    if(i1 < j1){
                        cplex.addLe(cplex.diff(z[i0][i1], z[i0][j1]), 0.0, "12e("+i0+","+i1+","+j1+")");
                    }
                }
            }
        }
    }

    private void setVarZ() throws IloException{
        z = new IloNumVar[I0.length][I1.length];
        for (int i0 : I0) {
            for(int i1 : I1){
                IloNumVar var = cplex.intVar(0,1,"z("+i0+","+i1+")");
                z[i0][i1] = var;
            }
        }
    }
    private void setVarT() throws IloException{
        t = new IloNumVar[I0.length];
        for(int i0 : I0){
            IloNumVar var = cplex.intVar(0,m*N, "t("+i0+")");
            t[i0] = var;
        }
    }
}