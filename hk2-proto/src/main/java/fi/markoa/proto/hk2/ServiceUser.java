package fi.markoa.proto.hk2;

import fi.markoa.proto.hk2.annotation.Autowired;


public class ServiceUser {
    @Autowired
    private GreetingService svc;
    
    public void doStuff() {
        System.out.println("svc: "+svc);
        System.out.println("greeting: "+svc.sayHello("world"));
    }

}
