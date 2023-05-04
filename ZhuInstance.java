public class ZhuInstance {
    public final int T       = 3 ;
    public final int n       = 1 ;
    public final int q       = 4 ;
    public final int[] cPR_i = new int[]{ 0, 1,2,3,4} ;
    public final int[] cCR_i = new int[]{ 0, 1,2,3,4} ;
    public final int d       = 3 ;
    public final int alpha = 1;
    public final int beta = 2;
    public final int lengthOmega;

    public ZhuInstance() {
        this.lengthOmega = (int) Math.pow(T+1, n*q);
    }

}
