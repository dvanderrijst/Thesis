package SchoutenTwoComp;

import Main.Instance;

public class Model {
    private final Instance i;
    private final int M;
    public Model(Instance i) {
        this.i = i;
        M = i.M;
    }

    /**
     * return the array int[] A. These are the action points that are possible, given the values of i0, i1, i2 which are the
     * time, age component 1 and age component 2 respectively.
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @return possible action points
     */
    private int[] A(int i0, int i1, int i2){
        int[] A;
        if ( (i1 == 0 || i1 == M) & (i2 == 0 || i2 == M)){
            A = new int[]{3} ;
        }
        else if( (i1 == 0 || i1 == M) & (i2 != 0 & i2 != M)){
            A = new int[]{1,3} ;
        }
        else if( (i2 == 0 || i2 == M) & (i1 != 0 & i1 != M)){
            A = new int[]{2,3} ;
        }
        else{
            A = new int[]{0,1,2,3};
        }
        return A;
    }

    /**
     * the value for pi(). See the corresponding thesis-report to find this in an easier-to-read format.
     * @param i0 time current
     * @param i1 age component 1
     * @param i2 age component 2
     * @param j0 time next step
     * @param j1 age component 1 next step
     * @param j2 age component 2 next step
     * @param a action taken
     * @return value of pi
     */
    private double pi(int i0, int i1, int i2, int j0, int j1, int j2, int a){
        double pi_value = 0.0;
        if( j0 != (i0+1)%i.N){
            System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
        }
        else if(a==0){
            if((j1 == (i1 + 1)) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(i1))*(1.0-i.p_i(i2));
            }
            else if((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(i1))*i.p_i(i2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(i1)*(1.0-i.p_i(i2));
            }
            else if((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(i1)*i.p_i(i2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==1){
            if     ((j1 == 1) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(0))*(1.0-i.p_i(i2));
            }
            else if((j1 == 1) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(0))*i.p_i(i2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(0)*(1.0-i.p_i(i2));
            }
            else if((j1 == 0) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(0)*i.p_i(i2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==2) {
            if ((j1 == (i1 + 1)) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.p_i(i1)) * (1.0 - i.p_i(0));
            } else if ((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.p_i(i1)) * i.p_i(0);
            } else if ((j1 == 0) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = i.p_i(i1) * (1.0 - i.p_i(0));
            } else if ((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = i.p_i(i1) * i.p_i(0);
            } else {
                pi_value = 0.0;
            }
        }
        else if(a==3){
            if((j1 == 1) & (j2 == 1)){
                pi_value = (1.0 - i.p_i(0))*(1.0-i.p_i(0));
            }
            else if((j1 == 1) & (j2 == 0) ){
                pi_value = (1.0 - i.p_i(0))*i.p_i(0);
            }
            else if((j1 == 0) & (j2 == 1)){
                pi_value = i.p_i(0)*(1.0-i.p_i(0));
            }
            else if((j1 == 0) & (j2 == 0)){
                pi_value = i.p_i(0)*i.p_i(0);
            }
            else{
                pi_value = 0.0;
            }
        }
        else{
            System.out.println("This value for a does not exist");
        }
        return pi_value;
    }

    /**
     * Costs for a certain time in the year, for ages i1 and i2 for the two components, and for action a.
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @param a action taken
     * @return value of the costs
     */
    private double c(int i0, int i1, int i2, int a){
        double c = 0.0;
        if (a==0){
            c = 0.0;
        }
        else if(a==1){
            if (i1 != 0){
                c = i.cPR_i[i0] + i.d;
            }
            else if(i1 ==0){
                c = i.cCR_i[i0] + i.d;
            }
        }
        else if(a==2) {
            if (i2 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i2 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        }
        else if(a==3){
            if (i1 != 0 & i2 != 0){
                c = 2 * i.cPR_i[i0] + i.d;
            }
            else if (i1 == 0 & i2 != 0){
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            }
            else if (i1 != 0 & i2 == 0){
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            }
            else if (i1 == 0 & i2 == 0) {
                c = 2 * i.cCR_i[i0] + i.d;
            }
        }
        else{
            System.out.println("This value for a does not exist");
        }
        return c;
    }

}
