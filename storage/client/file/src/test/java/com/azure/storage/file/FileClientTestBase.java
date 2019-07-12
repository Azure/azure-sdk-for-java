// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.test.TestBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

public abstract class FileClientTestBase extends TestBase {
    final Map<String, String> basicMetadata = Collections.singletonMap("test", "metadata");
    static final String DEFAULT_TEXT = "default";
    final ByteBuf defaultData = Unpooled.wrappedBuffer(DEFAULT_TEXT.getBytes(StandardCharsets.UTF_8));

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Gets the name of the current test being run.
     * <p>
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns {@code
     * null}. See https://stackoverflow.com/a/16113631/4220757.
     *
     * @return The name of the current test.
     */
    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Test
    public abstract void createFromFileClient();

    @Test
    public abstract void createExcessMaxSizeFromFileClient();

    @Test
    public abstract void startCopy() throws Exception;

    @Test
    public abstract void abortCopy();

    @Test
    public abstract void downloadWithProperties();

    @Test
    public abstract void uploadToStorageAndDownloadToFile() throws Exception;

    @Test
    public abstract void deleteFromFileClient();

    @Test
    public abstract void getPropertiesFromFileClient();

    @Test
    public abstract void setHttpHeadersFromFileClient();

    @Test
    public abstract void setMeatadataFromFileClient();

    @Test
    public abstract void upload();

    @Test
    public abstract void listRangesFromFileClient() throws Exception;

    @Test
    public abstract void listHandlesFromFileClient();

    @Test
    public abstract void forceCloseHandlesFromFileClient();
}
