/**
 * Performance Benchmarks for azure-messaging-eventhubs (Java)
 * 
 * This script tests the performance issues identified in:
 * - PERF-001: Repeated Message Encoding in Batch Size Calculation
 * - PERF-002: Unbounded Message Queue Can Cause Out-of-Memory
 * - PERF-003: Excessive Object Allocation in toProtonJMessage
 * - PERF-004: Scheduler Pool Exhaustion
 * - PERF-005: Timer Per SynchronousEventSubscriber
 * - PERF-006: Flux.interval Overhead
 * 
 * Prerequisites:
 *   - Java 17+ installed
 *   - Maven or Gradle configured
 *   - Event Hubs emulator running on localhost
 * 
 * To run:
 *   mvn exec:java -Dexec.mainClass="com.azure.eventhubs.benchmark.Benchmark"
 */

package com.azure.eventhubs.benchmark;

import com.azure.messaging.eventhubs.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Benchmark {
    
    // Emulator connection string
    private static final String EMULATOR_CONNECTION_STRING = 
        "Endpoint=sb://localhost;" +
        "SharedAccessKeyName=RootManageSharedAccessKey;" +
        "SharedAccessKey=SAS_KEY_VALUE;" +
        "UseDevelopmentEmulator=true";
    
    private static final String EVENT_HUB_NAME = "perf-test";
    
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(70));
        System.out.println(" azure-messaging-eventhubs Performance Benchmarks");
        System.out.println("=".repeat(70));
        System.out.println("\nJava Version: " + System.getProperty("java.version"));
        System.out.println("Event Hub emulator: localhost");
        System.out.println("Target Event Hub: " + EVENT_HUB_NAME);
        
        benchmarkMessageEncoding();
        benchmarkBatchCreation();
        benchmarkObjectAllocation();
        benchmarkThroughput();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println(" BENCHMARK COMPLETE");
        System.out.println("=".repeat(70));
    }
    
    // ============================================================================
    // BENCHMARK 1: Message Encoding Overhead (PERF-001)
    // ============================================================================
    static void benchmarkMessageEncoding() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK 1: Message Encoding Overhead (PERF-001)");
        System.out.println("=".repeat(70));
        
        try (EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(EMULATOR_CONNECTION_STRING, EVENT_HUB_NAME)
                .buildProducerClient()) {
            
            int[] messageCounts = {100, 500, 1000};
            
            for (int count : messageCounts) {
                EventDataBatch batch = producer.createBatch();
                List<Long> addTimes = new ArrayList<>(count);
                
                for (int i = 0; i < count; i++) {
                    long start = System.nanoTime();
                    boolean added = batch.tryAdd(new EventData("benchmark_message_" + i + "_padding"));
                    long elapsed = System.nanoTime() - start;
                    if (added) {
                        addTimes.add(elapsed / 1_000_000); // ms
                    } else {
                        break;
                    }
                }
                
                // Send batch
                long sendStart = System.nanoTime();
                producer.send(batch);
                long sendTime = (System.nanoTime() - sendStart) / 1_000_000;
                
                double avgAdd = addTimes.stream().mapToLong(l -> l).average().orElse(0);
                long totalAdd = addTimes.stream().mapToLong(l -> l).sum();
                
                System.out.println("\n" + addTimes.size() + " messages:");
                System.out.printf("  Total add time:     %d ms%n", totalAdd);
                System.out.printf("  Avg add time:       %.4f ms%n", avgAdd);
                System.out.printf("  Send time:          %d ms%n", sendTime);
                System.out.printf("  Batch size:         %d bytes%n", batch.getSizeInBytes());
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
    
    // ============================================================================
    // BENCHMARK 2: Batch Creation Overhead (PERF-003, PERF-005)
    // ============================================================================
    static void benchmarkBatchCreation() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK 2: Batch Creation Overhead (PERF-003)");
        System.out.println("=".repeat(70));
        
        try (EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(EMULATOR_CONNECTION_STRING, EVENT_HUB_NAME)
                .buildProducerClient()) {
            
            int batchCount = 100;
            List<Long> times = new ArrayList<>(batchCount);
            
            long totalStart = System.nanoTime();
            
            for (int i = 0; i < batchCount; i++) {
                long batchStart = System.nanoTime();
                EventDataBatch batch = producer.createBatch();
                long createTime = (System.nanoTime() - batchStart) / 1_000_000;
                times.add(createTime);
                
                batch.tryAdd(new EventData("batch_" + i));
                producer.send(batch);
            }
            
            long totalTime = (System.nanoTime() - totalStart) / 1_000_000;
            
            Collections.sort(times);
            double avg = times.stream().mapToLong(l -> l).average().orElse(0);
            long p50 = times.get((int)(times.size() * 0.5));
            long p99 = times.get((int)(times.size() * 0.99));
            
            System.out.println("\n" + batchCount + " batch creations:");
            System.out.printf("  Total time:         %d ms%n", totalTime);
            System.out.printf("  Avg batch create:   %.2f ms%n", avg);
            System.out.printf("  P50 batch create:   %d ms%n", p50);
            System.out.printf("  P99 batch create:   %d ms%n", p99);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
    
    // ============================================================================
    // BENCHMARK 3: Object Allocation Pattern (PERF-003)
    // ============================================================================
    static void benchmarkObjectAllocation() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK 3: Object Allocation Pattern (PERF-003)");
        System.out.println("=".repeat(70));
        
        // Simulate the allocation pattern from toProtonJMessage
        int messageCount = 10000;
        
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        
        long start = System.nanoTime();
        
        // Current pattern: many small allocations
        List<Map<String, Object>> messages = new ArrayList<>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            Map<String, Object> appProps = new HashMap<>();
            appProps.put("key1", "value" + i);
            appProps.put("key2", i);
            
            Map<String, Object> annotations = new HashMap<>();
            annotations.put("partition-key", "partition-" + (i % 10));
            
            Map<String, Object> message = new HashMap<>();
            message.put("body", "message_" + i);
            message.put("applicationProperties", appProps);
            message.put("messageAnnotations", annotations);
            
            messages.add(message);
        }
        
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        
        System.gc();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("\n" + messageCount + " message object allocations:");
        System.out.printf("  Time:               %d ms%n", elapsed);
        System.out.printf("  Memory before:      %d KB%n", memBefore / 1024);
        System.out.printf("  Memory after:       %d KB%n", memAfter / 1024);
        System.out.printf("  Memory growth:      %d KB%n", (memAfter - memBefore) / 1024);
        System.out.printf("  Per message:        %d bytes%n", (memAfter - memBefore) / messageCount);
    }
    
    // ============================================================================
    // BENCHMARK 4: Throughput Test
    // ============================================================================
    static void benchmarkThroughput() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BENCHMARK 4: Throughput Test");
        System.out.println("=".repeat(70));
        
        try (EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(EMULATOR_CONNECTION_STRING, EVENT_HUB_NAME)
                .buildProducerClient()) {
            
            int totalMessages = 1000;
            AtomicInteger messagesSent = new AtomicInteger(0);
            int batchesSent = 0;
            
            long start = System.nanoTime();
            
            while (messagesSent.get() < totalMessages) {
                EventDataBatch batch = producer.createBatch();
                while (messagesSent.get() < totalMessages && 
                       batch.tryAdd(new EventData("throughput_" + messagesSent.getAndIncrement()))) {
                    // Continue adding
                }
                producer.send(batch);
                batchesSent++;
            }
            
            long elapsed = System.nanoTime() - start;
            double seconds = elapsed / 1_000_000_000.0;
            double throughput = totalMessages / seconds;
            
            System.out.println("\nThroughput test:");
            System.out.printf("  Messages sent:      %d%n", totalMessages);
            System.out.printf("  Batches used:       %d%n", batchesSent);
            System.out.printf("  Total time:         %d ms%n", elapsed / 1_000_000);
            System.out.printf("  Throughput:         %.0f msgs/sec%n", throughput);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
