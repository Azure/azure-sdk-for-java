//package com.azure.spring.cloud.service.redis;
//
//import org.apache.commons.pool2.PooledObject;
//import org.apache.commons.pool2.PooledObjectFactory;
//import org.apache.commons.pool2.impl.DefaultPooledObject;
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import redis.clients.jedis.DefaultJedisClientConfig;
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisFactory;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.JedisPoolConfig;
//import redis.clients.jedis.Protocol;
//import redis.clients.jedis.Transaction;
//import redis.clients.jedis.exceptions.InvalidURIException;
//import redis.clients.jedis.exceptions.JedisConnectionException;
//import redis.clients.jedis.exceptions.JedisException;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//
//public class AzureJedisPoolTest {
//
//    private static final HostAndPort hnp = HostAndPorts.getRedisServers().get(0);
//
//    @Test
//    public void checkConnections() {
//        JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000);
//        try (Jedis jedis = pool.getResource()) {
//            jedis.auth("foobared");
//            jedis.set("foo", "bar");
//            assertEquals("bar", jedis.get("foo"));
//        }
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void checkResourceWithConfig() {
//        try (JedisPool pool = new JedisPool(HostAndPorts.getRedisServers().get(7),
//            DefaultJedisClientConfig.builder().socketTimeoutMillis(5000).build())) {
//
//            try (Jedis jedis = pool.getResource()) {
//                assertEquals("PONG", jedis.ping());
//                assertEquals(5000, jedis.getClient().getSoTimeout());
//                jedis.close();
//            }
//        }
//    }
//
//    @Test
//    public void checkCloseableConnections() throws Exception {
//        JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000);
//        try (Jedis jedis = pool.getResource()) {
//            jedis.auth("foobared");
//            jedis.set("foo", "bar");
//            assertEquals("bar", jedis.get("foo"));
//        }
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void checkConnectionWithDefaultHostAndPort() {
//        JedisPool pool = new JedisPool(new JedisPoolConfig());
//        try (Jedis jedis = pool.getResource()) {
//            jedis.auth("foobared");
//            jedis.set("foo", "bar");
//            assertEquals("bar", jedis.get("foo"));
//        }
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void checkResourceIsClosableAndReusable() {
//        GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
//        config.setMaxTotal(1);
//        config.setBlockWhenExhausted(false);
//        try (JedisPool pool = new JedisPool(config, hnp.getHost(), hnp.getPort(), 2000, "foobared", 0,
//            "closable-resuable-pool", false, null, null, null)) {
//
//            Jedis jedis = pool.getResource();
//            jedis.set("hello", "jedis");
//            jedis.close();
//
//            Jedis jedis2 = pool.getResource();
//            assertEquals(jedis, jedis2);
//            assertEquals("jedis", jedis2.get("hello"));
//            jedis2.close();
//        }
//    }
//
//    @Test
//    public void checkPoolRepairedWhenJedisIsBroken() {
//        JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort());
//        try (Jedis jedis = pool.getResource()) {
//            jedis.auth("foobared");
//            jedis.set("foo", "0");
//            jedis.quit();
//        }
//
//        try (Jedis jedis = pool.getResource()) {
//            jedis.auth("foobared");
//            jedis.incr("foo");
//        }
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void checkPoolOverflow() {
//        Assertions.assertThrows(JedisException.class, () -> {
//            GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
//            config.setMaxTotal(1);
//            config.setBlockWhenExhausted(false);
//            try (JedisPool pool = new JedisPool(config, hnp.getHost(), hnp.getPort());
//                 Jedis jedis = pool.getResource()) {
//                jedis.auth("foobared");
//
//                try (Jedis jedis2 = pool.getResource()) {
//                    jedis2.auth("foobared");
//                }
//            }
//        });
//    }
//
//    @Test
//    public void securePool() {
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setTestOnBorrow(true);
//        JedisPool pool = new JedisPool(config, hnp.getHost(), hnp.getPort(), 2000, "foobared");
//        try (Jedis jedis = pool.getResource()) {
//            jedis.set("foo", "bar");
//        }
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void nonDefaultDatabase() {
//        try (JedisPool pool0 = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "foobared"); Jedis jedis0 = pool0.getResource()) {
//            jedis0.set("foo", "bar");
//            assertEquals("bar", jedis0.get("foo"));
//        }
//
//        try (JedisPool pool1 = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "foobared", 1); Jedis jedis1 = pool1.getResource()) {
//            assertNull(jedis1.get("foo"));
//        }
//    }
//
//    @Test
//    public void startWithUrlString() {
//        try (Jedis j = new Jedis("localhost", 6380)) {
//            j.auth("foobared");
//            j.select(2);
//            j.set("foo", "bar");
//        }
//
//        try (JedisPool pool = new JedisPool("redis://:foobared@localhost:6380/2");
//             Jedis jedis = pool.getResource()) {
//            assertEquals("PONG", jedis.ping());
//            assertEquals("bar", jedis.get("foo"));
//        }
//    }
//
//    @Test
//    public void startWithUrl() throws URISyntaxException {
//        try (Jedis j = new Jedis("localhost", 6380)) {
//            j.auth("foobared");
//            j.select(2);
//            j.set("foo", "bar");
//        }
//
//        try (JedisPool pool = new JedisPool(new URI("redis://:foobared@localhost:6380/2"));
//             Jedis jedis = pool.getResource()) {
//            assertEquals("bar", jedis.get("foo"));
//        }
//    }
//
//    @Test
//    public void shouldThrowInvalidURIExceptionForInvalidURI() throws URISyntaxException {
//        Assertions.assertThrows(InvalidURIException.class, () -> {
//            new JedisPool(new URI("localhost:6380")).close();
//        });
//    }
//
//    @Test
//    public void allowUrlWithNoDBAndNoPassword() throws URISyntaxException {
//        new JedisPool("redis://localhost:6380").close();
//        new JedisPool(new URI("redis://localhost:6380")).close();
//    }
//
//    @Test
//    public void selectDatabaseOnActivation() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "foobared")) {
//
//            Jedis jedis0 = pool.getResource();
//            assertEquals(0, jedis0.getDB());
//
//            jedis0.select(1);
//            assertEquals(1, jedis0.getDB());
//
//            jedis0.close();
//
//            Jedis jedis1 = pool.getResource();
//            assertTrue(jedis1 == jedis0, "Jedis instance was not reused");
//            assertEquals(0, jedis1.getDB());
//
//            jedis1.close();
//        }
//    }
//
//    @Test
//    public void customClientName() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "foobared", 0, "my_shiny_client_name"); Jedis jedis = pool.getResource()) {
//
//            assertEquals("my_shiny_client_name", jedis.clientGetname());
//        }
//    }
//
//    @Test
//    public void returnResourceDestroysResourceOnException() {
//
//        class CrashingJedis extends Jedis {
//            @Override
//            public void resetState() {
//                throw new RuntimeException();
//            }
//        }
//
//        final AtomicInteger destroyed = new AtomicInteger(0);
//
//        class CrashingJedisPooledObjectFactory implements PooledObjectFactory<Jedis> {
//
//            @Override
//            public PooledObject<Jedis> makeObject() throws Exception {
//                return new DefaultPooledObject<Jedis>(new CrashingJedis());
//            }
//
//            @Override
//            public void destroyObject(PooledObject<Jedis> p) throws Exception {
//                destroyed.incrementAndGet();
//            }
//
//            @Override
//            public boolean validateObject(PooledObject<Jedis> p) {
//                return true;
//            }
//
//            @Override
//            public void activateObject(PooledObject<Jedis> p) throws Exception {
//            }
//
//            @Override
//            public void passivateObject(PooledObject<Jedis> p) throws Exception {
//            }
//        }
//
//        GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
//        config.setMaxTotal(1);
//        JedisPool pool = new JedisPool(config, new CrashingJedisPooledObjectFactory());
//        Jedis crashingJedis = pool.getResource();
//
//        try {
//            crashingJedis.close();
//        } catch (Exception ignored) {
//        }
//
//        assertEquals(1, destroyed.get());
//    }
//
//    @Test
//    public void returnResourceShouldResetState() {
//        GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
//        config.setMaxTotal(1);
//        config.setBlockWhenExhausted(false);
//        JedisPool pool = new JedisPool(config, hnp.getHost(), hnp.getPort(), 2000, "foobared");
//
//        Jedis jedis = pool.getResource();
//        try {
//            jedis.set("hello", "jedis");
//            Transaction t = jedis.multi();
//            t.set("hello", "world");
//        } finally {
//            jedis.close();
//        }
//
//        Jedis jedis2 = pool.getResource();
//        try {
//            assertTrue(jedis == jedis2);
//            assertEquals("jedis", jedis2.get("hello"));
//        } finally {
//            jedis2.close();
//        }
//
//        pool.close();
//        assertTrue(pool.isClosed());
//    }
//
//    @Test
//    public void getNumActiveWhenPoolIsClosed() {
//        JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "foobared", 0, "my_shiny_client_name");
//
//        try (Jedis j = pool.getResource()) {
//            j.ping();
//        }
//
//        pool.close();
//        assertEquals(0, pool.getNumActive());
//    }
//
//    @Test
//    public void getNumActiveReturnsTheCorrectNumber() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000)) {
//            Jedis jedis = pool.getResource();
//            jedis.auth("foobared");
//            jedis.set("foo", "bar");
//            assertEquals("bar", jedis.get("foo"));
//
//            assertEquals(1, pool.getNumActive());
//
//            Jedis jedis2 = pool.getResource();
//            jedis.auth("foobared");
//            jedis.set("foo", "bar");
//
//            assertEquals(2, pool.getNumActive());
//
//            jedis.close();
//            assertEquals(1, pool.getNumActive());
//
//            jedis2.close();
//
//            assertEquals(0, pool.getNumActive());
//        }
//    }
//
//    @Test
//    public void testAddObject() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000)) {
//            pool.addObjects(1);
//            assertEquals(1, pool.getNumIdle());
//        }
//    }
//
//    @Test
//    public void closeResourceTwice() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000)) {
//            Jedis j = pool.getResource();
//            j.auth("foobared");
//            j.ping();
//            j.close();
//            j.close();
//        }
//    }
//
//    @Test
//    public void closeBrokenResourceTwice() {
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000)) {
//            Jedis j = pool.getResource();
//            try {
//                // make connection broken
//                j.getClient().getOne();
//                fail();
//            } catch (Exception e) {
//                assertTrue(e instanceof JedisConnectionException);
//            }
//            assertTrue(j.isBroken());
//            j.close();
//            j.close();
//        }
//    }
//
//    @Test
//    public void testCloseConnectionOnMakeObject() {
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setTestOnBorrow(true);
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), hnp.getHost(), hnp.getPort(), 2000,
//            "wrong pass"); Jedis jedis = new Jedis("redis://:foobared@localhost:6379/")) {
//            int currentClientCount = getClientCount(jedis.clientList());
//            try {
//                pool.getResource();
//                fail("Should throw exception as password is incorrect.");
//            } catch (Exception e) {
//                assertEquals(currentClientCount, getClientCount(jedis.clientList()));
//            }
//        }
//    }
//
//    private int getClientCount(final String clientList) {
//        return clientList.split("\n").length;
//    }
//
//    @Test
//    public void testResetInvalidPassword() {
//        JedisFactory factory = new JedisFactory(hnp.getHost(), hnp.getPort(), 2000, 2000,
//            "foobared", 0, "my_shiny_client_name") {
//        };
//
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), factory)) {
//            Jedis obj1_ref;
//            try (Jedis obj1_1 = pool.getResource()) {
//                obj1_ref = obj1_1;
//                obj1_1.set("foo", "bar");
//                assertEquals("bar", obj1_1.get("foo"));
//                assertEquals(1, pool.getNumActive());
//            }
//            assertEquals(0, pool.getNumActive());
//            try (Jedis obj1_2 = pool.getResource()) {
//                assertSame(obj1_ref, obj1_2);
//                assertEquals(1, pool.getNumActive());
//                factory.setPassword("wrong password");
//                try (Jedis obj2 = pool.getResource()) {
//                    fail("Should not get resource from pool");
//                } catch (JedisException e) {
//                }
//                assertEquals(1, pool.getNumActive());
//            }
//            assertEquals(0, pool.getNumActive());
//        }
//    }
//
//    @Test
//    public void testResetValidPassword() {
//        JedisFactory factory = new JedisFactory(hnp.getHost(), hnp.getPort(), 2000, 2000,
//            "bad password", 0, "my_shiny_client_name") {
//        };
//
//        try (JedisPool pool = new JedisPool(new JedisPoolConfig(), factory)) {
//            try (Jedis obj1 = pool.getResource()) {
//                fail("Should not get resource from pool");
//            } catch (JedisException e) {
//            }
//            assertEquals(0, pool.getNumActive());
//
//            factory.setPassword("foobared");
//            try (Jedis obj2 = pool.getResource()) {
//                obj2.set("foo", "bar");
//                assertEquals("bar", obj2.get("foo"));
//            }
//        }
//    }
//
//    private final class HostAndPorts {
//
//        private static List<HostAndPort> redisHostAndPortList = new ArrayList<>();
//        private static List<HostAndPort> sentinelHostAndPortList = new ArrayList<>();
//        private static List<HostAndPort> clusterHostAndPortList = new ArrayList<>();
//
//        static {
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 1));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 2));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 3));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 4));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 5));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 6));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 7));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 8));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 9));
//            redisHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_PORT + 10));
//
//            sentinelHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_SENTINEL_PORT));
//            sentinelHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_SENTINEL_PORT + 1));
//            sentinelHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_SENTINEL_PORT + 2));
//            sentinelHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_SENTINEL_PORT + 3));
//            sentinelHostAndPortList.add(new HostAndPort("localhost", Protocol.DEFAULT_SENTINEL_PORT + 4));
//
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7379));
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7380));
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7381));
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7382));
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7383));
//            clusterHostAndPortList.add(new HostAndPort("localhost", 7384));
//        }
//
//        public static List<HostAndPort> parseHosts(String envHosts,
//                                                   List<HostAndPort> existingHostsAndPorts) {
//
//            if (null != envHosts && 0 < envHosts.length()) {
//
//                String[] hostDefs = envHosts.split(",");
//
//                if (null != hostDefs && 2 <= hostDefs.length) {
//
//                    List<HostAndPort> envHostsAndPorts = new ArrayList<>(hostDefs.length);
//
//                    for (String hostDef : hostDefs) {
//
//                        envHostsAndPorts.add(HostAndPort.from(hostDef));
//                    }
//
//                    return envHostsAndPorts;
//                }
//            }
//
//            return existingHostsAndPorts;
//        }
//
//        public static List<HostAndPort> getRedisServers() {
//            return redisHostAndPortList;
//        }
//
//        public static List<HostAndPort> getSentinelServers() {
//            return sentinelHostAndPortList;
//        }
//
//        public static List<HostAndPort> getClusterServers() {
//            return clusterHostAndPortList;
//        }
//
//        private HostAndPorts() {
//            throw new InstantiationError("Must not instantiate this class");
//        }
//    }
//
//}
