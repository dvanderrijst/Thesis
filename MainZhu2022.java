import ilog.concert.IloException;

public class MainZhu2022 {
    public static void main(String[] args) throws IloException {
        System.out.println("Hello donnie");
        ZhuInstance instance = new ZhuInstance();
        Omega scenarios = new Omega(instance);
        scenarios.createScenarios();
//        for (int w = 0; w < scenarios.lenghtOmega ; w++) {
        for (int w = 0; w < 100; w++) {
            ZhuCplexModel model = new ZhuCplexModel(instance, scenarios.getTir(w));
        }
    }
}
