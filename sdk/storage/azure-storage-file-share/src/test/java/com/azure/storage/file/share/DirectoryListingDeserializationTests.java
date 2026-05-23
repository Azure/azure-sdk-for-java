// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.file.share.models.NfsFileType;
import com.azure.storage.file.share.models.ShareFileItem;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryListingDeserializationTests {
    private static final String LIST_FILES_AND_DIRECTORIES_RESPONSE = "testfiles/ListFilesAndDirectoriesResponse.xml";

    @Test
    public void listFilesAndDirectoriesNfsAllItemTypes() throws Exception {
        String xml = readResource(LIST_FILES_AND_DIRECTORIES_RESPONSE);
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, 200, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"),
                xml.getBytes(StandardCharsets.UTF_8))))
            .build();

        ShareDirectoryClient directory = new ShareFileClientBuilder().endpoint("https://account.file.core.windows.net")
            .shareName("myshare")
            .resourcePath("mydir")
            .serviceVersion(ShareServiceVersion.V2026_12_06)
            .pipeline(httpPipeline)
            .buildDirectoryClient();

        List<ShareFileItem> items = directory.listFilesAndDirectories().stream().collect(Collectors.toList());

        assertAllNfsItemTypes(items);
    }

    @Test
    public void listFilesAndDirectoriesNfsAllItemTypesAsync() throws Exception {
        String xml = readResource(LIST_FILES_AND_DIRECTORIES_RESPONSE);
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, 200, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"),
                xml.getBytes(StandardCharsets.UTF_8))))
            .build();

        ShareDirectoryAsyncClient directory
            = new ShareFileClientBuilder().endpoint("https://account.file.core.windows.net")
                .shareName("myshare")
                .resourcePath("mydir")
                .serviceVersion(ShareServiceVersion.V2026_12_06)
                .pipeline(httpPipeline)
                .buildDirectoryAsyncClient();

        List<ShareFileItem> items = directory.listFilesAndDirectories().collectList().block();

        assertAllNfsItemTypes(items);
    }

    private static void assertAllNfsItemTypes(List<ShareFileItem> items) {
        assertEquals(10, items.size());

        ShareFileItem dir = getItemByFileType(items, NfsFileType.DIRECTORY);
        assertTrue(dir.isDirectory());
        assertEquals("subdir", dir.getName());
        assertEquals("12682206919419625485", dir.getId());
        assertEquals(Long.valueOf(2), dir.getLinkCount());
        assertNull(dir.getFileSize());
        assertPosixProperties(dir, "1000", "1000", "0755");
        assertEquals("\"0x8DEAF1479E1C087\"", dir.getProperties().getETag());

        List<ShareFileItem> files = items.stream()
            .filter(item -> !item.isDirectory() && item.getFileType() == null)
            .collect(Collectors.toList());
        assertEquals(4, files.size());
        files.forEach(file -> {
            assertFalse(file.isDirectory());
            assertPosixProperties(file, "1000", "1000", "0644");
        });

        assertListedFileItem(files.get(0), "hardlink.txt", "13835128424026472451", 80L, 2L);
        assertListedFileItem(files.get(1), "regular.txt", "13835128424026472451", 80L, 2L);
        assertListedFileItem(files.get(2), "regularClose.txt", "9799903157902508049", 14L, 1L);
        assertListedFileItem(files.get(3), "regularOpen.txt", "17293892937847013391", 14L, 1L);

        ShareFileItem symlink = getItemByFileType(items, NfsFileType.SYM_LINK);
        assertFalse(symlink.isDirectory());
        assertEquals("symlink.txt", symlink.getName());
        assertEquals("10376363910205931529", symlink.getId());
        assertEquals(Long.valueOf(1), symlink.getLinkCount());
        assertEquals("/mnt/s2/dir2/regular.txt", symlink.getLinkText());
        assertPosixProperties(symlink, "1000", "1000", "0777");

        ShareFileItem block = getItemByFileType(items, NfsFileType.BLOCK_DEVICE);
        assertEquals("block_device", block.getName());
        assertEquals("10952824662509355033", block.getId());
        assertEquals(Long.valueOf(1), block.getLinkCount());
        assertEquals(Long.valueOf(8), block.getDeviceMajor());
        assertEquals(Long.valueOf(0), block.getDeviceMinor());
        assertPosixProperties(block, "0", "0", "0640");

        ShareFileItem charDev = getItemByFileType(items, NfsFileType.CHARACTER_DEVICE);
        assertEquals("char_device", charDev.getName());
        assertEquals("16717432185543589911", charDev.getId());
        assertEquals(Long.valueOf(1), charDev.getLinkCount());
        assertEquals(Long.valueOf(1), charDev.getDeviceMajor());
        assertEquals(Long.valueOf(7), charDev.getDeviceMinor());
        assertPosixProperties(charDev, "0", "0", "0644");

        ShareFileItem fifo = getItemByFileType(items, NfsFileType.FIFO);
        assertEquals("fifo_pipe", fifo.getName());
        assertEquals("14988049928633319435", fifo.getId());
        assertEquals(Long.valueOf(1), fifo.getLinkCount());
        assertNull(fifo.getDeviceMajor());
        assertNull(fifo.getDeviceMinor());
        assertNull(fifo.getLinkText());
        assertPosixProperties(fifo, "1000", "1000", "0644");

        ShareFileItem socket = getItemByFileType(items, NfsFileType.SOCKET);
        assertEquals("unix_socket", socket.getName());
        assertEquals("16429201809391878183", socket.getId());
        assertEquals(Long.valueOf(1), socket.getLinkCount());
        assertNull(socket.getDeviceMajor());
        assertNull(socket.getDeviceMinor());
        assertNull(socket.getLinkText());
        assertPosixProperties(socket, "0", "0", "0755");
    }

    private static ShareFileItem getItemByFileType(List<ShareFileItem> items, NfsFileType fileType) {
        return items.stream()
            .filter(item -> fileType.equals(item.getFileType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected listed item not found for type: " + fileType));
    }

    private static void assertListedFileItem(ShareFileItem item, String name, String id, long fileSize,
        long linkCount) {
        assertEquals(name, item.getName());
        assertEquals(id, item.getId());
        assertEquals(Long.valueOf(fileSize), item.getFileSize());
        assertEquals(Long.valueOf(linkCount), item.getLinkCount());
    }

    private static void assertPosixProperties(ShareFileItem item, String owner, String group, String fileMode) {
        assertNotNull(item.getProperties());
        assertEquals(owner, item.getProperties().getOwner());
        assertEquals(group, item.getProperties().getGroup());
        assertEquals(fileMode, item.getProperties().getFileMode());
    }

    private static String readResource(String resourceName) throws Exception {
        URL resource = DirectoryListingDeserializationTests.class.getClassLoader().getResource(resourceName);
        assertNotNull(resource);
        return new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
    }
}
