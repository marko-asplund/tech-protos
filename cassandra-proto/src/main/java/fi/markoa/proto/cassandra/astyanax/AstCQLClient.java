package fi.markoa.proto.cassandra.astyanax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class AstCQLClient {
  private static final Logger logger = LoggerFactory.getLogger(AstCQLClient.class);
  
  private AstyanaxContext<Keyspace> context;
  private Keyspace keyspace;
  private ColumnFamily<Integer, String> EMP_CF;
  private static final String EMP_CF_NAME = "employees";
  final String INSERT_STATEMENT =
      "INSERT INTO employees (empID, deptID, first_name, last_name) VALUES (?, ?, ?, ?);";

  public void init() {
    logger.debug("init()");
    
    context = new AstyanaxContext.Builder()
    .forCluster("Test Cluster")
    .forKeyspace("test1")
    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()      
        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
    )
    .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
        .setPort(9160)
        .setMaxConnsPerHost(1)
        .setSeeds("127.0.0.1:9160")
    )
    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()      
        .setCqlVersion("3.0.0")
        .setTargetCassandraVersion("1.2"))
    .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
    .buildKeyspace(ThriftFamilyFactory.getInstance());

    context.start();
    keyspace = context.getEntity();
    
    EMP_CF = ColumnFamily.newColumnFamily(
        EMP_CF_NAME, 
        IntegerSerializer.get(), 
        StringSerializer.get());
  }
  
  public void insert(int empId, int deptId, String firstName, String lastName) {
    try {
      @SuppressWarnings("unused")
      OperationResult<CqlResult<Integer, String>> result = keyspace
          .prepareQuery(EMP_CF)
              .withCql(INSERT_STATEMENT)
          .asPreparedStatement()
              .withIntegerValue(empId)
              .withIntegerValue(deptId)
              .withStringValue(firstName)
              .withStringValue(lastName)
          .execute();
    } catch (ConnectionException e) {
      logger.error("failed to write data to C*", e);
      throw new RuntimeException("failed to write data to C*", e);
    }
  }
  
  public void createCF() {
    try {
      @SuppressWarnings("unused")
      OperationResult<CqlResult<Integer, String>> result = keyspace
          .prepareQuery(EMP_CF)
          .withCql("CREATE TABLE employees (empID int, deptID int, first_name varchar, last_name varchar, PRIMARY KEY (empID, deptID));")
          .execute();
    } catch (ConnectionException e) {
      logger.error("failed to create CF", e);
      throw new RuntimeException("failed to create CF", e);
    }
  }

  public void read() {
    logger.debug("read()");
    try {
      OperationResult<CqlResult<Integer, String>> result
        = keyspace.prepareQuery(EMP_CF)
          .withCql(String.format("SELECT * FROM %s WHERE empId=222;", EMP_CF_NAME))
          .execute();
      for (Row<Integer, String> row : result.getResult().getRows()) {
        logger.debug("row: "+row.getKey()+","+row);
        
        ColumnList<String> cl = row.getColumns();
        logger.debug("emp");
        logger.debug("- emp id: "+cl.getIntegerValue("empid", null));
        logger.debug("- dept: "+cl.getIntegerValue("deptid", null));
        logger.debug("- firstName: "+cl.getStringValue("first_name", null));
        logger.debug("- lastName: "+cl.getStringValue("last_name", null));
      }
    } catch (ConnectionException e) {
      logger.error("failed to read from C*", e);
      throw new RuntimeException("failed to read from C*", e);
    }
  }
  
  public static void main(String[] args) {
    logger.debug("foobar");
    AstCQLClient c = new AstCQLClient();
    c.init();
//    c.createCF();
    c.insert(222, 333, "Eric", "Cartman");
    c.read();
  }

}
