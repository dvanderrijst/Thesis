package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ValueDeterminationModel {
    private final Instance instance;
    private final Policy R;

    private static IloNumVar[] values;
    private static IloNumVar g;
    private static IloCplex cplex;
    public ValueDeterminationModel(Instance instance, Policy policy) throws IloException {
        this.instance = instance;
        R = policy;
        cplex = new IloCplex();
        cplex.setName("cplexModel");
        values = new IloNumVar[instance.I0.length * instance.I1.length];

        setVariables();
        setRelations();
        cplex.setOut(null);
        cplex.setWarning(null);
        cplex.minimize(g);
        cplex.exportModel("values.lp");
        cplex.solve();

//        for (IloNumVar value : values ) {
//            System.out.println(cplex.getValue(value));
//        }
//        System.out.println("g="+cplex.getValue(g));
    }

    private void setVariables() throws IloException{
        for(int i0: instance.I0){
            for(int i1 : instance.I1){
                values[i0 + i1 * instance.I0.length] = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, "v_"+i0+"_"+i1);
            }
        }
        g = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, "g");
    }


    private void setRelations() throws IloException{

        for(int i0: instance.I0) {
            for (int i1 : instance.I1) {

                IloNumExpr lhs = values[i0 + i1 * instance.I0.length];

                double rhs1 = 0.0;
                if(i1==0){
                    rhs1 = instance.cCR_i_t[0][i0];
                } else if (R.get(i0, i1) == 1) {
                    rhs1 = instance.cPR_i_t[0][i0];
                } else if (R.get(i0, i1) == 0) {
                    rhs1 = 0;
                }

                IloNumExpr rhs2 = cplex.prod(-1.0, g);

                IloNumExpr rhs3 = cplex.constant(0);
                for (int j0 : instance.I0) {
                    for (int j1 : instance.I1) {
                        rhs3 = cplex.sum(rhs3, cplex.prod(instance.piOneDim(i0, i1, j0, j1, R.get(i0,i1)), values[j0 + j1 * instance.I0.length]));
                    }
                }
                cplex.addEq(lhs, cplex.sum(rhs1, cplex.sum(rhs2, rhs3)), "constraint2a_" + i0 + "_" + i1);
            }
        }

        cplex.addEq(values[0], 0, "constraint2b");
    }



    public double getValueV(int i0, int i1) throws IloException {
        return cplex.getValue(values[i0+i1 * instance.I0.length]);
    }

    public double getValueG() throws IloException{
        return cplex.getValue(g);
    }

    public void clean() throws IloException {
        cplex.clearModel();
        cplex.close();
    }

}
