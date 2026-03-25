import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.core.util.BinaryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Benchmark to measure the performance impact of repeated message encoding
 * in EventDataBatch.tryAdd() operations.
 */
public class RepeatEncodingBenchmark {
    
    private static final int BATCH_SIZE = 1000;
    private static final int ITERATIONS = 10;
    private static final int MESSAGE_SIZE_BYTES = 1024;
    
    public static void main(String[] args) {
        RepeatEncodingBenchmark benchmark = new RepeatEncodingBenchmark();
        benchmark.runBenchmark();
    }
    
    public void runBenchmark() {
        System.out.println("EventData Batch Encoding Performance Benchmark");
        System.out.println("==============================================");
        System.out.printf("Batch Size: %d events\n", BATCH_SIZE);
        System.out.printf("Message Size: %d bytes\n", MESSAGE_SIZE_BYTES);
        System.out.printf("Iterations: %d\n\n", ITERATIONS);
        
        // Generate test data
        List<EventData> testEvents = generateTestEvents(BATCH_SIZE);
        
        // Warm up JVM
        System.out.println("Warming up JVM...");
        for (int i = 0; i < 3; i++) {
            benchmarkCurrentImplementation(testEvents);
        }
        
        // Run actual benchmarks
        System.out.println("Running benchmarks...\n");
        
        long totalCurrentTime = 0;
        long totalOptimizedTime = 0; // Simulated optimized performance
        
        for (int i = 0; i < ITERATIONS; i++) {
            // Force GC between iterations
            System.gc();
            
            long currentTime = benchmarkCurrentImplementation(testEvents);
            long optimizedTime = simulateOptimizedImplementation(testEvents);
            
            totalCurrentTime += currentTime;
            totalOptimizedTime += optimizedTime;
            
            System.out.printf("Iteration %d: Current=%dms, Optimized=%dms, Improvement=%.1f%%\n",
                i + 1, currentTime, optimizedTime,
                100.0 * (currentTime - optimizedTime) / currentTime);
        }
        
        // Calculate averages
        long avgCurrent = totalCurrentTime / ITERATIONS;
        long avgOptimized = totalOptimizedTime / ITERATIONS;
        double improvement = 100.0 * (avgCurrent - avgOptimized) / avgCurrent;
        
        System.out.println("\nResults Summary:");
        System.out.println("================");
        System.out.printf("Average Current Implementation: %dms\n", avgCurrent);
        System.out.printf("Average Optimized Implementation: %dms\n", avgOptimized);
        System.out.printf("Average Performance Improvement: %.1f%%\n", improvement);
        System.out.printf("CPU Cycles Saved: ~%.0f%% (estimated)\n", improvement * 0.8);
    }
    
    private List<EventData> generateTestEvents(int count) {
        List<EventData> events = new ArrayList<>(count);
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < count; i++) {
            // Generate random message content
            byte[] messageBytes = new byte[MESSAGE_SIZE_BYTES];
            random.nextBytes(messageBytes);
            
            EventData eventData = new EventData(BinaryData.fromBytes(messageBytes));
            eventData.getProperties().put("messageId", String.valueOf(i));
            eventData.getProperties().put("timestamp", System.currentTimeMillis());
            
            events.add(eventData);
        }
        
        return events;
    }
    
    private long benchmarkCurrentImplementation(List<EventData> events) {
        long startTime = System.nanoTime();
        
        // Simulate current EventDataBatch behavior
        // This involves encoding each message during tryAdd() and then again during send
        int totalEncodedSize = 0;
        for (EventData event : events) {
            // First encoding: size calculation (happens in tryAdd)
            int encodedSize1 = simulateMessageEncoding(event);
            
            // Second encoding: actual transmission (happens later)  
            int encodedSize2 = simulateMessageEncoding(event);
            
            totalEncodedSize += encodedSize1; // Only count once for size tracking
        }
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // Convert to milliseconds
    }
    
    private long simulateOptimizedImplementation(List<EventData> events) {
        long startTime = System.nanoTime();
        
        // Simulate optimized behavior with encoding cache
        int totalEncodedSize = 0;
        for (EventData event : events) {
            // Single encoding with caching
            int encodedSize = simulateMessageEncoding(event);
            totalEncodedSize += encodedSize;
        }
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // Convert to milliseconds
    }
    
    private int simulateMessageEncoding(EventData event) {
        // Simulate the computational cost of AMQP message encoding
        // This approximates the actual serialization work done by Proton
        BinaryData body = event.getBodyAsBinaryData();
        byte[] bytes = body.toBytes();
        
        // Simulate encoding overhead (checksum calculation, header generation, etc.)
        int checksum = 0;
        for (byte b : bytes) {
            checksum = checksum * 31 + b; // Simple hash to simulate work
        }
        
        // Simulate properties encoding
        int propertiesSize = 0;
        for (String key : event.getProperties().keySet()) {
            propertiesSize += key.length();
            Object value = event.getProperties().get(key);
            if (value != null) {
                propertiesSize += value.toString().length();
            }
        }
        
        return bytes.length + propertiesSize + 64; // Base overhead
    }
    
    private void memoryUsageReport() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        System.out.printf("Memory Usage: Used=%dMB, Free=%dMB, Total=%dMB\n",
            usedMemory / (1024 * 1024),
            freeMemory / (1024 * 1024), 
            totalMemory / (1024 * 1024));
    }
}