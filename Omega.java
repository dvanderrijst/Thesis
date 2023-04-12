public class Omega {
    private int T;
    private int n;
    public Omega(ZhuInstance instance) {
        this.T = instance.T;
        this.n = instance.n;
    }

    public int[][][] getTwir(){
        int[][][] Twir = new int[1][1][1];
        return Twir;
    }


    /**
     * As the article in Schouten also a discretised Weibull distribution is used, we will do the same.
     * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x)-F(x-1)
     * The number of scenarios is (T+1)^n. So for T=10, and n=3, we already have 1331 scenarios.
     * We say that the components fail independently.
     */
    private void descretisedWeibull(){

    }


    public void createScenarios(){
        this.T = 3;
        this.n = 5;

        int size = (int) Math.pow(T, n);
        int[][] s = new int[size][n];

        int V = T;

        for(int i = 0; i<n ; i++) {
            //first
            int R = (int) Math.pow(T, i);
            int S = (int) Math.pow(T, n - i);
            int D = (int) Math.pow(T, n - i - 1);

            for (int r = 0; r < R; r++) {
                for (int v = 0; v < V; v++) {

                    int d = 0;
                    while (d < D) {
                        int i1 = r * S + v * D + d;
                        if(i1 ==64){
                            System.out.println("uo");
                        }
                        s[i1][i] = v;
                        d++;
                    }
                }
            }
        }
//
//            //second
//            R = (int) Math.pow(T, i);
//            S = (int) Math.pow(T, T-i);
//            D = (int) Math.pow(T, T-i-1);
//
//            for (int r = 0; r < R; r++) {
//                for (int v = 0; v < V; v++) {
//                    int d = 0;
//                    while (d < D) {
//                        s[r * S + v * D + d][1] = v;
//                        d++;
//                    }
//                }
//            }
//
//            //third
//            R = (int) Math.pow(T, i);
//            S = (int) Math.pow(T, T-i);
//            D = (int) Math.pow(T, T-i-1);
//
//            for (int r = 0; r < R; r++) {
//                for (int v = 0; v < V; v++) {
//                    int d = 0;
//                    while (d < D) {
//                        s[r * S + v * D + d][2] = v;
//                        d++;
//                    }
//                }
//            }
//        }

//        //first
//        R = (int) Math.pow(T,0);
//        S = (int) Math.pow(T,3);
//        D = (int) Math.pow(T,2);
//
//        //second
//        R = (int) Math.pow(T,1);
//        S = (int) Math.pow(T,2);
//        D = (int) Math.pow(T,1);
//
//        //third
//        R = (int) Math.pow(T,2);
//        S = (int) Math.pow(T,1);
//        D = (int) Math.pow(T,0);
//
//
//        R = [0 , 1, 2];
//        S = [3, 2, 1];
//        D = [2, 1, 0];
//







//
//        int v = 0;
//        int d = 0;
//        int durance = 1;
//        while(v < T) {
//            int c = 0;
//            while (c < (int) Math.pow(T,2)) {
//                d = 0;
//                while(d<durance) {
//                    s[v + (int) Math.pow(T, 1) * c + d][2] = v;
//                    d++;
//                }
//                c = c + 1;
//            }
//            v++;
//        }
//
//        v = 0;
//        d = 0;
//        durance = 3;
//        while(v < T) {
//            int c = 0;
//            while (c < (int) Math.pow(T,1)) {
//                d = 0;
//                while(d<durance) {
//                    s[(int) Math.pow(T, 2) * c + d][1] = v;
//                    d++;
//                }
//                c = c + 1;
//            }
//            v++;
//        }
//
//        v = 0;
//        d = 0;
//        durance = 27;
//        while(v < T) {
//            int c = 0;
//            while (c < (int) Math.pow(T,0)) {
//                d = 0;
//                while(d<durance) {
//                    s[(int) Math.pow(T, 2) * c + d][0] = v;
//                    d++;
//                }
//                c = c + 1;
//            }
//            v++;
//        }



//        for(int i = 0; i < size - 1 ; i = i + (int) Math.pow(T,2)) {
//            int add2 = 0;
//            while (add2< (int) Math.pow(T,1)) {
//                int add1 = 0;
//                while (add1 < (int) Math.pow(T,1)) {
//                    s[i + add1 + (int) Math.pow(T,1) * add2][1] = add2;
//                    add1++;
//                }
//                add2++;
//            }
//        }

//        for(int i = 0; i < size - 1; i = i + (int) Math.pow(T,3)){
//            int add2 = 0;
//            while(add2< (int) Math.pow(T,1)) {
//                int add1 = 0;
//                while (add1 < (int) Math.pow(T,2)) {
//                    s[i + add1  + (int) Math.pow(T,2) * add2][0] = add2;
//                    add1 = add1 + 1;
//                }
//                add2++;
//            }
//        }

        System.out.println("this is the result");
        for (int i = 0; i < size; i++) {
            System.out.println();
            for (int j = 0; j < n; j++) {
                System.out.print(s[i][j]+"\t");
            }
        }
    }
}
