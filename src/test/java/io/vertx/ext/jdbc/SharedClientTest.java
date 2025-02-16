package io.vertx.ext.jdbc;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SharedClientTest extends JDBCClientTestBase {

  @BeforeClass
  public static void createDb() throws Exception {
    resetDb(SharedClientTest.class);
  }

  @Test(timeout = 60000)
  public void testDeadlock() throws Exception {
    JsonObject config = DBConfigs.hsqldb(SharedClientTest.class);
    int num = 6;
    int iter = 5000;
    AtomicInteger count = new AtomicInteger();
    List<Thread> threads = new ArrayList<>();
    for (int i = 0;i < num;i++) {
      Thread t = new Thread() {
        @Override
        public void run() {
          for (int i = 0;i < iter;i++) {
            count.incrementAndGet();
            SQLClient client = JDBCClient.createShared(vertx, config);
            close(client);
          }
        }
      };
      threads.add(t);
    }
    for (Thread t : threads) {
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
  }
}
