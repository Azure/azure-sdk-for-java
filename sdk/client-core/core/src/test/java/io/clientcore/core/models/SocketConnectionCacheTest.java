// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.clientcore.core.models.SocketConnection.SocketConnectionProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@link SocketConnectionCache} class.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SocketConnectionCacheTest {
    private static Socket myServerSocket;
    private static Thread server;
    private static final int PORT = 8080;
    private static volatile boolean keepRunning = true; // shared flag
    private static final SocketConnectionProperties SOCKET_PROPERTIES;

    static {
        try {
            SOCKET_PROPERTIES = new SocketConnectionProperties(new URL("http://localhost:8080"), "localhost", "8080");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void setup() {
        if (myServerSocket != null && !myServerSocket.isClosed()) {
            try {
                myServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        server = new Thread(() -> {
            try {
                ServerSocket myServer = new ServerSocket();
                myServer.setReuseAddress(true);
                myServer.bind(new java.net.InetSocketAddress(PORT));

                myServerSocket = myServer.accept();

                while (keepRunning) { // check the flag
                    BufferedReader br = new BufferedReader(new InputStreamReader(myServerSocket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(myServerSocket.getOutputStream());
                    while (keepRunning) { // check the flag
                        String line;
                        StringBuilder request = new StringBuilder();
                        while ((line = br.readLine()) != null && !line.isEmpty()) {
                            request.append(line).append("\n");
                        }
                        out.writeBytes("HTTP/1.1 200 OK\n\n");
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();
    }

    @AfterAll
    @SuppressWarnings("deprecation")
    public static void tearDown() throws IOException, InterruptedException {
        keepRunning = false; // stop the threads
        if (myServerSocket != null) {
            server.stop();
            if (!myServerSocket.isClosed()) {
                myServerSocket.close();
            }
            myServerSocket = null;
        }
        Thread.sleep(1000); // wait for the server to stop
    }

    @Test
    @Order(1)
    void testGetInstance() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance1 = SocketConnectionCache.getInstance(true, 10, 5000);
        SocketConnectionCache instance2 = SocketConnectionCache.getInstance(true, 10, 5000);
        assertSame(instance1, instance2, "Instances are not the same");
    }

    @Test
    @Order(2)
    void testGetConnectionReturnsNewConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10, 5000);

        try {
            SocketConnection connection = instance.get(SOCKET_PROPERTIES);
            assertNotNull(connection, "Connection is null");

        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testReuseConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10, 5000);
        try {
            SocketConnection connection = instance.get(SOCKET_PROPERTIES);
            instance.reuseConnection(connection);
            assertTrue(!connection.getSocket().isClosed(), "Connection is kept open");

            SocketConnection connection2 = instance.get(SOCKET_PROPERTIES);
            assertSame(connection, connection2, "Connections are not the same");
        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(4)
    void testConnectionPoolSize() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10, 5000);

        try {
            for (int i = 0; i < 10; i++) {
                SocketConnection connection = instance.get(SOCKET_PROPERTIES);
                instance.reuseConnection(connection);
            }
            // Access the connection pool size
            Field connectionPoolField = SocketConnectionCache.class.getDeclaredField("CONNECTION_POOL");
            connectionPoolField.setAccessible(true);
            Map<SocketConnectionProperties, List<SocketConnection>> connectionPool =
                (Map<SocketConnectionProperties, List<SocketConnection>>) connectionPoolField.get(instance);
            int poolSize = connectionPool.get(SOCKET_PROPERTIES).size();
            // connection pool size is 1 because of single threaded request
            assertEquals(1, poolSize, "Connection pool size is not as expected");
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(5)
    void testConnectionPoolSizeMultipleThreads() {
        SocketConnectionCache.clearCache();
        int maxConnections = 5;
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, maxConnections, 5000);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    SocketConnection connection = instance.get(SOCKET_PROPERTIES);
                    instance.reuseConnection(connection);
                } catch (IOException e) {
                    fail("Exception thrown: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        try {
            // Wait for all threads to finish
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }

        // Access the connection pool size
        try {
            Field connectionPoolField = SocketConnectionCache.class.getDeclaredField("CONNECTION_POOL");
            connectionPoolField.setAccessible(true);
            Map<SocketConnectionProperties, List<SocketConnection>> connectionPool =
                (Map<SocketConnectionProperties, List<SocketConnection>>) connectionPoolField.get(instance);
            int poolSize = connectionPool.get(SOCKET_PROPERTIES).size();
            assertEquals(maxConnections, poolSize, "Connection pool size is not as expected");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testKeepConnectionAliveFalse() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(false, 10, 5000);
        try {
            SocketConnection connection1 = instance.get(SOCKET_PROPERTIES);
            instance.reuseConnection(connection1);
            SocketConnection connection2 = instance.get(SOCKET_PROPERTIES);
            assertNotSame(connection1, connection2, "Connections are the same");
        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(7)
    void testReuseClosedConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10, 5000);
        try {
            SocketConnection connection = instance.get(SOCKET_PROPERTIES);
            connection.getSocket().close();
            instance.reuseConnection(connection);

            try {
                Field connectionPoolField = SocketConnectionCache.class.getDeclaredField("CONNECTION_POOL");
                connectionPoolField.setAccessible(true);
                Map<SocketConnectionProperties, List<SocketConnection>> connectionPool =
                    (Map<SocketConnectionProperties, List<SocketConnection>>) connectionPoolField.get(instance);
                int poolSize = connectionPool.get(SOCKET_PROPERTIES).size();
                assertEquals(0, poolSize, "Connection pool size should be 0 as connection is closed");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    void testReadTimeout() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10, 5000);

        // Start a server that delays its response
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(8081)) {
                Socket clientSocket = serverSocket.accept();
                Thread.sleep(6000); // delay longer than the read timeout
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeBytes("HTTP/1.1 200 OK\n\n");
                out.flush();
            } catch (IOException | InterruptedException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        }).start();

        // Try to get a connection and perform read
        assertThrows(SocketTimeoutException.class, () -> {
            SocketConnection connection = instance.get(
                new SocketConnectionProperties(new URL("http://localhost:8081"), "localhost", "8081"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getSocket().getInputStream()));
            reader.readLine(); // This should throw SocketTimeoutException
        }, "Expected readLine() to throw, but it didn't");
    }
}
