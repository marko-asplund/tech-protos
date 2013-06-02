package fi.markoa.proto.trywithresources;

public class Resource1 implements AutoCloseable {

    @Override
    public void close() throws Exception {
        System.out.println("Resource1.close()");
    }


}
