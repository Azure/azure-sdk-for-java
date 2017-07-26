/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.PacketCaptureResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import rx.Completable;
import rx.Observable;

import java.util.List;

/**
 * Client-side representation of Packet capture object, associated with Network Watcher.
 */
@Fluent
@Beta
public interface PacketCapture extends
        HasInner<PacketCaptureResultInner>,
        HasName,
        HasId,
        Indexable {
    /**
     * @return the target id value
     */
    String targetId();

    /**
     * @return the number of bytes captured per packet, the remaining bytes are truncated.
     */
    int bytesToCapturePerPacket();

    /**
     * @return the maximum size of the capture output
     */
    int totalBytesPerSession();

    /**
     * @return the maximum duration of the capture session in seconds
     */
    int timeLimitInSeconds();

    /**
     * @return the storageLocation value
     */
    PacketCaptureStorageLocation storageLocation();

    /**
     * @return the filters value
     */
    List<PacketCaptureFilter> filters();

    /**
     * Get the provisioning state of the packet capture session. Possible values
     * include: 'Succeeded', 'Updating', 'Deleting', 'Failed'.
     *
     * @return the provisioningState value
     */
    ProvisioningState provisioningState();

    /**
     * Stops a specified packet capture session.
     */
    @Method
    void stop();

    /**
     * Stops a specified packet capture session asynchronously.
     * @return the handle to the REST call
     */
    @Method
    Completable stopAsync();

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
    Observable<PacketCaptureStatus> getStatusAsync();

    /**
     * The entirety of the packet capture definition.
     */
    interface Definition extends
            PacketCapture.DefinitionStages.WithTarget,
            PacketCapture.DefinitionStages.WithStorageLocation,
            PacketCapture.DefinitionStages.WithCreateAndStoragePath {
    }

    /**
     * Grouping of Packet Capture definition stages.
     */
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
             * The ID of the storage account to save the packet capture session.
             * Required if no local file path is provided.
             *
             * @param storageId The ID of the storage account to save the packet capture session
             * @return the next stage of the definition
             */
            WithCreateAndStoragePath withStorageAccountId(String storageId);

            /**
             * A valid local path on the targeting VM. Must include the name of the
             * capture file (*.cap). For linux virtual machine it must start with
             * /var/captures. Required if no storage ID is provided, otherwise
             * optional.
             *
             * @param filePath A valid local path on the targeting VM
             * @return the next stage
             */
            WithCreate withFilePath(String filePath);
        }

        interface WithCreate extends
                Creatable<PacketCapture> {
            /**
             * Set number of bytes captured per packet, the remaining bytes are truncated.
             *
             * @param bytesToCapturePerPacket Number of bytes captured per packet
             * @return the next stage
             */
            WithCreate withBytesToCapturePerPacket(int bytesToCapturePerPacket);

            /**
             * Set maximum size of the capture output.
             *
             * @param totalBytesPerSession Maximum size of the capture output
             * @return the next stage
             */
            WithCreate withTotalBytesPerSession(int totalBytesPerSession);

            /**
             * Set maximum duration of the capture session in seconds.
             *
             * @param timeLimitInSeconds Maximum duration of the capture session in seconds
             * @return the next stage
             */
            WithCreate withTimeLimitInSeconds(int timeLimitInSeconds);

            /**
             * Begin the definition of packet capture filter.
             * @return the next stage
             */
            PCFilter.DefinitionStages.Blank<WithCreate> definePacketCaptureFilter();
        }

        interface WithCreateAndStoragePath extends WithCreate {
            /**
             * The URI of the storage path to save the packet capture. Must be a
             * well-formed URI describing the location to save the packet capture.
             *
             * @param storagePath The URI of the storage path to save the packet capture. Must be a well-formed URI describing the location to save the packet capture.
             * @return the next stage
             */
            WithCreate withStoragePath(String storagePath);
        }
    }
}
