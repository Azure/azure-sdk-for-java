// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.platformvalidation.fluent.PlatformValidationManagementClient;
import com.azure.resourcemanager.platformvalidation.fluent.models.CloudValidationInner;
import com.azure.resourcemanager.platformvalidation.fluent.models.ExecutionPlanRunInner;
import com.azure.resourcemanager.platformvalidation.fluent.models.ValidationExecutionPlanInner;
import com.azure.resourcemanager.platformvalidation.fluent.models.ValidationTestRunInner;
import com.azure.resourcemanager.platformvalidation.implementation.PlatformValidationManagementClientBuilder;
import com.azure.resourcemanager.platformvalidation.models.CloudValidationProperties;
import com.azure.resourcemanager.platformvalidation.models.CloudValidationUpdate;
import com.azure.resourcemanager.platformvalidation.models.CloudValidationUpdateProperties;
import com.azure.resourcemanager.platformvalidation.models.ExecutionPlanRunProperties;
import com.azure.resourcemanager.platformvalidation.models.TestRunSummary;
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanProperties;
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanUpdate;
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanUpdateProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.policy.HttpDebugLoggingPolicy;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlatformValidationLiveTests extends ResourceManagerTestProxyTestBase {
    private static final Duration OPERATION_TIMEOUT = Duration.ofMinutes(10);
    private static final String BOOT_TEST = "compute-vmimagevalidation-vm-boot-test";
    private static final String TEST_ID = "/providers/Microsoft.PlatformValidation/validationTests/" + BOOT_TEST;
    private static final Configuration CONFIG = Configuration.getGlobalConfiguration();
    static final String PLAYBACK_IMAGE = "https://example.invalid/image.vhd";
    static final String IMAGE_URL_REGEX = "https://[^\\s\"\\\\<>]+";
    static final String SIGNED_URL_REGEX = "https://[^\\s\"\\\\<>]+\\?[^\\s\"\\\\<>]+";
    private static final String SUBSCRIPTION_REGEX = "(?i)(?<=/subscriptions/)[^/\"\\\\?\\s]+";
    private final ProviderRegistrationPolicy registrationPolicy = new ProviderRegistrationPolicy();
    private final AtomicReference<String[]> operationTarget = new AtomicReference<>();
    private PlatformValidationManagementClient client;
    private ResourceManager resources;
    private String group;
    private String location;
    private String subscription;
    private boolean createManagedGroup;

    public PlatformValidationLiveTests() {
        addSanitizers(
            // Signed URLs can also appear in service errors and execution results, outside the embedded plan JSON.
            new TestProxySanitizer(proxyRegex(SIGNED_URL_REGEX), PLAYBACK_IMAGE, TestProxySanitizerType.BODY_REGEX),
            new TestProxySanitizer("$..planConfigurationJson", proxyRegex(IMAGE_URL_REGEX), PLAYBACK_IMAGE,
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..planConfigurationSnapshot", proxyRegex(IMAGE_URL_REGEX), PLAYBACK_IMAGE,
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..inputsJson", proxyRegex(IMAGE_URL_REGEX), PLAYBACK_IMAGE,
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,}", REDACTED_VALUE,
                TestProxySanitizerType.BODY_REGEX),
            new TestProxySanitizer(proxyRegex(SUBSCRIPTION_REGEX), "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.URL),
            new TestProxySanitizer(proxyRegex(SUBSCRIPTION_REGEX), "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.BODY_REGEX),
            new TestProxySanitizer("x-ms-operation-identifier", null, REDACTED_VALUE, TestProxySanitizerType.HEADER),
            new TestProxySanitizer("$..tenantId", null, "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..principalId", null, "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..createdBy", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..lastModifiedBy", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY));
        for (String header : Arrays.asList("Azure-AsyncOperation", "Operation-Location", "Location")) {
            addSanitizers(new TestProxySanitizer(header, proxyRegex(SUBSCRIPTION_REGEX),
                "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.HEADER));
        }
    }

    static String proxyRegex(String regex) {
        // This framework version interpolates regex strings directly into the proxy's JSON request.
        return regex.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        List<HttpPipelinePolicy> testPolicies = new ArrayList<>(policies);
        // The framework's default body logger would expose the image SAS before recording sanitization.
        testPolicies.removeIf(HttpDebugLoggingPolicy.class::isInstance);
        testPolicies.add(registrationPolicy);
        testPolicies.add((context, next) -> next.process().map(response -> {
            if (context.getHttpRequest().getHttpMethod() == HttpMethod.PATCH) {
                String target = response.getHeaderValue("Azure-AsyncOperation");
                if (target == null) {
                    target = response.getHeaderValue("Operation-Location");
                }
                operationTarget.set(operationStatusTarget(target));
            }
            return response;
        }));
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null,
            new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE), null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), testPolicies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        AzureProfile serviceProfile = isPlaybackMode()
            ? profile
            : new AzureProfile(profile.getTenantId(), profile.getSubscriptionId(),
                testEnvironment(CONFIG.get("PLATFORMVALIDATION_ARM_ENDPOINT")));
        subscription = serviceProfile.getSubscriptionId();
        group
            = testResourceNamer.recordValueFromConfig(isPlaybackMode() ? null : required("AZURE_RESOURCE_GROUP_NAME"));
        location
            = testResourceNamer.recordValueFromConfig(CONFIG.get("PLATFORMVALIDATION_LOCATION", "southcentralusstg"));
        createManagedGroup = Boolean.parseBoolean(
            testResourceNamer.recordValueFromConfig(CONFIG.get("PLATFORMVALIDATION_CREATE_MANAGED_GROUP", "false")));
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        resources = ResourceManager.authenticate(httpPipeline, serviceProfile).withSubscription(subscription);
        registrationPolicy.setProviders(resources.providers());
        client = new PlatformValidationManagementClientBuilder().pipeline(httpPipeline)
            .endpoint(serviceProfile.getEnvironment().getResourceManagerEndpoint())
            .subscriptionId(subscription)
            .defaultPollInterval(isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(5))
            .buildClient();
        Assertions.assertTrue(resources.resourceGroups().contain(group), "Provide a dedicated test resource group.");
    }

    @Override
    protected void cleanUpResources() {
        // Scenarios delete only their own children on success and retain failure evidence, never the supplied group.
    }

    @Test
    public void cloudValidationLifecycle() {
        safeServiceCall(() -> {
            String name = name("jcv");
            CloudValidationInner created = createCloudValidation(name);
            assertCloudValidation(created, name, "Java SDK live test");
            assertCloudValidation(client.getCloudValidations().getByResourceGroup(group, name), name,
                "Java SDK live test");
            Assertions.assertEquals(1,
                client.getCloudValidations()
                    .listByResourceGroup(group)
                    .stream()
                    .filter(item -> created.id().equalsIgnoreCase(item.id()))
                    .count());
            Assertions.assertEquals(1,
                client.getCloudValidations()
                    .list()
                    .stream()
                    .filter(item -> created.id().equalsIgnoreCase(item.id()))
                    .count());
            CloudValidationInner patched = complete(client.getCloudValidations()
                .beginUpdate(group, name, new CloudValidationUpdate().withTags(tags(name, "patched"))
                    .withProperties(new CloudValidationUpdateProperties().withDescription("Java SDK PATCH"))));
            assertCloudValidation(patched, name, "Java SDK PATCH");
            Assertions.assertEquals(tags(name, "patched"), patched.tags());
            assertOperationSucceeded();
            assertCloudValidation(client.getCloudValidations().getByResourceGroup(group, name), name, "Java SDK PATCH");
            CloudValidationInner replaced = complete(client.getCloudValidations()
                .beginCreateOrUpdate(group, name,
                    new CloudValidationInner().withLocation(location)
                        .withTags(tags(name, "put"))
                        .withProperties(new CloudValidationProperties().withDescription("Java SDK PUT"))));
            assertCloudValidation(replaced, name, "Java SDK PUT");
            Assertions.assertEquals(tags(name, "put"), replaced.tags());
            assertCloudValidation(client.getCloudValidations().getByResourceGroup(group, name), name, "Java SDK PUT");
            deleteCloudValidation(name);
        });
    }

    @Test
    public void executionPlanLifecycle() {
        String source = sourceImage();
        safeServiceCall(() -> exerciseExecution(source, false));
    }

    @Test
    public void bootExecutionLifecycle() {
        Assumptions.assumeTrue(isPlaybackMode() || CONFIG.get("PLATFORMVALIDATION_RUN_EXECUTION", false),
            "Boot execution requires explicit opt-in and an approved image fixture.");
        String source = isPlaybackMode() ? PLAYBACK_IMAGE : required("PLATFORMVALIDATION_SOURCE_VHD_URI");
        if (!isPlaybackMode()) {
            validateImageSource(source, OffsetDateTime.now());
        }
        safeServiceCall(() -> exerciseExecution(source, true));
    }

    @Test
    public void operationsList() {
        safeServiceCall(() -> Assertions.assertTrue(client.getOperations()
            .list()
            .stream()
            .anyMatch(operation -> operation.name() != null
                && operation.name().startsWith("Microsoft.PlatformValidation/"))));
    }

    @Test
    public void validationTestsList() {
        safeServiceCall(() -> Assertions.assertTrue(
            client.getValidationTests().list().stream().anyMatch(test -> BOOT_TEST.equals(test.name())),
            "Boot test should be discoverable."));
    }

    @Test
    public void validationTestGet() {
        safeServiceCall(() -> Assertions.assertEquals(BOOT_TEST, client.getValidationTests().get(BOOT_TEST).name()));
    }

    @Test
    public void validationTestVersions() {
        safeServiceCall(() -> {
            List<String> versions = client.getValidationTestVersions()
                .list(BOOT_TEST)
                .stream()
                .map(version -> version.name())
                .collect(Collectors.toList());
            Assertions.assertFalse(versions.isEmpty(), "No test version was discoverable; do not guess a version.");
            for (String version : versions) {
                Assertions.assertEquals(version, client.getValidationTestVersions().get(BOOT_TEST, version).name());
            }
        });
    }

    @Test
    public void validationTestCategories() {
        safeServiceCall(() -> {
            List<String> categories = client.getValidationTestCategories()
                .list()
                .stream()
                .map(category -> category.name())
                .collect(Collectors.toList());
            Assertions.assertFalse(categories.isEmpty(), "No category was discoverable; do not guess a category.");
            for (String category : categories) {
                Assertions.assertEquals(category, client.getValidationTestCategories().get(category).name());
            }
        });
    }

    private void exerciseExecution(String source, boolean execute) {
        String cv = name("jcv");
        String planName = name("jplan");
        String runName = name("jrun");
        String planId = cvId(cv) + "/validationExecutionPlans/" + planName;
        String runId = planId + "/executionPlanRuns/" + runName;
        String description = "Java SDK execution " + runName;
        String configuration = planConfiguration(planName, source);
        createCloudValidation(cv);
        if (createManagedGroup) {
            Assertions.assertFalse(resources.resourceGroups().contain(cv + "-mrg"),
                "Refusing to adopt a managed resource group created by another owner.");
            resources.resourceGroups().define(cv + "-mrg").withRegion(location).withTags(tags(cv, "manual")).create();
        }
        Assertions.assertTrue(resources.resourceGroups().contain(cv + "-mrg"),
            "Managed group is absent. Retaining CV; no automatic manual provisioning or RBAC fallback.");
        assertMissing(() -> client.getValidationExecutionPlans().get(group, cv, planName));
        System.out.println("JAVA_TEST_PLAN " + planId);
        ValidationExecutionPlanInner plan
            = complete(
                client.getValidationExecutionPlans()
                    .beginCreateOrUpdate(group, cv, planName, new ValidationExecutionPlanInner().withLocation(location)
                        .withTags(tags(planName, "created"))
                        .withProperties(new ValidationExecutionPlanProperties().withDescription("Java SDK boot plan")
                            .withPlanConfigurationJson(configuration))));
        assertPlan(plan, planId, configuration, "Java SDK boot plan");
        assertPlan(client.getValidationExecutionPlans().get(group, cv, planName), planId, configuration,
            "Java SDK boot plan");
        Assertions.assertEquals(1,
            client.getValidationExecutionPlans()
                .listByResourceGroup(group, cv)
                .stream()
                .filter(item -> planId.equalsIgnoreCase(item.id()))
                .count());
        ValidationExecutionPlanInner updated = complete(client.getValidationExecutionPlans()
            .beginUpdate(group, cv, planName, new ValidationExecutionPlanUpdate().withTags(tags(planName, "patched"))
                .withProperties(new ValidationExecutionPlanUpdateProperties().withDescription("Java SDK plan PATCH"))));
        assertPlan(updated, planId, configuration, "Java SDK plan PATCH");
        Assertions.assertEquals(tags(planName, "patched"), updated.tags());
        assertOperationSucceeded();
        assertPlan(client.getValidationExecutionPlans().get(group, cv, planName), planId, configuration,
            "Java SDK plan PATCH");
        if (execute) {
            assertMissing(() -> client.getExecutionPlanRuns().get(group, cv, planName, runName));
            System.out.println("JAVA_TEST_RUN " + runId);
            complete(client.getExecutionPlanRuns()
                .beginCreateOrUpdate(group, cv, planName, runName, new ExecutionPlanRunInner()
                    .withProperties(new ExecutionPlanRunProperties().withDescription(description))));
            ExecutionPlanRunInner run = waitForWorkload(cv, planName, runName);
            assertRun(run, runId, description);
            List<ExecutionPlanRunInner> runs = client.getExecutionPlanRuns()
                .listByExecutionPlan(group, cv, planName)
                .stream()
                .collect(Collectors.toList());
            Assertions.assertEquals(1, runs.size());
            assertRun(runs.get(0), runId, description);
            List<ValidationTestRunInner> tests = client.getValidationTestRuns()
                .listByExecutionPlanRun(group, cv, planName, runName)
                .stream()
                .collect(Collectors.toList());
            Assertions.assertEquals(1, tests.size());
            for (ValidationTestRunInner test : tests) {
                assertTestRun(test, runId);
                assertTestRun(client.getValidationTestRuns().get(group, cv, planName, runName, test.name()), runId);
            }
            System.out.println("JAVA_TEST_WORKLOAD_PASSED " + runId);
            complete(client.getExecutionPlanRuns().beginDelete(group, cv, planName, runName));
            assertMissing(() -> client.getExecutionPlanRuns().get(group, cv, planName, runName));
            // A missing parent does not establish child cleanup. Preserve evidence if any child survives.
            for (ValidationTestRunInner test : tests) {
                assertMissing(() -> client.getValidationTestRuns().get(group, cv, planName, runName, test.name()));
            }
        }
        Assertions.assertFalse(
            client.getExecutionPlanRuns().listByExecutionPlan(group, cv, planName).iterator().hasNext(),
            "Runs remain; refusing parent deletion.");
        plan = client.getValidationExecutionPlans().get(group, cv, planName);
        Assertions.assertEquals(planName, plan.tags().get("sdk-test"));
        assertPlan(plan, planId, configuration, "Java SDK plan PATCH");
        complete(client.getValidationExecutionPlans().beginDelete(group, cv, planName));
        assertMissing(() -> client.getValidationExecutionPlans().get(group, cv, planName));
        deleteCloudValidation(cv);
    }

    private CloudValidationInner createCloudValidation(String name) {
        assertMissing(() -> client.getCloudValidations().getByResourceGroup(group, name));
        Assertions.assertFalse(resources.resourceGroups().contain(name + "-mrg"),
            "Refusing to reuse an existing managed group.");
        System.out.println("JAVA_TEST_CV " + cvId(name));
        CloudValidationInner result = complete(client.getCloudValidations()
            .beginCreateOrUpdate(group, name,
                new CloudValidationInner().withLocation(location)
                    .withTags(tags(name, "created"))
                    .withProperties(new CloudValidationProperties().withDescription("Java SDK live test"))));
        assertCloudValidation(result, name, "Java SDK live test");
        return result;
    }

    private void deleteCloudValidation(String name) {
        CloudValidationInner current = client.getCloudValidations().getByResourceGroup(group, name);
        Assertions.assertEquals(cvId(name).toLowerCase(java.util.Locale.ROOT),
            current.id().toLowerCase(java.util.Locale.ROOT));
        Assertions.assertEquals(name, current.tags().get("sdk-test"));
        Assertions.assertEquals("Succeeded", current.properties().provisioningState().toString());
        Assertions.assertFalse(
            client.getValidationExecutionPlans().listByResourceGroup(group, name).iterator().hasNext(),
            "Plans remain; refusing parent deletion.");
        if (createManagedGroup && resources.resourceGroups().contain(name + "-mrg")) {
            Assertions.assertEquals(name, resources.resourceGroups().getByName(name + "-mrg").tags().get("sdk-test"));
            Assertions.assertFalse(resources.genericResources().listByResourceGroup(name + "-mrg").iterator().hasNext(),
                "Managed group is not empty; retaining it.");
            resources.resourceGroups().deleteByName(name + "-mrg");
            waitForManagedGroupDeletion(name + "-mrg");
        }
        deleteWithRetry(() -> complete(client.getCloudValidations().beginDelete(group, name)),
            () -> Assertions.assertFalse(
                client.getValidationExecutionPlans().listByResourceGroup(group, name).iterator().hasNext(),
                "Plans remain; refusing parent deletion."),
            () -> sleepIfRunningAgainstService(10000));
        assertMissing(() -> client.getCloudValidations().getByResourceGroup(group, name));
        waitForManagedGroupDeletion(name + "-mrg");
        System.out.println("JAVA_TEST_CV_DELETED " + cvId(name));
    }

    static void deleteWithRetry(Runnable delete, Runnable verifyNoPlans, Runnable delay) {
        for (int attempt = 0;; attempt++) {
            verifyNoPlans.run();
            try {
                delete.run();
                return;
            } catch (ManagementException error) {
                if (attempt == 29
                    || error.getResponse() == null
                    || error.getResponse().getStatusCode() != 409
                    || error.getValue() == null
                    || !"CannotDeleteResource".equals(error.getValue().getCode())) {
                    throw error;
                }
                // Child deletion can precede the service's parent-deletion eligibility check.
                delay.run();
            }
        }
    }

    private ExecutionPlanRunInner waitForWorkload(String cv, String plan, String run) {
        long deadline = System.nanoTime() + Duration.ofMinutes(30).toNanos();
        while (System.nanoTime() < deadline) {
            ExecutionPlanRunInner result = client.getExecutionPlanRuns().get(group, cv, plan, run);
            String status = String.valueOf(result.properties().status());
            if (Arrays.asList("Succeeded", "Completed", "Failed", "Canceled", "Cancelled", "TimedOut")
                .contains(status)) {
                return result;
            }
            Assertions.assertFalse(
                Arrays.asList("Failed", "Canceled").contains(String.valueOf(result.properties().provisioningState())),
                "Run provisioning failed.");
            sleepIfRunningAgainstService(10000);
        }
        throw new AssertionError("Workload did not finish in 30 minutes; all owned resources retained.");
    }

    private void waitForManagedGroupDeletion(String name) {
        long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
        int absentChecks = 0;
        while (System.nanoTime() < deadline) {
            absentChecks = resources.resourceGroups().contain(name) ? 0 : absentChecks + 1;
            if (absentChecks == 2) {
                return;
            }
            sleepIfRunningAgainstService(5000);
        }
        throw new AssertionError("Managed group deletion was not confirmed; resources retained.");
    }

    private void assertCloudValidation(CloudValidationInner value, String name, String description) {
        Assertions.assertTrue(cvId(name).equalsIgnoreCase(value.id()));
        Assertions.assertEquals(name, value.name());
        Assertions.assertEquals(name, value.tags().get("sdk-test"));
        Assertions.assertEquals(location, value.location());
        Assertions.assertEquals("Succeeded", value.properties().provisioningState().toString());
        Assertions.assertEquals(description, value.properties().description());
    }

    private static void assertPlan(ValidationExecutionPlanInner plan, String id, String configuration,
        String description) {
        Assertions.assertTrue(id.equalsIgnoreCase(plan.id()));
        Assertions.assertEquals("Succeeded", plan.properties().provisioningState().toString());
        Assertions.assertEquals(description, plan.properties().description());
        // Never include signed image URLs in assertion diagnostics.
        Assertions.assertTrue(
            BinaryData.fromString(configuration)
                .toObject(Map.class)
                .equals(BinaryData.fromString(plan.properties().planConfigurationJson()).toObject(Map.class)),
            "Plan configuration changed.");
    }

    private static void assertRun(ExecutionPlanRunInner run, String id, String description) {
        Assertions.assertTrue(id.equalsIgnoreCase(run.id()));
        Assertions.assertEquals(description, run.properties().description());
        Assertions.assertEquals("Succeeded", run.properties().provisioningState().toString());
        Assertions.assertEquals("Succeeded", run.properties().status().toString());
        Assertions.assertTrue(run.properties().error() == null, "Run returned a service error.");
        TestRunSummary summary = run.properties().testRunSummary();
        Assertions.assertNotNull(summary);
        Assertions.assertEquals("Passed", summary.overallResult().toString());
        Assertions.assertEquals(Integer.valueOf(1), summary.totalTests());
        Assertions.assertEquals(Integer.valueOf(1), summary.passedTests());
        Assertions.assertEquals(Integer.valueOf(0), summary.failedTests());
        Assertions.assertEquals(Integer.valueOf(0), summary.skippedTests());
        Assertions.assertNotNull(run.properties().startedAt());
        Assertions.assertNotNull(run.properties().completedAt());
        Assertions.assertFalse(run.properties().completedAt().isBefore(run.properties().startedAt()));
    }

    private static void assertTestRun(ValidationTestRunInner test, String runId) {
        Assertions.assertTrue((runId + "/validationTestRuns/" + test.name()).equalsIgnoreCase(test.id()));
        Assertions.assertEquals("Completed", test.properties().status().toString());
        Assertions.assertEquals("Succeeded", test.properties().provisioningState().toString());
        Assertions.assertEquals(TEST_ID, test.properties().testId());
        Assertions.assertTrue(test.properties().error() == null, "Test run returned a service error.");
        Assertions.assertNotNull(test.properties().passDetails());
        Assertions.assertFalse(test.properties().passDetails().isEmpty());
        Assertions.assertTrue(
            test.properties().failureDetails() == null || test.properties().failureDetails().isEmpty(),
            "Boot test contains failure details.");
    }

    static <T, R> R complete(SyncPoller<T, R> poller) {
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            poller.waitForCompletion(OPERATION_TIMEOUT).getStatus(), "LRO failed; resources retained.");
        return poller.getFinalResult();
    }

    static void assertMissing(Executable operation) {
        HttpResponseException error = Assertions.assertThrows(HttpResponseException.class, operation,
            "Expected HTTP 404; resource still exists. Retaining parent resources.");
        Assertions.assertNotNull(error.getResponse());
        Assertions.assertEquals(404, error.getResponse().getStatusCode());
    }

    private String cvId(String name) {
        return "/subscriptions/" + subscription + "/resourceGroups/" + group
            + "/providers/Microsoft.PlatformValidation/cloudValidations/" + name;
    }

    private void assertOperationSucceeded() {
        String[] target = operationTarget.get();
        Assertions.assertNotNull(target, "PATCH must expose a supported operation-status route.");
        Assertions.assertEquals("Succeeded", client.getOperationStatus().get(target[0], target[1]).status());
    }

    static String[] operationStatusTarget(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("/locations/([^/]+)/operationStatuses/([^/]+)$", Pattern.CASE_INSENSITIVE)
            .matcher(URI.create(url).getPath());
        return matcher.find() ? new String[] { matcher.group(1), matcher.group(2) } : null;
    }

    static AzureEnvironment testEnvironment(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return AzureEnvironment.AZURE;
        }
        URI uri = URI.create(endpoint);
        Assertions.assertTrue("https".equals(uri.getScheme())
            && uri.getHost() != null
            && uri.getRawUserInfo() == null
            && uri.getRawQuery() == null
            && uri.getRawFragment() == null
            && (uri.getPath().isEmpty() || "/".equals(uri.getPath())), "ARM endpoint must be an HTTPS origin.");
        Map<String, String> endpoints = new LinkedHashMap<>(AzureEnvironment.AZURE.getEndpoints());
        endpoints.put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(), endpoint);
        return new AzureEnvironment(endpoints);
    }

    private String name(String prefix) {
        return generateRandomResourceName(prefix, prefix.length() + 16);
    }

    private static Map<String, String> tags(String owner, String phase) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("sdk-test", owner);
        result.put("phase", phase);
        return result;
    }

    private static String required(String name) {
        String value = CONFIG.get(name);
        Assertions.assertTrue(value != null && !value.isEmpty(), name + " must be configured.");
        return value;
    }

    private String sourceImage() {
        if (isPlaybackMode()) {
            return PLAYBACK_IMAGE;
        }
        String source = CONFIG.get("PLATFORMVALIDATION_SOURCE_VHD_URI");
        Assumptions.assumeTrue(source != null && !source.isEmpty(), "An approved image fixture is required.");
        validateImageSource(source, OffsetDateTime.now());
        return source;
    }

    static void validateImageSource(String source, OffsetDateTime now) {
        URI uri;
        try {
            uri = URI.create(source);
        } catch (IllegalArgumentException error) {
            throw new AssertionError("Image fixture is not a valid URI.");
        }
        Assertions.assertEquals("https", uri.getScheme(), "Image fixture must use HTTPS.");
        Assertions.assertNotNull(uri.getHost(), "Image fixture must include a host.");
        String query = uri.getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                if (parameter.startsWith("se=")) {
                    try {
                        OffsetDateTime expiry
                            = OffsetDateTime.parse(URLDecoder.decode(parameter.substring(3), "UTF-8"));
                        Assertions.assertTrue(expiry.isAfter(now.plusHours(25)),
                            "Image SAS must remain valid for more than 25 hours (service minimum: one day).");
                    } catch (java.time.format.DateTimeParseException | UnsupportedEncodingException error) {
                        throw new AssertionError("Image SAS expiry cannot be parsed.");
                    }
                }
            }
        }
    }

    static String planConfiguration(String name, String source) {
        Map<String, Object> image = new LinkedHashMap<>();
        image.put("osType", "Linux");
        image.put("vmGenerationType", "V1");
        image.put("architectureType", "X64");
        image.put("recommendedVMSizes", Collections.singletonList("Standard_D4s_v3"));
        Map<String, Object> storage = new LinkedHashMap<>();
        storage.put("osDiskImage", Collections.singletonMap("sourceVhdUri", source));
        storage.put("dataDiskImages", Collections.emptyList());
        image.put("storageProfile", storage);
        image.put("additionalProperties", Collections.emptyMap());
        Map<String, String> step = new LinkedHashMap<>();
        step.put("name", "vm-boot-test");
        step.put("type", "test");
        step.put("testRef", TEST_ID);
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("apiVersion", "microsoft.validate/executionPlan.v0");
        document.put("kind", "ExecutionPlan");
        document.put("metadata", Collections.singletonMap("name", name));
        document.put("parameters", Collections.singletonMap("certificationPackageReference", image));
        document.put("authoring", Collections.singletonMap("steps", Collections.singletonList(step)));
        return BinaryData.fromObject(document).toString();
    }

    private static void safeServiceCall(Runnable action) {
        try {
            action.run();
        } catch (HttpResponseException error) {
            // Service messages may echo the embedded document and its image SAS. Do not attach the original cause.
            int status = error.getResponse() == null ? 0 : error.getResponse().getStatusCode();
            String requestId
                = error.getResponse() == null ? null : error.getResponse().getHeaderValue("x-ms-request-id");
            String path = error.getResponse() == null ? null : error.getResponse().getRequest().getUrl().getPath();
            String code = error instanceof ManagementException && ((ManagementException) error).getValue() != null
                ? ((ManagementException) error).getValue().getCode()
                : error.getClass().getSimpleName();
            throw new AssertionError("Service request failed: HTTP " + status + "; requestId=" + requestId + "; code="
                + code + "; path=" + path + ". Test-owned resources retained.");
        }
    }
}
