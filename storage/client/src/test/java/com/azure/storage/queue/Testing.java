package com.azure.storage.queue;

import org.junit.Test;

public final class Testing {
    @Test
    public void TestBuilder() {
        QueueServiceAsyncClient client = QueueServiceAsyncClient.builder()
            .endpoint("https://alzimmerstorage.queue.core.windows.net/?sv=2018-03-28&ss=q&srt=sco&sp=rwdlacup&se=2019-06-07T02:14:26Z&st=2019-06-06T18:14:26Z&spr=https&sig=dXbxKynbeMusT25Rgs%2BsDpbkrXSrcR5zQFnsaCR1d3I%3D")
            .build();

        QueueAsyncClient queueAsyncClient = client.createQueue("testsample");
    }
}
