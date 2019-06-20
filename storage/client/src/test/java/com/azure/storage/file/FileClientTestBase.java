package com.azure.storage.file;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.storage.StorageTestBase;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

public abstract class FileClientTestBase extends StorageTestBase {
    final Map<String, String> basicMetadata = Collections.singletonMap("test", "metadata");
    static final String defaultText = "default";
    final ByteBuf defaultData = Unpooled.wrappedBuffer(defaultText.getBytes(StandardCharsets.UTF_8));;

    @Test
    public abstract void create();

    @Test
    public abstract void createExcessMaxSize();

    @Test
    public abstract void startCopy() throws Exception;

    @Test
    public abstract void abortCopy();

    @Test
    public abstract void downloadWithProperties();

    @Test
    public abstract void delete();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void setHttpHeaders();

    @Test
    public abstract void setMeatadata();

    @Test
    public abstract void upload();

    @Test
    public abstract void listRanges() throws Exception;

    @Test
    public abstract void listHandles();

    @Test
    public abstract void forceCloseHandles();
}
