package com.azure.perf.test.core;

import java.util.function.Consumer;

public class MockEventProcessorTest extends EventPerfTest<PerfStressOptions> {
    private MockEventProcessor mockEventProcessor;

    public MockEventProcessorTest(PerfStressOptions perfStressOptions) {
        super(perfStressOptions);
        Consumer<MockErrorContext> errorprocessor = new Consumer<MockErrorContext>() {
            @Override
            public void accept(MockErrorContext mockErrorContext) {
                eventRaised();
                System.err.println("Got Error: " + mockErrorContext.getThrowable().getMessage());
            }
        };

        Consumer<MockEventContext> eventPrcessor = new Consumer<MockEventContext>() {
            @Override
            public void accept(MockEventContext mockEventContext) {
                errorRaised();
                System.out.println("Got event: " + mockEventContext.getEventData());
            }
        };
        mockEventProcessor = new MockEventProcessor(2, 3, errorprocessor, eventPrcessor);
    }
}
