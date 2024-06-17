package com.azure.spring.testcontainers.service.connection.storage;

import com.azure.storage.blob.BlobServiceVersion;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApiVersionUtilTests {

    public static boolean apiVersionIsAfterToday(String apiVersion) {
        LocalDate date = LocalDate.from(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(apiVersion));
        return date.isAfter(LocalDate.now());
    }

    @Test
    public void test() {
        assertTrue(apiVersionIsAfterToday("2200-01-01"));
        assertFalse(apiVersionIsAfterToday(BlobServiceVersion.V2024_05_04.getVersion()));
    }
}
