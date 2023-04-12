import ilog.concert.IloException;

public class MainZhu2022 {
    public static void main(String[] args) throws IloException {
        System.out.println("Hello donnie");
        ZhuInstance instance = new ZhuInstance();
        Omega scenarios = new Omega(instance);
        scenarios.createScenarios();
//        ZhuCplexModel model = new ZhuCplexModel(instance, scenarios.getTwir());

    }
}
