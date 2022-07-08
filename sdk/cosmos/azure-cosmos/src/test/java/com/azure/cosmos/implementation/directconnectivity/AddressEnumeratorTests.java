// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.addressEnumerator.AddressEnumerator;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Connected;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unhealthy;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.UnhealthyPending;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unknown;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class AddressEnumeratorTests {
    private final AddressEnumerator addressEnumerator;

    public AddressEnumeratorTests() {
        this.addressEnumerator = new AddressEnumerator();
    }

    @Test(groups = "unit")
    public void replicaAddressValidationEnabledComparatorTests() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Method sortAddressesMethod = AddressEnumerator.class.getDeclaredMethod("sortAddresses", List.class, RxDocumentServiceRequest.class);
        sortAddressesMethod.setAccessible(true);

        // set a different health status to each endpoint to test the sorting logic
        Uri testUri1 = new Uri("https://127.0.0.1:1");
        assertThat(testUri1.getHealthStatus()).isEqualTo(Unknown);

        Uri testUri2 = new Uri("https://127.0.0.1:2");
        testUri2.setConnected();
        assertThat(testUri2.getHealthStatus()).isEqualTo(Connected);

        Uri testUri3 = new Uri("https://127.0.0.1:3");
        testUri3.setUnhealthy();
        testUri3.setRefreshed();
        assertThat(testUri3.getHealthStatus()).isEqualTo(UnhealthyPending);

        Uri testUri4 = new Uri("https://127.0.0.1:4");
        testUri4.setUnhealthy();
        assertThat(testUri4.getHealthStatus()).isEqualTo(Unhealthy);

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        requestMock.requestContext = new DocumentServiceRequestContext();
        requestMock.requestContext.replicaAddressValidationEnabled = true;

        // when replicaAddressValidation is enabled, we prefer Connected/Unknown > UnhealthyPending > Unhealthy
        List<SortAddressesTestScenario> testScenarios = Arrays.asList(
                new SortAddressesTestScenario(Arrays.asList(testUri1, testUri2), Arrays.asList(testUri1, testUri2)), // unknown, connected -> unknown, connected
                new SortAddressesTestScenario(Arrays.asList(testUri2, testUri1), Arrays.asList(testUri2, testUri1)), // connected, unknown -> connected, unknown
                new SortAddressesTestScenario(Arrays.asList(testUri3, testUri2), Arrays.asList(testUri2, testUri3)), // unhealthyPending, connected -> connected, unhealthyPending
                new SortAddressesTestScenario(Arrays.asList(testUri4, testUri1), Arrays.asList(testUri1, testUri4)), // unhealthy, unknown -> unknown, unhealthy
                new SortAddressesTestScenario(Arrays.asList(testUri4, testUri3), Arrays.asList(testUri3, testUri4))); // unhealthy, unhealthyPending -> unhealthyPending, unhealthy

        for (SortAddressesTestScenario testScenario : testScenarios) {
            System.out.println("Test scenario, comparing " + testScenario.getAddresses());
            for (Uri uri : testScenario.getAddresses()) {
                this.setTimestamp(uri, Instant.now());
            }
            List<Uri> sortedAddresses =
                    (List<Uri>) sortAddressesMethod.invoke(this.addressEnumerator, testScenario.getAddresses(), requestMock);
            assertThat(sortedAddresses).containsExactlyElementsOf(testScenario.expectedAddresses);
        }

        System.out.println("Test scenario: comparing when unhealthyPending roll into healthy status after 1 min");
        setTimestamp(testUri3, Instant.now().minusMillis(Duration.ofMinutes(2).toMillis()));
        List<Uri> sortedAddresses = (List<Uri>) sortAddressesMethod.invoke(this.addressEnumerator, Arrays.asList(testUri3, testUri2), requestMock);
        assertThat(sortedAddresses).containsExactlyElementsOf(Arrays.asList(testUri3, testUri2));

        System.out.println("Test scenario: comparing when there is failedEndpoints marked in request context");
        requestMock.requestContext.addToFailedEndpoints(new GoneException("Test"), testUri2);
        sortedAddresses = (List<Uri>) sortAddressesMethod.invoke(this.addressEnumerator, Arrays.asList(testUri4, testUri2), requestMock);
        assertThat(sortedAddresses).containsExactlyElementsOf(Arrays.asList(testUri4, testUri2));
    }

    @Test(groups = "unit")
    public void replicaAddressValidationDisabledComparatorTests() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Method sortAddressesMethod = AddressEnumerator.class.getDeclaredMethod("sortAddresses", List.class, RxDocumentServiceRequest.class);
        sortAddressesMethod.setAccessible(true);

        // set a different health status to each endpoint to test the sorting logic
        Uri testUri1 = new Uri("https://127.0.0.1:1");
        assertThat(testUri1.getHealthStatus()).isEqualTo(Unknown);

        Uri testUri2 = new Uri("https://127.0.0.1:2");
        testUri2.setConnected();
        assertThat(testUri2.getHealthStatus()).isEqualTo(Connected);

        Uri testUri3 = new Uri("https://127.0.0.1:3");
        testUri3.setUnhealthy();
        testUri3.setRefreshed();
        assertThat(testUri3.getHealthStatus()).isEqualTo(UnhealthyPending);

        Uri testUri4 = new Uri("https://127.0.0.1:4");
        testUri4.setUnhealthy();
        assertThat(testUri4.getHealthStatus()).isEqualTo(Unhealthy);

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        requestMock.requestContext = new DocumentServiceRequestContext();

        // when replicaAddressValidation is enabled, we prefer Connected/Unknown/UnhealthyPending > Unhealthy
        List<SortAddressesTestScenario> testScenarios = Arrays.asList(
                new SortAddressesTestScenario(Arrays.asList(testUri1, testUri2), Arrays.asList(testUri1, testUri2)), // unknown, connected -> unknown, connected
                new SortAddressesTestScenario(Arrays.asList(testUri2, testUri1), Arrays.asList(testUri2, testUri1)), // connected, unknown -> connected, unknown
                new SortAddressesTestScenario(Arrays.asList(testUri3, testUri2), Arrays.asList(testUri3, testUri2)), // unhealthyPending, connected -> unhealthyPending, connected
                new SortAddressesTestScenario(Arrays.asList(testUri4, testUri1), Arrays.asList(testUri1, testUri4)), // unhealthy, unknown -> unknown, unhealthy
                new SortAddressesTestScenario(Arrays.asList(testUri4, testUri3), Arrays.asList(testUri3, testUri4))); // unhealthy, unhealthyPending -> unhealthyPending, unhealthy

        for (SortAddressesTestScenario testScenario : testScenarios) {
            System.out.println("Test scenario, comparing " + testScenario.getAddresses());
            for (Uri uri : testScenario.getAddresses()) {
                this.setTimestamp(uri, Instant.now());
            }
            List<Uri> sortedAddresses =
                    (List<Uri>) sortAddressesMethod.invoke(this.addressEnumerator, testScenario.getAddresses(), requestMock);
            assertThat(sortedAddresses).containsExactlyElementsOf(testScenario.expectedAddresses);
        }

        System.out.println("Test scenario: comparing when there is failedEndpoints marked in request context");
        requestMock.requestContext.addToFailedEndpoints(new GoneException("Test"), testUri2);
        List<Uri> sortedAddresses = (List<Uri>) sortAddressesMethod.invoke(this.addressEnumerator, Arrays.asList(testUri4, testUri2), requestMock);
        assertThat(sortedAddresses).containsExactlyElementsOf(Arrays.asList(testUri4, testUri2));
    }

    private void setTimestamp(Uri testUri, Instant time) throws NoSuchFieldException, IllegalAccessException {
        switch (testUri.getHealthStatus()) {
            case Unknown:
                Field lastUnknownTimestampField = Uri.class.getDeclaredField("lastUnknownTimestamp");
                lastUnknownTimestampField.setAccessible(true);
                lastUnknownTimestampField.set(testUri, time);
                break;
            case Unhealthy:
                Field lastUnhealthyTimestampField = Uri.class.getDeclaredField("lastUnhealthyTimestamp");
                lastUnhealthyTimestampField.setAccessible(true);
                lastUnhealthyTimestampField.set(testUri, time);
                break;
            case UnhealthyPending:
                Field lastUnhealthyPendingTimestampField = Uri.class.getDeclaredField("lastUnhealthyPendingTimestamp");
                lastUnhealthyPendingTimestampField.setAccessible(true);
                lastUnhealthyPendingTimestampField.set(testUri, time);
                break;
            case Connected:
                break;
            default:
                throw new IllegalStateException("Unknown status " + testUri.getHealthStatus());
        }
    }

    private static class SortAddressesTestScenario {
        private final List<Uri> addresses;
        private final List<Uri> expectedAddresses;

        public SortAddressesTestScenario(List<Uri> addresses, List<Uri> expectedAddresses) {
            this.addresses = addresses;
            this.expectedAddresses = expectedAddresses;
        }

        public List<Uri> getAddresses() {
            return addresses;
        }

        public List<Uri> getExpectedAddresses() {
            return expectedAddresses;
        }
    }
}