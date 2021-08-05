// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.PacketCaptureResultInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.util.List;
import reactor.core.publisher.Mono;

/** Client-side representation of Packet capture object, associated with Network Watcher. */
@Fluent
public interface PacketCapture extends HasInnerModel<PacketCaptureResultInner>, HasName, HasId, Indexable {
    /** @return the target id value */
    String targetId();

    /** @return the number of bytes captured per packet, the remaining bytes are truncated. */
    long bytesToCapturePerPacket();

    /** @return the maximum size of the capture output */
    long totalBytesPerSession();

    /** @return the maximum duration of the capture session in seconds */
    int timeLimitInSeconds();

    /** @return the storageLocation value */
    PacketCaptureStorageLocation storageLocation();

    /** @return the filters value */
    List<PacketCaptureFilter> filters();

    /**
     * Get the provisioning state of the packet capture session.
     *
     * @return the provisioningState value
     */
    ProvisioningState provisioningState();

    /** Stops a specified packet capture session. */
    void stop();

    /**
     * Stops a specified packet capture session asynchronously.
     *
     * @return the handle to the REST call
     */
    Mono<Void> stopAsync();

    /**
     * Query the status of a running packet capture session.
     *
     * @return packet capture status
     */
    PacketCaptureStatus getStatus();

    /**
     * Query the status of a running packet capture session asynchronously.
     *
     * @return packet capture status
     */
    Mono<PacketCaptureStatus> getStatusAsync();

    /** The entirety of the packet capture definition. */
    interface Definition
        extends PacketCapture.DefinitionStages.WithTarget,
            PacketCapture.DefinitionStages.WithStorageLocation,
            PacketCapture.DefinitionStages.WithCreateAndStoragePath {
    }

    /** Grouping of Packet Capture definition stages. */
    interface DefinitionStages {

        interface WithTarget {
            /**
             * Set target resource ID, only VM is currently supported.
             *
             * @param target The ID of the targeted resource
             * @return the next stage
             */
            WithStorageLocation withTarget(String target);
        }

        interface WithStorageLocation {
            /**
             * The ID of the storage account to save the packet capture session. Required if no local file path is
             * provided.
             *
             * @param storageId The ID of the storage account to save the packet capture session
             * @return the next stage of the definition
             */
            WithCreateAndStoragePath withStorageAccountId(String storageId);

            /**
             * A valid local path on the targeting VM. Must include the name of the capture file (*.cap). For linux
             * virtual machine it must start with /var/captures. Required if no storage ID is provided, otherwise
             * optional.
             *
             * @param filePath A valid local path on the targeting VM
             * @return the next stage
             */
            WithCreate withFilePath(String filePath);
        }

        interface WithCreate extends Creatable<PacketCapture> {
            /**
             * Set number of bytes captured per packet, the remaining bytes are truncated.
             *
             * @param bytesToCapturePerPacket Number of bytes captured per packet
             * @return the next stage
             */
            WithCreate withBytesToCapturePerPacket(long bytesToCapturePerPacket);

            /**
             * Set maximum size of the capture output.
             *
             * @param totalBytesPerSession Maximum size of the capture output
             * @return the next stage
             */
            WithCreate withTotalBytesPerSession(long totalBytesPerSession);

            /**
             * Set maximum duration of the capture session in seconds.
             *
             * @param timeLimitInSeconds Maximum duration of the capture session in seconds
             * @return the next stage
             */
            WithCreate withTimeLimitInSeconds(int timeLimitInSeconds);

            /**
             * Begin the definition of packet capture filter.
             *
             * @return the next stage
             */
            PCFilter.DefinitionStages.Blank<WithCreate> definePacketCaptureFilter();
        }

        interface WithCreateAndStoragePath extends WithCreate {
            /**
             * The URI of the storage path to save the packet capture. Must be a well-formed URI describing the location
             * to save the packet capture.
             *
             * @param storagePath The URI of the storage path to save the packet capture. Must be a well-formed URI
             *     describing the location to save the packet capture.
             * @return the next stage
             */
            WithCreate withStoragePath(String storagePath);
        }
    }
}
