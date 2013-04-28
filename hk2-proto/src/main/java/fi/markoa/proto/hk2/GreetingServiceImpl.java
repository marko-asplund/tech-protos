package fi.markoa.proto.hk2;


public class GreetingServiceImpl implements GreetingService {

    @Override
    public String sayHello(String who) {
        return "hello, "+who;
    }
}
