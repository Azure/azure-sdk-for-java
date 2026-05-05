package com.azure.cosmos.avadtest.reconciliation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Append-only event log for reconciliation.
 *
 * Format per line:
 *   correlationId,seqNo,opType,partitionKey,timestamp,lsn[,crts]
 *
 * Ingestor writes: correlationId,seqNo,opType,partitionKey,timestamp,
 * LV reader writes: correlationId,seqNo,opType,partitionKey,timestamp,lsn
 * AVAD reader writes: correlationId,seqNo,opType,partitionKey,timestamp,lsn,crts
 */
public final class EventLog implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(EventLog.class);

    private final BufferedWriter writer;
    private final ReentrantLock lock = new ReentrantLock();

    public EventLog(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        this.writer = Files.newBufferedWriter(path,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        log.info("EventLog opened: {}", path.toAbsolutePath());
    }

    public void logProduced(String correlationId, long seqNo, String opType,
                            String partitionKey, String timestamp) {
        writeLine(String.format("%s,%d,%s,%s,%s,", correlationId, seqNo, opType, partitionKey, timestamp));
    }

    public void logConsumed(String correlationId, long seqNo, String opType,
                            String partitionKey, String timestamp, long lsn) {
        writeLine(String.format("%s,%d,%s,%s,%s,%d", correlationId, seqNo, opType, partitionKey, timestamp, lsn));
    }

    public void logConsumedAvad(String correlationId, long seqNo, String opType,
                                String partitionKey, String timestamp, long lsn, long crts) {
        writeLine(String.format("%s,%d,%s,%s,%s,%d,%d", correlationId, seqNo, opType, partitionKey, timestamp, lsn, crts));
    }

    private void writeLine(String line) {
        lock.lock();
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to write event log", e);
        } finally {
            lock.unlock();
        }
    }

    public void flush() {
        lock.lock();
        try {
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to flush event log", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }
}
