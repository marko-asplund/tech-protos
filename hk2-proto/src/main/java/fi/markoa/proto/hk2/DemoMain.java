package fi.markoa.proto.hk2;

public class DemoMain {

    public static void main(String ... args) {
        Container c = new Container();
        c.init();
        
        c.getServiceUser().doStuff();

        c.getServiceUser().doStuff();
    }

}
