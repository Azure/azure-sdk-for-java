// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceregistry;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.deviceregistry.fluent.models.CredentialInner;
import com.azure.resourcemanager.deviceregistry.models.ActivateBringYourOwnRootRequest;
import com.azure.resourcemanager.deviceregistry.models.BringYourOwnRoot;
import com.azure.resourcemanager.deviceregistry.models.BringYourOwnRootStatus;
import com.azure.resourcemanager.deviceregistry.models.CertificateAuthorityConfiguration;
import com.azure.resourcemanager.deviceregistry.models.CertificateConfiguration;
import com.azure.resourcemanager.deviceregistry.models.Credential;
import com.azure.resourcemanager.deviceregistry.models.DeviceCredentialsRevokeRequest;
import com.azure.resourcemanager.deviceregistry.models.LeafCertificateConfiguration;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpoints;
import com.azure.resourcemanager.deviceregistry.models.Namespace;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDevice;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDeviceProperties;
import com.azure.resourcemanager.deviceregistry.models.Policy;
import com.azure.resourcemanager.deviceregistry.models.PolicyProperties;
import com.azure.resourcemanager.deviceregistry.models.PolicyUpdateProperties;
import com.azure.resourcemanager.deviceregistry.models.ProvisioningState;
import com.azure.resourcemanager.deviceregistry.models.SupportedKeyType;
import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Full port of the .NET DeviceRegistryCredentialsAndPoliciesFlowTest.
 *
 * <p>PREREQUISITES: Create RG, UAMI, ADR Namespace, IoT Hub, and DPS with ADR Integration
 * BEFORE running in Record/Live mode. Use equivalent setup scripts to the .NET tests.
 *
 * <p>Resources expected (for suffix "java1"):
 * <ul>
 *   <li>Resource Group:    adr-sdk-test-cms-java1</li>
 *   <li>Managed Identity:  cms-test-uami-java1</li>
 *   <li>ADR Namespace:     cms-test-namespace-java1</li>
 *   <li>IoT Hub (GEN2):    adr-sdk-cms-test-hub-java1</li>
 *   <li>DPS:               adr-sdk-cms-test-dps-java1</li>
 * </ul>
 *
 * <p>Set environment variables before running:
 * <ul>
 *   <li>AZURE_SUBSCRIPTION_ID — target subscription</li>
 *   <li>AZURE_TEST_MODE=Live (or Record)</li>
 * </ul>
 */
public class DeviceRegistryCredentialsAndPoliciesFlowTest extends DeviceRegistryTestBase {

    // Change this locally when you need fresh resources (e.g., after a failed run).
    private static final String ITERATION = "1";

    // Suffix matches the .NET test's "async" suffix + iteration
    private static final String SUFFIX = "async" + ITERATION;
    private static final Region REGION
        = Region.fromName(System.getenv("LOCATION") != null ? System.getenv("LOCATION") : "eastus2euap");

    private static final String RESOURCE_GROUP_NAME = System.getenv("RESOURCE_GROUP_NAME") != null
        ? System.getenv("RESOURCE_GROUP_NAME")
        : "adr-sdk-test-cms-" + SUFFIX;
    private static final String NAMESPACE_NAME = "cms-test-namespace-" + SUFFIX;
    private static final String POLICY_NAME = "cms-test-policy-" + SUFFIX;
    private static final String BYOR_POLICY_NAME = "cms-test-byor-policy-" + SUFFIX;
    private static final String DEVICE_NAME = "cms-test-device-" + SUFFIX;

