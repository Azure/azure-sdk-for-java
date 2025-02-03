package com.azure.storage.common.implementation;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class StorageCrc64Measurement {

    public static void main(String[] args) throws Exception {
        //checksumTestForLargeFiles();
        //measureDurationsForFiles();
        calculatePerformance();
    }

    private static void measureDurationsForFiles() throws IOException {
        // Directory to save sample files
        String sampleDir
            = "C:\\azure-sdk-for-net\\artifacts\\bin\\StorageCrc64Measurement\\Debug\\net8.0\\SampleFiles2";
        //String sampleDir = "C:\\azure-sdk-for-net\\artifacts\\bin\\StorageCrc64Measurement\\Debug\\net8.0\\SampleFilesIntermediate";

        // File to save results
        String resultFilePath = "Results2.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath))) {
            Files.list(Paths.get(sampleDir)).forEach(filePath -> {
                try {
                    byte[] data = Files.readAllBytes(filePath);
                    String fileName = filePath.getFileName().toString();
                    long fileSize = data.length;

                    // Measure CRC64 duration
                    long[] crc64Result = measureLongFunctionExecution(() -> StorageCrc64Calculator.compute(data, 0L));
                    long crc64Value = crc64Result[0];
                    long crc64Time = crc64Result[1];

                    // Measure MD5 duration
                    String[] md5Result = measureStringFunctionExecution(() -> computeMd5InChunks(filePath.toString()));
                    String md5Value = md5Result[0];
                    long md5Time = Long.parseLong(md5Result[1]);

                    // Convert crc64Value to BigInteger and then to String
                    String crc64ValueStr = new BigInteger(Long.toUnsignedString(crc64Value)).toString();

                    // Print and write results
                    String result = String.format(
                        "File: %s, Size: %d bytes%n" + "CRC64: %s%n" + "MD5: %s%n" + "CRC64 Time: %.3f ms%n"
                            + "MD5 Time: %.3f ms%n",
                        fileName, fileSize, crc64ValueStr, md5Value, crc64Time / 1_000_000.0, md5Time / 1_000_000.0);

                    System.out.println(result);
                    writer.write(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("Results have been saved to " + Paths.get(resultFilePath).toAbsolutePath());
    }

    public static void calculatePerformance() throws IOException {
        String sampleDir
            = "C:\\azure-sdk-for-net\\artifacts\\bin\\StorageCrc64Measurement\\Debug\\net8.0\\SampleFiles2";

        // File to save results
        String resultFilePath = "Results2.txt";

        // initial dry run to load the class
        StorageCrc64Calculator.compute(new byte[1024], 0L);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(resultFilePath))) {
            // Process each file in the directory
            Files.list(Paths.get(sampleDir)).forEach(filePath -> {
                long crc64ResultLong = 0L;
                String md5Result = "";
                try {
                    byte[] data = Files.readAllBytes(filePath);
                    String fileName = filePath.getFileName().toString();
                    long fileSize = data.length;

                    List<Long> crc64Times = new ArrayList<>();
                    List<Long> md5Times = new ArrayList<>();
                    int iterations = 100;

                    for (int i = 0; i < iterations; i++) {
                        // Measure CRC64
                        long crc64Start = System.nanoTime();
                        crc64ResultLong = StorageCrc64Calculator.compute(data, 0L);
                        long crc64End = System.nanoTime();
                        crc64Times.add(crc64End - crc64Start);

                        // Measure MD5
                        long md5Start = System.nanoTime();
                        md5Result = computeMd5(data);
                        long md5End = System.nanoTime();
                        md5Times.add(md5End - md5Start);
                    }

                    double avgCrc64Time
                        = crc64Times.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1_000_000.0;
                    double avgMd5Time
                        = md5Times.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1_000_000.0;

                    double stdDevCrc64Time = calculateStandardDeviation(crc64Times) / 1_000_000.0;
                    double stdDevMd5Time = calculateStandardDeviation(md5Times) / 1_000_000.0;

                    BigInteger crc64Result = new BigInteger(Long.toUnsignedString(crc64ResultLong));

                    // Format results
                    String result = String.format(
                        "File: %s, Size: %d bytes%n" + "CRC64: %d, Avg Time: %.3f ms, Std Dev: %.3f ms%n"
                            + "MD5: %s, Avg Time: %.3f ms, Std Dev: %.3f ms%n\n",
                        fileName, fileSize, crc64Result, avgCrc64Time, stdDevCrc64Time, md5Result, avgMd5Time,
                        stdDevMd5Time);

                    // Print and write results
                    System.out.println(result);
                    writer.write(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("Results have been saved to " + Paths.get(resultFilePath).toAbsolutePath());
    }

    private static double calculateStandardDeviation(List<Long> times) {
        double mean = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = times.stream()
            .mapToLong(Long::longValue)
            .mapToDouble(time -> Math.pow(time - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * Creates a directory with sample files of varying sizes.
     */
    public static void createSampleFiles(String directory) throws IOException {
        // Ensure the directory exists
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Define file sizes in bytes (1KB, 10KB, 50KB, 1GB)
        long[] fileSizes = { 1024, 10 * 1024, 50 * 1024, 1024L * 1024 * 1024 };
        Random random = new Random();

        for (int i = 0; i < fileSizes.length; i++) {
            Path filePath = dirPath.resolve("SampleFile_" + (i + 1) + ".bin");
            try (OutputStream os = Files.newOutputStream(filePath)) {
                long remaining = fileSizes[i];
                byte[] buffer = new byte[8192];
                while (remaining > 0) {
                    int toWrite = (int) Math.min(buffer.length, remaining);
                    random.nextBytes(buffer);
                    os.write(buffer, 0, toWrite);
                    remaining -= toWrite;
                }
            }
        }
    }

    public static void checksumTestForLargeFiles() throws IOException {
        // Directory to save sample files
        //        String sampleDir = "C:\\azure-sdk-for-net\\artifacts\\bin\\StorageCrc64Measurement\\Debug\\net8.0\\LargeSampleFiles";
        String sampleDir
            = "C:\\azure-sdk-for-net\\artifacts\\bin\\StorageCrc64Measurement\\Debug\\net8.0\\SampleFilesIntermediate";

        //createLargeSampleFiles(sampleDir);

        // File to save results
        String resultFilePath = "UnalignedResults.txt";

        // initial dry run to load the class
        StorageCrc64Calculator.compute(new byte[1024], 0L);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath))) {
            Files.list(Paths.get(sampleDir)).forEach(filePath -> {
                try {
                    String fileName = filePath.getFileName().toString();
                    long fileSize = Files.size(filePath);

                    // Measure CRC64
                    long[] crc64Result = measureLongFunctionExecution(() -> computeCrc64InChunks(filePath.toString()));
                    long crc64Value = crc64Result[0];
                    long crc64Time = crc64Result[1];

                    // Convert crc64Value to BigInteger and then to String
                    String crc64ValueStr = new BigInteger(Long.toUnsignedString(crc64Value)).toString();

                    // Measure MD5
                    String[] md5Result = measureStringFunctionExecution(() -> computeMd5InChunks(filePath.toString()));
                    String md5Value = md5Result[0];
                    long md5Time = Long.parseLong(md5Result[1]);

                    // Print and write results
                    String result = String.format(
                        "File: %s, Size: %d bytes%n" + "CRC64: %s%n" + "CRC64 Long Value: %d%n" + "MD5: %s%n"
                            + "CRC64 Time: %.3f ms%n" + "MD5 Time: %.3f ms%n",
                        fileName, fileSize, crc64ValueStr, crc64Value, md5Value, crc64Time / 1_000_000.0,
                        md5Time / 1_000_000.0);

                    System.out.println(result);
                    writer.write(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("Results have been saved to " + resultFilePath);
    }

    private static long[] measureLongFunctionExecution(Supplier<Long> function) {
        Instant start = Instant.now();
        long result = function.get();
        Instant end = Instant.now();
        return new long[] { result, Duration.between(start, end).toNanos() };
    }

    private static String[] measureStringFunctionExecution(Supplier<String> function) {
        Instant start = Instant.now();
        String result = function.get();
        Instant end = Instant.now();
        return new String[] { result, String.valueOf(Duration.between(start, end).toNanos()) };
    }

    public static long computeCrc64InChunks(String filePath) {
        final int bufferSize = 8192;
        long crc64 = 0L;
        long lastChunkCrc = 0L;
        boolean first = true;

        int chunks = 0;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                byte[] data = byteBuffer.array();
                if (data.length != bufferSize)
                    System.out.println("Data length: " + data.length);
                //crc64 = StorageCrc64Calculator.compute(data, crc64);
                crc64 = StorageCrc64Calculator.compute(Arrays.copyOfRange(buffer, 0, bytesRead), crc64);

                chunks++;
                lastChunkCrc = crc64;
                if (first) {
                    //System.out.println("Intermediate CRC64: " + lastChunkCrc);
                    System.out.println("Intermediate CRC64: " + Long.toUnsignedString(lastChunkCrc));
                    System.out.println("Current chunk: " + chunks);
                    System.out.println("Bytes read: " + bytesRead);
                    //                    first = false;
                }
            }
            System.out.println("Chunks: " + chunks);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastChunkCrc;
    }

    public static String computeMd5InChunks(String filePath) {
        final int bufferSize = 8192;

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    md5.update(buffer, 0, bytesRead);
                }
            }

            byte[] hash = md5.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * Computes the MD5 hash of the provided data.
     */
    public static String computeMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
