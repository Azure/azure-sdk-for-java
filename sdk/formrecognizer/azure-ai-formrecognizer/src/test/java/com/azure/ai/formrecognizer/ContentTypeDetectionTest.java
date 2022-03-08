// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import static com.azure.ai.formrecognizer.implementation.Utility.detectContentType;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for automatically detection of content type.
 */
public class ContentTypeDetectionTest {

    /**
     * Test for JPG file content type detection for {@link Utility#detectContentType} method.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void jpgContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/contoso-allinone.jpg");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertEquals(ContentType.IMAGE_JPEG, detectContentType(buffer).block());
    }

    /**
     * Test for PDF file content type detection for {@link Utility#detectContentType} method.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void pdfContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/Invoice_6.pdf");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertEquals(ContentType.APPLICATION_PDF, detectContentType(buffer).block());
    }

    /**
     * Test for PNG file content type detection for {@link Utility#detectContentType} method.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void pngContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/pngFile.png");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertEquals(ContentType.IMAGE_PNG, detectContentType(buffer).block());
    }

    /**
     * Test for little-endian TIFF file content type detection for {@link Utility#detectContentType} method.
     * File header must begin with: 49, 49, 2a, 0 in hex value, which is 73, 73, 42, 0 in decimal.
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void tiffLittleEndianContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/cell.tif");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertEquals(ContentType.IMAGE_TIFF, detectContentType(buffer).block());
    }

    /**
     * Test for big-endian TIFF content type detection for {@link Utility#detectContentType} method.
     * No file available. Input must begin with: 4D 4D 00 2A in hex value, which is 77, 77, 0, 42 in decimal
     */
    @Test
    public void tiffBigEndianContentDetectionTest() {
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(new byte[]{0x4D, 0x4D, 0x00, 0x2A}));
        assertEquals(ContentType.IMAGE_TIFF, detectContentType(buffer).block());
    }

    /**
     * Test for not supported file content type detection for {@link Utility#detectContentType} method.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void notSupportContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/docFile.doc");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertThrows(RuntimeException.class, () -> detectContentType(buffer).block());
    }

    /**
     * Test for BMP file content type detection for {@link Utility#detectContentType} method.
     *
     * @throws IOException if an I/O error occurs reading from the stream
     */
    @Test
    public void bmpContentDetectionTest() throws IOException {
        File sourceFile = new File("src/test/resources/sample_files/Test/sample_bmp.bmp");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        assertEquals(ContentType.IMAGE_BMP, detectContentType(buffer).block());
    }
}
