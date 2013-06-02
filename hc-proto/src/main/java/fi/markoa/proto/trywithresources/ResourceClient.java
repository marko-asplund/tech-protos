package fi.markoa.proto.trywithresources;

public class ResourceClient {

    /*
     * test execution order of close() methods for resources declared in try-with-resources statement and the finally block.
     * 
     * try-with-resources declared resources are closed before finally block gets executed.
     */
    public static void main(String ... args) throws Exception {
        Resource3 r3 = null;
        try (
                        Resource1 r1 = new Resource1();
                        Resource2 r2 = new Resource2();
                        ) {
            r3 = new Resource3();
        } finally {
            r3.close();
        }
        
    }

}
