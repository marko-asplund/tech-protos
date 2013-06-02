package fi.markoa.proto.trywithresources;

public class Resource3 implements AutoCloseable {

    @Override
    public void close() throws Exception {
        System.out.println("Resource3.close()");
    }


}
