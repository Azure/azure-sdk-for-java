// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.ListHookOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class HookTest extends HookTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createEmailHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        Hook createdHook  = client.createHook(CreateEmailHookInput.INSTANCE.hook);
        Assertions.assertNotNull(createdHook);
        assertCreateEmailHookOutput(createdHook);
        client.deleteHook(createdHook.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createWebHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        Hook createdHook  = client.createHook(CreateWebHookInput.INSTANCE.hook);
        Assertions.assertNotNull(createdHook);
        assertCreateWebHookOutput(createdHook);
        client.deleteHook(createdHook.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void testListHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        String[] hookId = new String[2];
        Hook hook1 = client.createHook(ListHookInput.INSTANCE.emailHook);
        hookId[0] = hook1.getId();

        Hook hook2 = client.createHook(ListHookInput.INSTANCE.webHook);
        hookId[1] = hook2.getId();

        Assertions.assertNotNull(hookId[0]);
        Assertions.assertNotNull(hookId[1]);

        List<Hook> hookList = client.listHooks()
            .stream()
            .collect(Collectors.toList());

        assertListHookOutput(hookList);

        List<PagedResponse<Hook>> hookPageList
            = client.listHooks(new ListHookOptions().setTop(ListHookInput.INSTANCE.pageSize), Context.NONE)
            .streamByPage()
            .collect(Collectors.toList());

        assertPagedListHookOutput(hookPageList);

        client.deleteHook(hookId[0]);
        client.deleteHook(hookId[1]);
    }
}
