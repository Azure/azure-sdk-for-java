package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.PortPolicy;
import com.microsoft.rest.v2.policy.ProtocolPolicy;
import com.microsoft.rest.v2.policy.RetryPolicy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpPipelineBuilderTests {
    @Test
    public void constructorWithNoArguments() {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder();
        assertEquals(0, builder.requestPolicyFactories().size());
        assertNull(builder.options());
    }

    @Test
    public void withRequestPolicy() {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder();

        builder.withRequestPolicy(new PortPolicy.Factory(80));
        assertEquals(1, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());

        builder.withRequestPolicy(new ProtocolPolicy.Factory("ftp"));
        assertEquals(2, builder.requestPolicyFactories().size());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());

        builder.withRequestPolicy(new RetryPolicy.Factory());
        assertEquals(3, builder.requestPolicyFactories().size());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());
    }

    @Test
    public void withRequestPolicyWithIndex() {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder();

        builder.withRequestPolicy(0, new PortPolicy.Factory(80));
        assertEquals(1, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());

        builder.withRequestPolicy(0, new ProtocolPolicy.Factory("ftp"));
        assertEquals(2, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());

        builder.withRequestPolicy(1, new RetryPolicy.Factory());
        assertEquals(3, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());
    }

    @Test
    public void withRequestPolicyBefore() {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder();

        builder.withRequestPolicyBefore(RetryPolicy.Factory.class, new PortPolicy.Factory(80));
        assertEquals(1, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());

        builder.withRequestPolicyBefore(RetryPolicy.Factory.class, new ProtocolPolicy.Factory("ftp"));
        assertEquals(2, builder.requestPolicyFactories().size());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());

        builder.withRequestPolicyBefore(ProtocolPolicy.Factory.class, new RetryPolicy.Factory());
        assertEquals(3, builder.requestPolicyFactories().size());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());

        builder.withRequestPolicyBefore(PortPolicy.Factory.class, new LoggingPolicy.Factory(LoggingPolicy.LogLevel.BODY));
        assertEquals(4, builder.requestPolicyFactories().size());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());
        assertEquals(LoggingPolicy.Factory.class, builder.requestPolicyFactories().get(3).getClass());
    }

    @Test
    public void withRequestPolicyAfter() {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder();

        builder.withRequestPolicyAfter(RetryPolicy.Factory.class, new PortPolicy.Factory(80));
        assertEquals(1, builder.requestPolicyFactories().size());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());

        builder.withRequestPolicyAfter(RetryPolicy.Factory.class, new ProtocolPolicy.Factory("ftp"));
        assertEquals(2, builder.requestPolicyFactories().size());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());

        builder.withRequestPolicyAfter(ProtocolPolicy.Factory.class, new RetryPolicy.Factory());
        assertEquals(3, builder.requestPolicyFactories().size());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());

        builder.withRequestPolicyAfter(PortPolicy.Factory.class, new LoggingPolicy.Factory(LoggingPolicy.LogLevel.BODY));
        assertEquals(4, builder.requestPolicyFactories().size());
        assertEquals(RetryPolicy.Factory.class, builder.requestPolicyFactories().get(0).getClass());
        assertEquals(ProtocolPolicy.Factory.class, builder.requestPolicyFactories().get(1).getClass());
        assertEquals(LoggingPolicy.Factory.class, builder.requestPolicyFactories().get(2).getClass());
        assertEquals(PortPolicy.Factory.class, builder.requestPolicyFactories().get(3).getClass());
    }
}
