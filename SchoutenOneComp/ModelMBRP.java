package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.io.IOException;
/**
 * This class models the p-MBRP for a single component. It mainly uses the methods from the p-BRP model.
 *
 * @author 619034dr Donna van der Rijst
 */
public class ModelMBRP extends ModelBRP{
    private static IloNumVar[][] z;
    private static IloNumVar[] t;

    /**
     * Constructor for the ModelMBRP class
     * @param i Instance containing all important parameters
     * @param fileName to write output and found policy in.
     * @throws IloException as we work with CPLEX.
     * @throws IOException as we work with an input file. This might raise errors.
     */
    public ModelMBRP(Instance i, String fileName) throws IloException, IOException {
        super(i, fileName);
    }

    /**
     * The p-MBRP model also introduces some extra variables which are added to the model here.
     * @throws IloException as we are working with a CPLEX model.
     */
    @Override
    public void setVariables() throws IloException {
        setVarX();
        setVarY();
        setVarZ();
        setVarT();
    }

    /**
     * This method overrides the setConstraints from ModelBRP as the p-MBRP model add some constraints.
     * These extra constraints can be found in paper of Schouten. Note that we have also added
     * the constraints that where missing. These are called constraint Cremers as she detected the
     * missing constraints first.
     * @throws IloException as we are working with a CPLEX model.
     */
    @Override
    public void setConstraints() throws IloException{
        constraint10b(); //12b
        constraint10e(); //12c

        constraint12de();
        constraint12fg();
        constraint12hi();

        constraintCremers();
    }

    /**
     * The missing constraints from the paper Schouten. These are called constraint Cremers as Roby Cremers detected the
     * missing constraints first.
     * @throws IloException as we are working with a CPLEX model.
     */
    private void constraintCremers() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.sum(x[i0][i1][0], z[i0][i1]), 1.0);
                if(i1 != 0 & i1 !=M-1){
                    cplex.addLe(cplex.diff(x[i0][i1][1], z[i0][i1]), 0.0);
                }
            }
        }
    }

    /**
     * Overrides the printSoltution as we want to put the cplex .lp file under a different name.
     * @throws IloException as we work with a CPLEX model
     */
    @Override
    public void printSolution() throws IloException {
        cplex.exportModel("model1CompMBRP.lp");
//        cplex.setOut(null);
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        printX();
    }

    private void constraint12hi() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.sum(cplex.prod(M,y[i0]),cplex.prod(-1.0*M,z[i0][i1]), cplex.prod(-1.0,t[i0])), M - 1-i1);
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