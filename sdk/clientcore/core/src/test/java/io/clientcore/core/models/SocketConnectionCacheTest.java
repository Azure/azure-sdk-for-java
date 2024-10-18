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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.clientcore.core.models.SocketConnection.SocketConnectionProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@link SocketConnectionCache} class.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SocketConnectionCacheTest {
    private static Socket myServerSocket;
    private static Thread server;
    private static volatile boolean keepRunning = true; // shared flag
    private static SocketConnectionProperties socketConnectionProperties;

    @BeforeAll
    public static void setup() throws IOException {
        // Ensure previous server socket is closed if necessary
        if (myServerSocket != null && !myServerSocket.isClosed()) {
            try {
                myServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a CountDownLatch to signal when the server is ready
        CountDownLatch serverReady = new CountDownLatch(1);

        ServerSocket myServer = new ServerSocket(0);  // Bind to any available port
        int port = myServer.getLocalPort();  // Get the actual port number
        myServer.setReuseAddress(true);

        // Update connection properties with the actual port number
        socketConnectionProperties = new SocketConnectionProperties("http", "localhost", port, null, 5000);

        // Start the server thread
        server = new Thread(() -> {
            try {
                // Signal that the server is ready to accept connections
                serverReady.countDown();

                myServerSocket = myServer.accept();  // Accept a connection

                // Set up input and output streams
                BufferedReader br = new BufferedReader(new InputStreamReader(myServerSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(myServerSocket.getOutputStream());

                while (!Thread.currentThread().isInterrupted() && keepRunning) {
                    StringBuilder request = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null && !line.isEmpty()) {
                        request.append(line).append("\n");
                    }

                    // Send an HTTP response
                    out.writeBytes("HTTP/1.1 200 OK\n\n");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (myServerSocket != null && !myServerSocket.isClosed()) {
                        myServerSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        server.start();

        try {
            // Wait for the server to be ready before proceeding
            serverReady.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void tearDown() throws IOException, InterruptedException {
        keepRunning = false; // stop the threads
        if (myServerSocket != null) {
            server.interrupt();
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
        SocketConnectionCache instance1 = SocketConnectionCache.getInstance(true, 10);
        SocketConnectionCache instance2 = SocketConnectionCache.getInstance(true, 10);
        assertSame(instance1, instance2, "Instances are not the same");
    }

    @Test
    @Order(2)
    void testGetConnectionReturnsNewConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10);

        try {
            SocketConnection connection = instance.get(socketConnectionProperties);
            assertNotNull(connection, "Connection is null");

        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testReuseConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10);

        try {
            SocketConnection connection = instance.get(socketConnectionProperties);
            // Ensure the connection is established and open
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.getSocket().isClosed(), "Initial connection should be open");

            // Step 2: Reuse the connection
            instance.reuseConnection(connection);

            // Ensure the connection remains open after reuse
            assertFalse(connection.getSocket().isClosed(), "Connection should still be open after reuse");

            // Step 3: Fetch the connection again from the cache
            SocketConnection connection2 = instance.get(socketConnectionProperties);

            // Assert that the two connections are the same (i.e., reuse worked)
            assertSame(connection, connection2, "Connection should be reused");

        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(4)
    void testConnectionPoolSize() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10);

        try {
            for (int i = 0; i < 10; i++) {
                SocketConnection connection = instance.get(socketConnectionProperties);
                instance.reuseConnection(connection);
            }
            // Access the connection pool size
            Field connectionPoolField = SocketConnectionCache.class.getDeclaredField("CONNECTION_POOL");
            connectionPoolField.setAccessible(true);
            Map<SocketConnectionProperties, List<SocketConnection>> connectionPool =
                (Map<SocketConnectionProperties, List<SocketConnection>>) connectionPoolField.get(instance);
            int poolSize = connectionPool.get(socketConnectionProperties).size();
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
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, maxConnections);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    SocketConnection connection = instance.get(socketConnectionProperties);
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
            int poolSize = connectionPool.get(socketConnectionProperties).size();
            assertTrue(poolSize >= 1 && poolSize <= 5, "Connection pool size is not within the expected range (1-5)");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testKeepConnectionAliveFalse() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(false, 10);
        try {
            SocketConnection connection1 = instance.get(socketConnectionProperties);
            instance.reuseConnection(connection1);
            SocketConnection connection2 = instance.get(socketConnectionProperties);
            assertNotSame(connection1, connection2, "Connections are the same");
        } catch (IOException e) {
            fail("Test failed due to IOException: " + e.getMessage());
        } finally {
            // Clean up and close the server socket
            if (myServerSocket != null && !myServerSocket.isClosed()) {
                try {
                    myServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(7)
    void testReuseClosedConnection() {
        SocketConnectionCache.clearCache();
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10);
        try {
            SocketConnection connection = instance.get(socketConnectionProperties);
            connection.getSocket().close();
            instance.reuseConnection(connection);

            try {
                Field connectionPoolField = SocketConnectionCache.class.getDeclaredField("CONNECTION_POOL");
                connectionPoolField.setAccessible(true);
                Map<SocketConnectionProperties, List<SocketConnection>> connectionPool =
                    (Map<SocketConnectionProperties, List<SocketConnection>>) connectionPoolField.get(instance);
                int poolSize = connectionPool.get(socketConnectionProperties).size();
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
    public void testSocketReadTimeout() {
        SocketConnectionCache instance = SocketConnectionCache.getInstance(true, 10);
        try {
            SocketConnection connection = instance.get(socketConnectionProperties);
            Socket socket = connection.getSocket();
            assertEquals(5000, socket.getSoTimeout(), "Socket read timeout is not as expected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
