// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples.utils;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.function.Supplier;

public final class Utils {
    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper
        .builder()
        .addModule(new JavaTimeModule())
        .build()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static void log(String str) {
        Date dt = new Date();
        String message = String.format("%s %d %s", LOG_DATE_FORMAT.format(dt), Thread.currentThread().getId(), str);
        System.out.println(message);
    }

    public static void pressAnyKeyToContinue(String message) {
        log(message);
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }

    public static void logObject(String message, Object object) {
        log(message + toString(object));
    }

    public static void logObject(Object object) {
        log(toString(object));
    }

    public static String toString(Object object) {
        try {
            String  jsonString = BinaryData.fromObject(object).toString();
            Object jsonObject = OBJECT_MAPPER.readValue(jsonString, Object.class);
            return OBJECT_MAPPER.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static BinaryData loadFromFile(String pathString) {
        Path path = Paths.get(pathString);
        return BinaryData.fromFile(path);
    }

    public static <T> T safelyRun(Supplier<T> suppler) {
        try {
            return suppler.get();
        } catch (RuntimeException ignore) { }
        return null;
    }

    private Utils() {}
}
