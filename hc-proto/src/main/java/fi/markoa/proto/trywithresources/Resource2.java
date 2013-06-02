package fi.markoa.proto.trywithresources;

public class Resource2 implements AutoCloseable {

    @Override
    public void close() throws Exception {
        System.out.println("Resource2.close()");
    }


}
