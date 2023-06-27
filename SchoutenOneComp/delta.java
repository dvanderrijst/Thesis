package SchoutenOneComp;

import Main.Instance;
import com.sun.jdi.Value;
import ilog.concert.IloException;

public class delta {
    public final TransitionMatrix matrix;
    public final Policy R;
    public final Instance instance;

    public delta(TransitionMatrix matrix, Policy policy, Instance instance) {
        this.matrix = matrix;
        R = policy;
        this.instance = instance;
    }

    public double getdelta(int i0, int i1, int v, int x) throws IloException {
        int z0 = (i0 + v + instance.N)%instance.N;
        int z1 = i1 + v;
        int j0 = (i0 + v + x + instance.N)%instance.N;
        int j1 = i1 + v + x;

        System.out.println(j1);
        if(j1>instance.M){
            System.out.println("We can't provide a value as j0>M");
            return 0.0;
        }

        //term 1
        double term1 = matrix.getPiPowerValue(i0, i1, z0, z1, v) * getDelta(z0, z1, 1);

        //term 2
        double term2 = 0.0;
        if(x<0){
            double sum = 0.0;
            for (int k = x+1; k < 0; k++) {
                sum = sum + matrix.getPiPowerValue((i0+v+x + instance.N)%instance.N,0,(i0+v+k + instance.N)%instance.N, k-x, k-x) * getDelta((i0+v+k + instance.N)%instance.N, k-x, 0);
            }
            term2 = matrix.getPiPowerValue(i0,i1,j0,j1,v+x) * sum;
        }

        //term3
        double term3 = 0.0;
        if(x>0){
            double sum = 0.0;
            for (int k = 0; k < x - 1; k++) {
                sum = sum + matrix.getPiPowerValue(i0,i1,(i0+v+k + instance.N)%instance.N,i1+v+k, v+k) * getDelta((i0+v+k + instance.N)%instance.N,i1+v+k,0);
            }
            term3 = sum;
        }

        //term4
        double term4 = matrix.getPiPowerValue(i0,i1,j0,j1,v+x) * getDelta(j0,j1,1);


        return -term1 + term2 + term3 + term4;
    }

    public double getDelta(int i0, int i1, int a) throws IloException {

        ValueDeterminationModel values = new ValueDeterminationModel(1, matrix, instance, R);

        double instantCosts = 0.0;
        if(i1==0){
            instantCosts = instance.cCR_i_t[0][i0];
        } else if (a == 1) {
            instantCosts = instance.cPR_i_t[0][i0];
        } else if (a == 0) {
            instantCosts = 0;
        }

        double sum = 0.0;
        for(int j0 : instance.I0){
            for(int j1 : instance.I1){
                sum = sum + matrix.getPiValue(i0, i1, j0, j1)*values.getValueV(j0,j1);
            }
        }
        double Delta = instantCosts + sum - values.getValueG() - values.getValueV(i0,i1);
        return Delta;
    }

}
