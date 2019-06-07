package com.azure.storage.queue;

import org.junit.Test;

public final class Testing {
    @Test
    public void TestBuilder() {
        QueueServiceAsyncClient client = QueueServiceAsyncClient.builder()
            .endpoint("https://alzimmerstorage.queue.core.windows.net/sampletest?sv=2018-03-28&ss=bfqt&srt=sco&sp=rwdlacup&se=2019-06-07T13:44:47Z&st=2019-06-07T05:44:47Z&spr=https&sig=L4NTsVGwrqa1gmXTBk%2BIPhJnOBXXzpV%2BiZ23E%2BQ1dV8%3D")
            .build();

        QueueAsyncClient queueAsyncClient = client.createQueue("samplequeue");
        queueAsyncClient.enqueueMessage("Hello, Azure").block();
    }
}
