package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

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
        constraint10b();
        constraint10cd(); //this is actually not in the paper but lets see
        constraint10e();

        constraint12de();
        constraint12fg();
    }

    private void constraint12fg() throws IloException{
        for(int i1 : I1){
            for(int j1 : I1){
                if(j1<i1){};
            }
        }
    }

    private void constraint12de() throws IloException{
        for(int i0 : I0){
            for(int i1 : I1){
                cplex.addLe(cplex.diff(z[i0][i1],y[i0]),0.0, "12d("+i0+","+i1+")");

                for(int j1 : I1){
                    if(i1 < j1){
                        cplex.addLe(cplex.diff(z[i0][i1], z[i0][j1]), 0, "12e("+i0+","+i1+","+j1+")");
                    }
                }
            }
        }
    }

    private void setVarZ() throws IloException{
        z = new IloNumVar[I0.length][I1.length];
        for (int i0 : I0) {
            for(int i1 : I1){
                IloNumVar var = cplex.boolVar("z("+i0+","+i1+")");
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
