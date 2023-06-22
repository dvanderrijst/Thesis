package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class ModelARP_warmstart extends ModelARP {
    final int[][][] actions;

    public ModelARP_warmstart(Instance instance, String fileName, int[][][] actions) throws IloException, IOException {
        super(instance, fileName);
        this.actions = actions;
    }


    @Override
    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
        addWarmStart();
    }

    /**
     * This method adds the warm start, so we force the model to return us the SchoutenTwoComp.policies that are in transient state now.
     * @throws IloException
     */
    private void addWarmStart() throws IloException {
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    //action should be 0 here. So in case action was 0, it remains zero. In case it was 1,2,3 it should become zero.
                    if( (i2 == 0 || i2 == M) && i1 !=0 && i1 !=M ) {
                        if (this.actions[i0][i1][i2] != 4) {
                            cplex.addEq(x[i0][i1][i2][3], 0);
                        }
                    }
                    else if((i1 ==0 || i1 == M ) && i2 !=0 && i2 !=M) {
                        if (this.actions[i0][i1][i2] != 4) {
                            cplex.addEq(x[i0][i1][i2][3], 0);
                        }
                    }
                    else if(i1 !=0 && i2 !=0 && i1 !=M && i2 != M) {
                        if (this.actions[i0][i1][i2] != 4) {
                            cplex.addEq(x[i0][i1][i2][1], 0);
                            cplex.addEq(x[i0][i1][i2][2], 0);
                            cplex.addEq(x[i0][i1][i2][3], 0);
                        }
                    }
                }
            }
        }
    }
}
