// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorResultInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.time.OffsetDateTime;
import java.util.Map;
import reactor.core.publisher.Mono;

/** Client-side representation of Connection Monitor object, associated with Network Watcher. */
@Fluent
public interface ConnectionMonitor extends HasInnerModel<ConnectionMonitorResultInner>, HasName, HasId, Indexable {
    /**
     * Gets connection monitor location.
     *
     * @return connection monitor location
     */
    String location();

    /**
     * Gets connection monitor tags.
     *
     * @return connection monitor tags
     */
    Map<String, String> tags();

    /**
     * Gets the source property.
     *
     * @return the source property
     */
    ConnectionMonitorSource source();

    /**
     * Gets the destination property.
     *
     * @return the destination property
     */
    ConnectionMonitorDestination destination();

    /**
     * Determines if the connection monitor will start automatically once created.
     *
     * @return true if the connection monitor will start automatically once created, false otherwise
     */
    boolean autoStart();

    /**
     * Gets the provisioning state of the connection monitor.
     *
     * @return the provisioning state of the connection monitor
     */
    ProvisioningState provisioningState();

    /**
     * Gets the date and time when the connection monitor was started.
     *
     * @return the date and time when the connection monitor was started
     */
    OffsetDateTime startTime();

    /**
     * Gets the monitoring status of the connection monitor.
     *
     * @return the monitoring status of the connection monitor
     */
    String monitoringStatus();

    /**
     * Gets monitoring interval in seconds.
     *
     * @return monitoring interval in seconds
     */
    int monitoringIntervalInSeconds();

    /** Stops a specified connection monitor. */
    void stop();

    /**
     * Stops a specified connection monitor asynchronously.
     *
     * @return the handle to the REST call
     */
    Mono<Void> stopAsync();

    /** The entirety of the connection monitor definition. */
    interface Definition extends DefinitionStages.WithSource, DefinitionStages.WithDestination,
        DefinitionStages.WithDestinationPort, DefinitionStages.WithCreate {
    }

    /** Grouping of connection monitor definition stages. */
    interface DefinitionStages {

        /** Sets the source property. */
        interface WithSource {
            /**
             * Specifies the ID of the resource used as the source by connection monitor.
             *
             * @param resourceId the ID of the resource used as the source by connection monitor
             * @return next definition stage
             */
            WithDestination withSourceId(String resourceId);

            /**
             * Specifies virtual machine used as the source by connection monitor.
             *
             * @param vm virtual machine used as the source by connection monitor
             * @return next definition stage
             */
            WithDestination withSource(HasNetworkInterfaces vm);
        }

        /** Sets the source port used by connection monitor. */
        interface WithSourcePort {
            /**
             * Specifies source port used by connection monitor.
             *
             * @param port source port used by connection monitor
             * @return next definition stage
             */
            WithDestination withSourcePort(int port);
        }

        /** Sets the destination. */
        interface WithDestination {
            /**
             * Specifies the ID of the resource used as the source by connection monitor.
             *
             * @param resourceId the ID of the resource used as the source by connection monitor
             * @return next definition stage
             */
            WithDestinationPort withDestinationId(String resourceId);

            /**
             * Specifies virtual machine used as the source by connection monitor.
             *
             * @param vm virtual machine used as the source by connection monitor
             * @return next definition stage
             */
            WithDestinationPort withDestination(HasNetworkInterfaces vm);

            /**
             * Specifies address of the connection monitor destination (IP or domain name).
             *
             * @param address address of the connection monitor destination (IP or domain name)
             * @return next definition stage
             */
            WithDestinationPort withDestinationAddress(String address);
        }

        /** Sets the destination port used by connection monitor. */
        interface WithDestinationPort {
            /**
             * Specifies the ID of the resource used as the source by connection monitor.
             *
             * @param port the ID of the resource used as the source by connection monitor
             * @return next definition stage
             */
            WithCreate withDestinationPort(int port);
        }

        /**
         * Determines if the connection monitor will start automatically once created. By default it is started
         * automatically.
         */
        interface WithAutoStart {
            /**
             * Disable auto start.
             *
             * @return next definition stage
             */
            WithCreate withoutAutoStart();
        }

        /** Sets monitoring interval in seconds. */
        interface WithMonitoringInterval {
            /**
             * Specifies monitoring interval in seconds.
             *
             * @param seconds monitoring interval in seconds
             * @return next definition stage
             */
            WithCreate withMonitoringInterval(int seconds);
        }

        /** The stage of the connection monitor definition allowing to add or update tags. */
        interface WithTags {
            /**
             * Specifies tags for the connection monitor.
             *
             * @param tags tags indexed by name
             * @return the next stage of the definition
             */
            WithCreate withTags(Map<String, String> tags);

            /**
             * Adds a tag to the connection monitor.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the definition
             */
            WithCreate withTag(String key, String value);

            /**
             * Removes a tag from the connection monitor.
             *
             * @param key the key of the tag to remove
             * @return the next stage of the definition
             */
            WithCreate withoutTag(String key);
        }

        /**
         * The stage of the connection monitor definition allowing creating and updating.
         */
        interface WithCreate
            extends Creatable<ConnectionMonitor>, WithSourcePort, WithAutoStart, WithMonitoringInterval, WithTags {
        }
    }
}
