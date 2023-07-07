package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;

/**
 * Class that calculates the value of small delta, given a policy R, and transition probabilities from the instance.
 * Small delta are the costs incurred when shifting the preventive maintenance for one component.
 *
 * @author 619034dr Donna van der Rijst
 */
public class delta {
    public final TransitionMatrix nulMatrix;
    public final Policy R;
    public final Instance instance;

    /**
     * Constructor for the delta class.
     * @param policy Policy R containing actions for all states.
     * @param instance Instance containing all important parameters
     */
    public delta(Policy policy, Instance instance) {
        this.nulMatrix = new TransitionMatrix(instance);
        this.R = policy;
        this.instance = instance;
    }

    /**
     * Method that calculates the small delta.
     * It refers to method getDelta, to calculate the big delta value. This method on its turn refers to the ValueDetermationModel class, to calculate values of v and g.
     * @param i0 starting state i0
     * @param i1 starting state i1
     * @param x number of shifts the PM is making
     * @return extra costs incurred when shifting the components PM by x.
     * @throws IloException as the getDelta method works with ValueDeterminationModel that works with CPLEX.
     */
    public double getdelta(int i0, int i1, int x) throws IloException {
        int v = findV(i0,i1);
        int z0 = (i0 + v + instance.N)%instance.N;
        int z1 = i1 + v;
        int j0 = (i0 + v + x + instance.N)%instance.N;
        int j1 = i1 + v + x;
        System.out.println("v="+v);

        //Detect Some errors
        if(x < -v){ System.out.println("x can't be smaller than -v, v="+v); System.exit(1); }
        else if(j1>instance.M){ System.out.println("We can't provide a value as j0>M"); System.exit(1); }

        //term 1
        double term1 = nulMatrix.getPiPowerValue(i0, i1, z0, z1, v) * getDelta(z0, z1, 1);
        if(Math.abs(term1)>0.000000000001){System.out.println("This should be 0 in our use case. ");System.exit(1);}

        //term 2
        double term2 = 0.0;
        if(x<0){
            double sum = 0.0;
            for (int k = x+1; k <= 0; k++) {
                double piValue    = nulMatrix.getPiPowerValue(j0,0,(i0+v+k + instance.N)%instance.N, k-x, k-x);
                double deltaValue = getDelta((i0+v+k + instance.N)%instance.N, k-x, 0);
//                System.out.println(piValue+" * "+ deltaValue);
                sum = sum + piValue * deltaValue;
            }
            term2 = nulMatrix.getPiPowerValue(i0,i1,j0,j1,v+x) * sum;
//            term2 = sum;
        }


        //term3
        double term3 = 0.0;
        if(x>0){
            double sum = 0.0;
            for (int k = 0; k <= x - 1; k++) {
                double piValue    = nulMatrix.getPiPowerValue(i0,i1,(i0+v+k + instance.N)%instance.N,i1+v+k, v+k);
                double deltaValue = getDelta((i0+v+k + instance.N)%instance.N,i1+v+k,0);
//                System.out.println(piValue+" * "+ deltaValue);
                sum = sum + piValue * deltaValue;
            }
            term3 = sum;
        }


        //term4
        double piValue    = nulMatrix.getPiPowerValue(i0,i1,j0,j1,v+x);
        double deltaValue = getDelta(j0,j1,1);
//        System.out.println(piValue+" * "+ deltaValue);
        double term4 = piValue * deltaValue;
//        System.out.println("term1 = -"+term1);
//        System.out.println("term2 = "+term2);
//        System.out.println("term3 = "+term3);
//        System.out.println("term4 = "+term4);

        return -term1 + term2 + term3 + term4;
    }


    /**
     * This method find the value of v, which is the initial ultimate number of periods to wait until maintenance should be done.
     * It scans through the policy, and stops when the action 1 is chosen which denotes maintenance.
     * @param i0 time of input state
     * @param i1 age of input state
     * @return v
     */
    public int findV(int i0, int i1) {
        int v = -1 ;
        int action = 0;
        while(action == 0){
            v++;
            action = R.get( (i0 + v + instance.N) % instance.N , i1 + v);
        }
        return v;
    }

    /**
     * Calculates big Delta, the extra costs incurred when chosing another action than the optimal one, when starting in state i=(i0,i1)
     * @param i0 starting period
     * @param i1 starting age
     * @param a new action taken. If this action is the optimal action, the value for Delta will be zero
     * @return value Delta
     * @throws IloException as ValueDeterminationModel works with CPLEX.
     */
    public double getDelta(int i0, int i1, int a) throws IloException {
        ValueDeterminationModel values = new ValueDeterminationModel(instance, R);

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
                sum = sum + instance.piOneDim(i0, i1, j0, j1, a) * values.getValueV(j0 , j1);
//                System.out.println("i0="+i0+", i1="+i1+", j0="+j0+", j1="+j1+"\t"+newMatrix.getPiValue(i0, i1, j0, j1)+"*"+values.getValueV(j0,j1));
            }
        }
//        System.out.println("\n\n\nBIG DELTA OUTPUT");
//        System.out.println(instantCosts);
//        System.out.println(sum);
//        System.out.println("g="+values.getValueG());
//        System.out.println(values.getValueV(i0,i1));
        double Delta = instantCosts + sum - values.getValueG() - values.getValueV(i0,i1);
//        System.out.println(Delta);

        values.clean();

        return Delta;
    }

}
