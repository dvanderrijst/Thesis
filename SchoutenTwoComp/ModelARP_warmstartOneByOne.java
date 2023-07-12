package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class ModelARP_warmstartOneByOne extends ModelARP {
    private final int i0_start;
    private final int i1_start;
    private final int i2_start;

    public ModelARP_warmstartOneByOne(Instance instance, String fileName, int i0, int i1, int i2) throws IloException, IOException {
        super(instance, fileName);
        this.i0_start = i0;
        this.i1_start = i1;
        this.i2_start = i2;
    }

    @Override
    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
        addOneByOneConstraints();
    }

    /**
     * This method adds the warm start, so we force the model to return us the SchoutenTwoComp.policies that are in transient state now.
     * @throws IloException
     */
    private void addOneByOneConstraints() throws IloException {
        int i0 = (i0_start - 1 + i.N) % i.N;
        int i1 = i1_start - 1;
        int i2 = i2_start - 1;

        while(i1 != 0 && i2 != 0){
            cplex.addEq(x[i0][i1][i2][1],0);
            cplex.addEq(x[i0][i1][i2][2],0);
            cplex.addEq(x[i0][i1][i2][3],0);

            i0 = (i0 - 1 + i.N) % i.N;
            i1 = i1 - 1;
            i2 = i2 - 1;
        }



    }
}
