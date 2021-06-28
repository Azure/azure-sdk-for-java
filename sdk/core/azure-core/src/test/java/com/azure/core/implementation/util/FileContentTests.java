// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests {@link FileContent}.
 */
public class FileContentTests {
    @Test
    public void fileChannelOpenErrorReturnsReactively() {
        Path notARealPath = Paths.get("fake");
        FileContent fileContent = new FileContent(notARealPath, 0, 1024, 8092);

        StepVerifier.create(fileContent.asFluxByteBuffer())
            .verifyError(IOException.class);
    }

    @Test
    public void fileChannelCloseErrorReturnsReactively() throws IOException {
        MyFileChannel myFileChannel = spy(MyFileChannel.class);
        when(myFileChannel.map(any(), anyLong(), anyLong())).thenReturn(mock(MappedByteBuffer.class));
        doThrow(IOException.class).when(myFileChannel).implCloseChannel();

        FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
        when(fileSystemProvider.newFileChannel(any(), any(), any())).thenReturn(myFileChannel);

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);

        Path path = mock(Path.class);
        when(path.getFileSystem()).thenReturn(fileSystem);

        FileContent fileContent = new FileContent(path, 0, 1024, 8092);
        StepVerifier.create(fileContent.asFluxByteBuffer())
            .thenConsumeWhile(Objects::nonNull)
            .verifyError(IOException.class);
    }

    @Test
    public void fileChannelIsClosedWhenMapErrors() throws IOException {
        MyFileChannel myFileChannel = spy(MyFileChannel.class);
        when(myFileChannel.map(any(), anyLong(), anyLong())).thenThrow(IOException.class);

        FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
        when(fileSystemProvider.newFileChannel(any(), any(), any())).thenReturn(myFileChannel);

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);

        Path path = mock(Path.class);
        when(path.getFileSystem()).thenReturn(fileSystem);

        FileContent fileContent = new FileContent(path, 0, 1024, 8092);
        StepVerifier.create(fileContent.asFluxByteBuffer())
            .thenConsumeWhile(Objects::nonNull)
            .verifyError(IOException.class);

        assertFalse(myFileChannel.isOpen());
    }
}
