package fi.markoa.proto.hk2;

import javax.inject.Singleton;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

import fi.markoa.proto.hk2.annotation.Autowired;

@Singleton
public class AutowiredInjectResolver implements InjectionResolver<Autowired> {
    private GreetingService gs;

    public AutowiredInjectResolver() {
        gs = new GreetingServiceImpl();
    }
    
    @Override
    public boolean isConstructorParameterIndicator() {
        System.out.println("*isConstructorParameterIndicator");
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        System.out.println("*isMethodParameterIndicator");
        return false;
    }

    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        System.out.println("*resolve");
        return gs;
    }

}