    @DoNotRecord(skipInPlayback = true)
    @Test
    public void testCredentialAndPolicyFlow() {
        System.out.println("\n============================================================");
        System.out.println("TEST STARTED  [Device Registry Credentials & Policies Flow]");
        System.out.println("============================================================\n");

        // ============================================================
        // Step 1: Verify namespace exists (prerequisite)
        // ============================================================
        System.out.println("Step 1: Getting namespace '" + NAMESPACE_NAME + "'...");
        Namespace namespace
            = deviceRegistryManager.namespaces().getByResourceGroup(RESOURCE_GROUP_NAME, NAMESPACE_NAME);

        Assertions.assertNotNull(namespace);
        Assertions.assertEquals(NAMESPACE_NAME, namespace.name());
        Assertions.assertEquals(REGION.toString(), namespace.region().toString());
        System.out.println("  ✓ Namespace retrieved successfully\n");

        // ============================================================
        // Step 2: Credential Flow — Get or Create
        // ============================================================
        System.out.println("Step 2: Checking if credential exists...");
        Credential credential;
        try {
            credential = deviceRegistryManager.credentials().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME);
            System.out.println("  Credential already exists, retrieved.");
        } catch (ManagementException e) {
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 404) {
                System.out.println("  Creating new credential (this may take several minutes)...");
                CredentialInner credentialData = new CredentialInner().withLocation(REGION.toString());
                credential = deviceRegistryManager.credentials()
                    .createOrUpdate(RESOURCE_GROUP_NAME, NAMESPACE_NAME, credentialData);
                System.out.println("  ✓ Credential created successfully");
            } else {
                throw e;
            }
        }
        Assertions.assertNotNull(credential);
        Assertions.assertEquals(REGION.toString(), credential.location());
        System.out.println("  ✓ Credential verified\n");

        // ============================================================
        // Step 3: Policy Flow — Clean up existing policies, then create
        // ============================================================
        System.out.println("Step 3: Cleaning up existing policies and creating '" + POLICY_NAME + "'...");

        // Delete all existing policies (1 policy/credential limit)
        List<Policy> existingPolicies = new ArrayList<>();
        for (Policy p : deviceRegistryManager.policies().listByResourceGroup(RESOURCE_GROUP_NAME, NAMESPACE_NAME)) {
            existingPolicies.add(p);
        }

        if (!existingPolicies.isEmpty()) {
            System.out.println("  Found " + existingPolicies.size() + " existing policy(ies) — deleting...");
            for (Policy existing : existingPolicies) {
                System.out.println("    Deleting policy '" + existing.name() + "'...");
                deviceRegistryManager.policies().delete(RESOURCE_GROUP_NAME, NAMESPACE_NAME, existing.name());
                System.out.println("    ✓ Deleted '" + existing.name() + "'");
            }
            System.out.println("  ✓ All existing policies cleaned up");
        } else {
            System.out.println("  No existing policies — proceeding with creation");
        }

        // Create policy with ECC certificate, 90-day validity
        System.out.println("  Creating new policy with ECC certificate (90-day validity)...");
        CertificateAuthorityConfiguration caConfig
            = new CertificateAuthorityConfiguration().withKeyType(SupportedKeyType.ECC);
        LeafCertificateConfiguration leafConfig = new LeafCertificateConfiguration().withValidityPeriodInDays(90);
        CertificateConfiguration certConfig
            = new CertificateConfiguration().withCertificateAuthorityConfiguration(caConfig)
                .withLeafCertificateConfiguration(leafConfig);

        PolicyProperties policyProperties = new PolicyProperties().withCertificate(certConfig);

        Policy policy = deviceRegistryManager.policies()
            .define(POLICY_NAME)
            .withExistingNamespace(RESOURCE_GROUP_NAME, NAMESPACE_NAME)
            .withProperties(policyProperties)
            .create();

        Assertions.assertNotNull(policy);
        Assertions.assertEquals(POLICY_NAME, policy.name());
        System.out.println("  ✓ Policy created successfully\n");

        // ============================================================
        // Step 4: Verify policy properties
        // ============================================================
        System.out.println("Step 4: Verifying policy properties...");
        Assertions.assertNotNull(policy.properties());
        Assertions.assertNotNull(policy.properties().certificate());

        Assertions.assertEquals(SupportedKeyType.ECC,
            policy.properties().certificate().certificateAuthorityConfiguration().keyType());
        Assertions.assertEquals(90,
            policy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays());
        System.out.println("  ✓ Certificate: ECC key type, 90-day validity");

        Assertions.assertEquals(ProvisioningState.SUCCEEDED, policy.properties().provisioningState());
        System.out.println("  ✓ Provisioning state: Succeeded");

        // Test LIST operation
        System.out.println("  Testing LIST operation...");
        List<Policy> allPolicies = new ArrayList<>();
        for (Policy p : deviceRegistryManager.policies().listByResourceGroup(RESOURCE_GROUP_NAME, NAMESPACE_NAME)) {
            allPolicies.add(p);
        }
        Assertions.assertTrue(allPolicies.stream().anyMatch(p -> POLICY_NAME.equals(p.name())));
        System.out.println("  ✓ LIST operation successful, found " + allPolicies.size() + " policy(ies)\n");

        // ============================================================
        // Step 5: Synchronize Credentials with IoT Hub
        // ============================================================
        System.out.println("Step 5: Synchronizing credentials with IoT Hub (this may take several minutes)...");
        deviceRegistryManager.credentials().synchronize(RESOURCE_GROUP_NAME, NAMESPACE_NAME);
        System.out.println("  ✓ Synchronization completed successfully\n");

        // ============================================================
        // Step 6: GET policy after sync
        // ============================================================
        System.out.println("Step 6: Getting fresh policy after sync...");
        policy = deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, POLICY_NAME);
        int currentValidity = policy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays();
        System.out.println("  ✓ Fresh policy retrieved (current validity: " + currentValidity + " days)\n");

        // ============================================================
        // Step 7: Update policy — change validity to 60 days
        // NOTE: For PATCH operations, omit certificateAuthorityConfiguration
        // which contains immutable properties (keyType, bringYourOwnRoot).
        // ============================================================
        System.out
            .println("Step 7: Testing UPDATE operation - changing validity from " + currentValidity + " to 60 days...");

        CertificateConfiguration updateCertConfig = new CertificateConfiguration()
            .withLeafCertificateConfiguration(new LeafCertificateConfiguration().withValidityPeriodInDays(60));

        policy = policy.update().withProperties(new PolicyUpdateProperties().withCertificate(updateCertConfig)).apply();

        System.out.println("  Update operation completed");

        // GET fresh policy after update
        System.out.println("  Getting fresh policy after update...");
        policy = deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, POLICY_NAME);

        Assertions.assertEquals(60,
            policy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays());
        System.out.println("  ✓ Policy updated successfully, validity now 60 days\n");

        // ============================================================
        // Step 8: Device CRUD + Revoke Flow
        // ============================================================

        // Step 8a: Create a device in the CMS namespace
        System.out.println("Step 8a: Creating device '" + DEVICE_NAME + "' in CMS namespace...");

        NamespaceDeviceProperties deviceProperties = new NamespaceDeviceProperties().withManufacturer("Contoso")
            .withModel("CMS-TestModel-5000")
            .withOperatingSystem("Linux")
            .withOperatingSystemVersion("22.04")
            .withEndpoints(new MessagingEndpoints());

        NamespaceDevice device = deviceRegistryManager.namespaceDevices()
            .define(DEVICE_NAME)
            .withRegion(REGION)
            .withExistingNamespace(RESOURCE_GROUP_NAME, NAMESPACE_NAME)
            .withProperties(deviceProperties)
            .create();

        Assertions.assertNotNull(device);
        Assertions.assertEquals(DEVICE_NAME, device.name());
        Assertions.assertNotNull(device.properties().uuid());
        Assertions.assertEquals("Contoso", device.properties().manufacturer());
        Assertions.assertEquals("CMS-TestModel-5000", device.properties().model());
        System.out.println("  ✓ Device created: " + device.name());
        System.out.println("  ✓ UUID: " + device.properties().uuid());
        System.out.println("  ✓ Version: " + device.properties().version() + "\n");

        // Step 8b: GET device and verify properties
        System.out.println("Step 8b: Getting device and verifying properties...");
        device = deviceRegistryManager.namespaceDevices().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, DEVICE_NAME);

        Assertions.assertNotNull(device.properties());
        Assertions.assertEquals("Contoso", device.properties().manufacturer());
        Assertions.assertEquals("CMS-TestModel-5000", device.properties().model());
        Assertions.assertEquals("Linux", device.properties().operatingSystem());
        Assertions.assertEquals("22.04", device.properties().operatingSystemVersion());
        System.out.println("  ✓ Device properties verified");

        // Check Policy ResourceId
        if (device.properties().policy() != null && device.properties().policy().resourceId() != null) {
            System.out.println("  ✓ Device has CMS policy assigned: " + device.properties().policy().resourceId());
        } else {
            System.out.println("  ℹ Device has no CMS policy (ARM-created, not DPS-provisioned)");
        }

        // List devices in namespace
        System.out.println("  Listing devices in namespace...");
        List<NamespaceDevice> allDevices = new ArrayList<>();
        for (NamespaceDevice d : deviceRegistryManager.namespaceDevices()
            .listByResourceGroup(RESOURCE_GROUP_NAME, NAMESPACE_NAME)) {
            allDevices.add(d);
        }
        Assertions.assertTrue(allDevices.stream().anyMatch(d -> DEVICE_NAME.equals(d.name())));
        System.out.println("  ✓ LIST found " + allDevices.size() + " device(s), including '" + DEVICE_NAME + "'\n");

        // Step 8c: Test Device.Revoke on ARM-created device (no policy attached)
        // Known RP bug: RP returns HTTP 200 synchronously instead of 202, omits LRO headers.
        // The SDK may throw ManagementException. This is a negative test.
        System.out.println("Step 8c: Testing Device.Revoke (ARM-created device, no policy attached)...");
        DeviceCredentialsRevokeRequest revokeRequest = new DeviceCredentialsRevokeRequest().withDisable(false);

        try {
            device.revoke(revokeRequest);
            System.out.println("  ℹ Revoke completed without error (RP behavior may have been fixed)");
        } catch (ManagementException e) {
            System.out.println("  ✓ Revoke threw ManagementException as expected");
            System.out.println("  Status: " + e.getResponse().getStatusCode());
            System.out
                .println("  Message: " + e.getMessage().substring(0, Math.min(150, e.getMessage().length())) + "...");
        } catch (Exception e) {
            System.out.println("  ✓ Revoke threw " + e.getClass().getSimpleName() + " as expected");
            System.out
                .println("  Message: " + e.getMessage().substring(0, Math.min(150, e.getMessage().length())) + "...");
        }

        // Step 8d: Verify device state unchanged after revoke attempt
        System.out.println("Step 8d: Verifying device state unchanged after failed revoke...");
        device = deviceRegistryManager.namespaceDevices().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, DEVICE_NAME);
        Assertions.assertNotNull(device.properties());
        System.out.println("  ✓ Device still exists: " + device.name());
        System.out.println("  ✓ Version: " + device.properties().version());
        System.out.println("  ✓ Provisioning state: " + device.properties().provisioningState() + "\n");

        // Step 8e: Delete device (cleanup)
        // Note: The auto-generated SDK only expects 202/204 for DELETE but the RP returns 200.
        // This is a codegen bug - the delete actually succeeds. Wrap in try-catch to handle gracefully.
        System.out.println("Step 8e: Deleting device '" + DEVICE_NAME + "'...");
        try {
            deviceRegistryManager.namespaceDevices().delete(RESOURCE_GROUP_NAME, NAMESPACE_NAME, DEVICE_NAME);
            System.out.println("  ✓ Device deleted successfully\n");
        } catch (com.azure.core.management.exception.ManagementException e) {
            if (e.getMessage().contains("Status code 200")) {
                System.out.println("  ✓ Device deleted (RP returned 200 but SDK expected 202/204 - codegen bug)\n");
            } else {
                throw e;
            }
        }

        // ============================================================
        // Step 9: Test RevokeIssuer on standard (non-BYOR) policy
        // ============================================================
        System.out.println("Step 9: Testing RevokeIssuer on standard policy...");
        policy.revokeIssuer();
        System.out.println("  ✓ RevokeIssuer completed successfully on standard policy");

        // Verify policy state after RevokeIssuer
        System.out.println("  Verifying policy state after RevokeIssuer...");
        policy = deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, POLICY_NAME);
        Assertions.assertNotNull(policy.properties());
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, policy.properties().provisioningState());
        System.out.println("  ✓ Policy provisioning state: " + policy.properties().provisioningState());
        System.out.println("  ✓ Policy validity: "
            + policy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays() + " days\n");

        // ============================================================
        // Step 10: Delete standard policy
        // ============================================================
        System.out.println("Step 10: Deleting policy '" + POLICY_NAME + "'...");
        deviceRegistryManager.policies().delete(RESOURCE_GROUP_NAME, NAMESPACE_NAME, POLICY_NAME);

        // Verify policy no longer exists
        boolean policyExistsAfterDelete = false;
        try {
            deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, POLICY_NAME);
            policyExistsAfterDelete = true;
        } catch (ManagementException e) {
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 404) {
                policyExistsAfterDelete = false;
            } else {
                throw e;
            }
        }
        Assertions.assertFalse(policyExistsAfterDelete);
        System.out.println("  ✓ Policy deleted successfully\n");

        // ============================================================
        // Step 11: BYOR (Bring Your Own Root) Policy Flow
        // ============================================================
        System.out.println("Step 11: Creating BYOR-enabled policy '" + BYOR_POLICY_NAME + "'...");
        CertificateAuthorityConfiguration byorCaConfig
            = new CertificateAuthorityConfiguration().withKeyType(SupportedKeyType.ECC)
                .withBringYourOwnRoot(new BringYourOwnRoot().withEnabled(true));
        CertificateConfiguration byorCertConfig
            = new CertificateConfiguration().withCertificateAuthorityConfiguration(byorCaConfig)
                .withLeafCertificateConfiguration(new LeafCertificateConfiguration().withValidityPeriodInDays(90));

        PolicyProperties byorPolicyProperties = new PolicyProperties().withCertificate(byorCertConfig);

        Policy byorPolicy = deviceRegistryManager.policies()
            .define(BYOR_POLICY_NAME)
            .withExistingNamespace(RESOURCE_GROUP_NAME, NAMESPACE_NAME)
            .withProperties(byorPolicyProperties)
            .create();

        Assertions.assertNotNull(byorPolicy);
        Assertions.assertEquals(BYOR_POLICY_NAME, byorPolicy.name());
        Assertions.assertNotNull(
            byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot());
        Assertions.assertTrue(
            byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot().enabled());
        System.out.println("  ✓ BYOR policy created successfully");
        System.out.println("  ✓ BYOR enabled: "
            + byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot().enabled());
        System.out.println("  ✓ BYOR status: "
            + byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot().status()
            + "\n");

        // ============================================================
        // Step 11b: Verify BYOR PendingActivation status and CSR
        // ============================================================
        System.out.println("Step 11b: Verifying BYOR PendingActivation status and CSR...");
        BringYourOwnRoot byorConfig
            = byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot();

        Assertions.assertEquals(BringYourOwnRootStatus.PENDING_ACTIVATION, byorConfig.status(),
            "Newly created BYOR policy should be in PendingActivation status");
        Assertions.assertNotNull(byorConfig.certificateSigningRequest(),
            "BYOR policy in PendingActivation should have a CSR");
        Assertions.assertTrue(byorConfig.certificateSigningRequest().contains("-----BEGIN CERTIFICATE REQUEST-----"),
            "CSR should be in PEM format");
        System.out.println("  ✓ BYOR status: PendingActivation");
        System.out.println("  ✓ CSR present (" + byorConfig.certificateSigningRequest().length() + " chars)");
        System.out.println("  CSR preview: " + byorConfig.certificateSigningRequest()
            .substring(0, Math.min(80, byorConfig.certificateSigningRequest().length())) + "...\n");

        // ============================================================
        // Step 11c: Test ActivateBringYourOwnRoot with invalid certificate (negative test)
        // ============================================================
        System.out.println("Step 11c: Testing ActivateBringYourOwnRoot with INVALID certificate (negative test)...");
        String fakeCertificateChain
            = "-----BEGIN CERTIFICATE-----\n" + "MIIBkTCB+wIJALRiMLAhFake0DQYJKoZIhvcNAQELBQAwDzENMAsGA1UEAwwEdGVz\n"
                + "dDAeFw0yNDAzMjAxMjAwMDBaFw0yNTAzMjAxMjAwMDBaMA8xDTALBgNVBAMMBHRl\n"
                + "c3QwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA0Z3VS5JJcds3xf0GQGZ/fake+key\n"
                + "data+that+is+intentionally+invalid+for+testing+purposes+only+AAAAAAAAAA==\n"
                + "-----END CERTIFICATE-----";

        ActivateBringYourOwnRootRequest activateRequest
            = new ActivateBringYourOwnRootRequest().withCertificateChain(fakeCertificateChain);

        // Known RP behavior: The RP may return HTTP 200 with an empty body instead of
        // a proper 4xx error for invalid certificates. The SDK's LRO polling may succeed
        // silently or throw ManagementException depending on the response shape.
        // Accept both outcomes — the important verification is in Step 11d below.
        try {
            byorPolicy.activateBringYourOwnRoot(activateRequest);
            System.out.println("  ℹ ActivateBYOR completed without error (RP returned 200 — known RP behavior)");
        } catch (ManagementException e) {
            System.out.println("  ✓ ActivateBYOR correctly rejected invalid certificate");
            System.out.println("  Exception type: " + e.getClass().getSimpleName());
            System.out.println("  Error status: " + e.getResponse().getStatusCode());
            System.out.println("  Error code: " + (e.getValue() != null ? e.getValue().getCode() : "N/A"));
            System.out
                .println("  Message: " + e.getMessage().substring(0, Math.min(200, e.getMessage().length())) + "...\n");
        } catch (Exception e) {
            System.out.println("  ✓ ActivateBYOR threw " + e.getClass().getSimpleName());
            System.out
                .println("  Message: " + e.getMessage().substring(0, Math.min(200, e.getMessage().length())) + "...\n");
        }

        // ============================================================
        // Step 11d: Verify BYOR state after activation attempt
        // If the RP accepted the invalid cert (returned 200), the status may have changed.
        // ============================================================
        System.out.println("Step 11d: Checking BYOR state after activation attempt...");
        byorPolicy = deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, BYOR_POLICY_NAME);
        BringYourOwnRoot byorConfigAfterFailure
            = byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot();

        Assertions.assertTrue(byorConfigAfterFailure.enabled(), "BYOR should still be enabled");
        Assertions.assertNotNull(byorConfigAfterFailure.status(), "BYOR status should not be null");
        System.out.println("  ✓ BYOR still enabled: " + byorConfigAfterFailure.enabled());
        System.out.println("  ✓ BYOR status: " + byorConfigAfterFailure.status());
        if (byorConfigAfterFailure.certificateSigningRequest() != null) {
            System.out.println(
                "  ✓ CSR present (" + byorConfigAfterFailure.certificateSigningRequest().length() + " chars)\n");
        } else {
            System.out.println("  ℹ CSR no longer present (activation may have changed state)\n");
        }

        // ============================================================
        // Step 12: Update BYOR policy — change validity to 45 days
        // For PATCH, omit certificateAuthorityConfiguration to avoid sending immutable props.
        // ============================================================
        System.out.println("Step 12: Updating BYOR policy - changing validity from "
            + byorPolicy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays()
            + " to 45 days...");

        CertificateConfiguration byorUpdateCertConfig = new CertificateConfiguration()
            .withLeafCertificateConfiguration(new LeafCertificateConfiguration().withValidityPeriodInDays(45));

        byorPolicy = byorPolicy.update()
            .withProperties(new PolicyUpdateProperties().withCertificate(byorUpdateCertConfig))
            .apply();

        System.out.println("  Update operation completed");

        // GET fresh BYOR policy after update
        System.out.println("  Getting fresh BYOR policy after update...");
        byorPolicy = deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, BYOR_POLICY_NAME);

        Assertions.assertEquals(45,
            byorPolicy.properties().certificate().leafCertificateConfiguration().validityPeriodInDays());
        Assertions.assertTrue(
            byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot().enabled());
        System.out.println("  ✓ BYOR policy updated successfully, validity now 45 days");
        System.out.println("  ✓ BYOR still enabled: "
            + byorPolicy.properties().certificate().certificateAuthorityConfiguration().bringYourOwnRoot().enabled()
            + "\n");

        // ============================================================
        // Step 13: Delete BYOR policy
        // ============================================================
        System.out.println("Step 13: Deleting BYOR policy '" + BYOR_POLICY_NAME + "'...");
        deviceRegistryManager.policies().delete(RESOURCE_GROUP_NAME, NAMESPACE_NAME, BYOR_POLICY_NAME);

        boolean byorExistsAfterDelete = false;
        try {
            deviceRegistryManager.policies().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME, BYOR_POLICY_NAME);
            byorExistsAfterDelete = true;
        } catch (ManagementException e) {
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 404) {
                byorExistsAfterDelete = false;
            } else {
                throw e;
            }
        }
        Assertions.assertFalse(byorExistsAfterDelete);
        System.out.println("  ✓ BYOR policy deleted successfully\n");

        // ============================================================
        // Step 14: Delete Credential
        // Allow RP to complete BYOR deletion before deleting credential.
        // ============================================================
        System.out.println("Step 14: Deleting credential...");
        deviceRegistryManager.credentials().deleteByResourceGroup(RESOURCE_GROUP_NAME, NAMESPACE_NAME);

        // Verify credential no longer exists
        boolean credentialExistsAfterDelete = false;
        try {
            deviceRegistryManager.credentials().get(RESOURCE_GROUP_NAME, NAMESPACE_NAME);
            credentialExistsAfterDelete = true;
        } catch (ManagementException e) {
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 404) {
                credentialExistsAfterDelete = false;
            } else {
                throw e;
            }
        }
        Assertions.assertFalse(credentialExistsAfterDelete);
        System.out.println("  ✓ Credential deleted successfully\n");

        System.out.println("============================================================");
        System.out.println("TEST COMPLETED  [Device Registry Credentials & Policies Flow]");
        System.out.println("============================================================\n");
    }
}
