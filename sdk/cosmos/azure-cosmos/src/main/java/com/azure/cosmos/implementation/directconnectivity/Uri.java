// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.directconnectivity.addressEnumerator.AddressEnumerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Uri {
    private static final Logger logger = LoggerFactory.getLogger(Uri.class);

    private static long DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS = 60 * 1000;
    private final String uriAsString;
    private final URI uri;
    private final AtomicReference<HealthStatus> healthStatus;
    private volatile Instant lastUnknownTimestamp;
    private volatile Instant lastUnhealthyPendingTimestamp;
    private volatile Instant lastUnhealthyTimestamp;

    public static Uri create(String uriAsString) {
        return new Uri(uriAsString);
    }

    public Uri(String uri) {
        this.uriAsString = uri;

        URI uriValue = null;
        try {
            uriValue = URI.create(uri);
        } catch (IllegalArgumentException e) {
            uriValue = null;
        }
        this.uri = uriValue;
        this.healthStatus = new AtomicReference<>(HealthStatus.Unknown);
        this.lastUnknownTimestamp = Instant.now();
        this.lastUnhealthyTimestamp = null;
        this.lastUnhealthyTimestamp = null;
    }

    public URI getURI() {
        return this.uri;
    }

    public String getURIAsString() {
        return this.uriAsString;
    }

    /***
     * This method will be called if a connection can be established successfully to the backend.
     */
    public void setConnected() {
        this.setHealthStatus(HealthStatus.Connected);
    }

    /***
     * This method will be called if a request failed with 410 and will cause forceRefresh behavior.
     */
    public void setUnhealthy() {
        this.setHealthStatus(HealthStatus.Unhealthy);
    }

    /***
     * This method will be called if the same address being returned from gateway.
     *
     * Unknown will remain Unknown.
     * Connected will remain Connected.
     * UnhealthyPending will remain UnhealthyPending.
     * Unhealthy will change into UnhealthyPending.
     */
    public void setRefreshed() {
        if (this.healthStatus.get() == HealthStatus.Unhealthy) {
            this.setHealthStatus(HealthStatus.UnhealthyPending);
        }
    }

    /***
     * Please reference the /docs/replicaValidation/ReplicaClientSideStatus.png for valid status transition.
     *
     * @param status the health status.
     */
    public void setHealthStatus(HealthStatus status) {
        this.healthStatus.updateAndGet(previousStatus -> {

            HealthStatus newStatus = previousStatus;
            switch (status) {
                case Unhealthy:
                    this.lastUnhealthyTimestamp = Instant.now();
                    newStatus = status;
                    break;

                case UnhealthyPending:
                    if (previousStatus == HealthStatus.Unhealthy || previousStatus == HealthStatus.UnhealthyPending) {
                        this.lastUnhealthyPendingTimestamp = Instant.now();
                        newStatus = status;
                    }
                    break;
                case Connected:
                    if (previousStatus != HealthStatus.Unhealthy
                        || (previousStatus == HealthStatus.Unhealthy &&
                            Instant.now().compareTo(this.lastUnhealthyTimestamp.plusMillis(DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS)) > 0)) {
                        newStatus = status;
                    }
                    break;
                case Unknown:
                    // there is no reason we are going to reach here
                    throw new IllegalStateException("It is impossible to set to unknown status");
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Called setHealthStatus with status [{}]. Result: previousStatus [{}], newStatus [{}]",
                        status, previousStatus, newStatus);
            }

            return newStatus;
        });
    }

    public HealthStatus getHealthStatus() {
        return this.healthStatus.get();
    }

    /***
     * In {@link AddressEnumerator}, it could de-prioritize uri in unhealthyPending/unhealthy health status (depending on whether replica validation is enabled)
     * If the replica stuck in those statuses for too long, in order to avoid replica usage skew,
     * we are going to rolling them into healthy category, so it is status can be validated by requests again
     *
     * @return
     */
    public HealthStatus getEffectiveHealthStatus() {
        HealthStatus snapshot = this.healthStatus.get();
        switch (snapshot) {
            case Connected:
            case Unhealthy:
                return snapshot;
            case Unknown:
                if (Instant.now()
                        .compareTo(this.lastUnknownTimestamp.plusMillis(DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS)) > 0) {
                    return HealthStatus.Connected;
                }
                return snapshot;
            case UnhealthyPending:
                if (Instant.now()
                        .compareTo(this.lastUnhealthyPendingTimestamp.plusMillis(DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS)) > 0) {
                    return HealthStatus.Connected;
                }
                return snapshot;
            default:
                throw new IllegalStateException("Unknown status " + snapshot);
        }
    }

    public boolean shouldRefreshHealthStatus() {
        return this.healthStatus.get() == HealthStatus.Unhealthy
                && Instant.now().compareTo(this.lastUnhealthyTimestamp.plusMillis(DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS)) > 0;
    }

    public String getHealthStatusDiagnosticString() {
        return this.uri.getPort() + ":" + this.healthStatus.get().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uri uri1 = (Uri) o;
        return uriAsString.equals(uri1.uriAsString) && uri.equals(uri1.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriAsString, uri);
    }

    @Override
    public String toString() {
        return this.uriAsString;
    }


    /***
     * <p>
     * NOTE: Please DO NOT change the priority of the enums,
     * as it is used in {@link AddressEnumerator} for correct sorting purpose
     *
     * if you are going to change the priority of this, please reexamine the sorting logic as well.
     * </p>
     */
    public enum HealthStatus {
        Connected(0),
        Unknown(1),
        UnhealthyPending(2),
        Unhealthy(3);

        private int priority;
        HealthStatus(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return this.priority;
        }
    }
}
