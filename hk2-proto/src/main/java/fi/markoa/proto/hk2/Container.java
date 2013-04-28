package fi.markoa.proto.hk2;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;


public class Container {
    private ServiceLocator locator;

    public void init() {
        ServiceLocatorFactory f = ServiceLocatorFactory.getInstance();
        locator = f.create("HelloWorld");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);

        DynamicConfiguration c = dcs.createDynamicConfiguration();
        c.addActiveDescriptor(AutowiredInjectResolver.class);
        c.commit();

        DescriptorImpl d = BuilderHelper.link(GreetingServiceImpl.class.getName())
                        .to(GreetingService.class.getName())
                        .in(Singleton.class.getName())
                        .build();
        c = dcs.createDynamicConfiguration();
        c.bind(d);
        c.commit();
    }
    
    public ServiceUser getServiceUser() {
        ServiceUser u = new ServiceUser();
        locator.inject(u);
        return u;
    }
    
    
}
