package com.azure.storage.common.resource;

import java.io.InputStream;
import java.util.List;

public interface StorageResource {

    long getLength();
    List<String> getPath();

    boolean canConsumeStream();
    boolean canProduceStream();
    boolean canConsumeUri();
    boolean canProduceUri();


    InputStream openInputStream();
    void consumeInputStream(InputStream inputStream, long length);

    String getUri();
    void consumeUri(String sasUri);

}
