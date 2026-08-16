// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import feign.codec.Encoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for issue
 * <a href="https://github.com/Azure/azure-sdk-for-java/issues/49441">#49441</a>:
 * using {@code azure-spring-data-cosmos} together with OpenFeign on Spring Boot 4 failed at startup with
 * {@code IllegalStateException: No bean found of type interface feign.codec.Encoder}.
 * <p>
 * Root cause: OpenFeign's {@code FeignClientsConfiguration} registers an {@link Encoder} via two mutually
 * exclusive paths:
 * <ul>
 *     <li>{@code feignEncoder} - guarded by
 *     {@code @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")}, which is skipped because
 *     {@code azure-spring-data-cosmos} transitively brings {@code spring-data-commons} (and therefore
 *     {@code Pageable}) onto the classpath.</li>
 *     <li>{@code feignEncoderPageable} (in the nested {@code SpringDataConfiguration}) - guarded by
 *     {@code @ConditionalOnClass({ Pageable.class, DataWebProperties.class })}. In Spring Boot 4
 *     {@code DataWebProperties} moved from {@code spring-data-commons} to {@code spring-boot-data-commons}, so
 *     this path was skipped when {@code spring-boot-data-commons} was missing from the classpath.</li>
 * </ul>
 * With neither path firing, no {@link Encoder} bean was created and the application context failed to start.
 * <p>
 * The fix adds the {@code spring-boot-data-commons} dependency so that {@code DataWebProperties} is on the
 * classpath, allowing the {@code SpringDataConfiguration} path to fire and register a
 * {@link PageableSpringEncoder}. This test loads OpenFeign's {@link FeignClientsConfiguration} on the
 * {@code azure-spring-data-cosmos} classpath and asserts that the {@link Encoder} bean is present and is a
 * {@link PageableSpringEncoder}. If the {@code spring-boot-data-commons} dependency is removed, the
 * {@code Encoder} bean disappears and this test fails.
 */
public class OpenFeignEncoderBeanRegistrationUnitTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(FeignClientsConfiguration.class);

    @Test
    public void encoderBeanIsRegistered() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(Encoder.class));
    }

    @Test
    public void encoderBeanIsPageableSpringEncoder() {
        contextRunner.run(context -> assertThat(context.getBean(Encoder.class)).isInstanceOf(PageableSpringEncoder.class));
    }
}
