package pl.edu.agh.amber.drivers.dummy;

public class App 
{
    public static void main( String[] args )
    {
        Dummy dummy = new Dummy();
        DummyController dummyController = new DummyController(System.in, System.out, dummy);
        dummyController.run();
    }
}
