/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.PacketCaptureResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.Map;

/**
 * Client-side representation of Packet capture object, associated with Network Watcher.
 */
@Fluent
@Beta
public interface PacketCapture extends
        HasInner<PacketCaptureResultInner>,
        IndependentChildResource<NetworkManager, PacketCaptureResultInner> {
    /**
     * Get packet capture filters.
     *
     * @return the PacketCaptureFilters
     */
    Map<String, PacketCaptureFilter> filters();

    /**
     * Stops a specified packet capture session.
     */
    void stop();

    /**
     * Query the status of a running packet capture session.
     *
     * @return packet capture status
     */
    PacketCaptureStatus getStatus();


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
             */
            WithCreateAndStoragePath withStorageAccountId(String storageId);

            /**
             * Specify storage account to save the packet capture session.
             * Required if no local file path is provided.
             *
             * @param storageAccount The storage account to save the packet capture session
             */
            WithCreateAndStoragePath withStorageAccount(StorageAccount storageAccount);

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
             * the description
             * @param param the param
             * @return the next stage
             */
//            PCFilter.DefinitionStages.Blank<Definition> definePacketCaptureFilter(String param);
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

    /**
     * The template for a packet capture update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<PacketCapture>,
            PacketCapture.UpdateStages.WithTarget /*required*/,
            PacketCapture.UpdateStages.WithBytesToCapturePerPacket,
            PacketCapture.UpdateStages.WithTotalBytesPerSession,
            PacketCapture.UpdateStages.WithTimeLimitInSeconds,
            PacketCapture.UpdateStages.WithStorageLocation /*required*/,
            PacketCapture.UpdateStages.DefineFilters {
    }

    /**
     * Grouping of packet capture update stages.
     */
    interface UpdateStages {

        interface WithTarget {
            /**
             * Set the ID of the targeted resource, only VM is currently supported.
             *
             * @param targetId The ID of the targeted resource.
             * @return the next stage
             */
            Update withTarget(String targetId);
        }

        interface WithBytesToCapturePerPacket {
            /**
             * Set the number of bytes captured per packet, the remaining bytes are truncated.
             *
             * @param bytesToCapturePerPacket Number of bytes
             * @return the next stage
             */
            Update withBytesToCapturePerPacket(int bytesToCapturePerPacket);
        }

        interface WithTotalBytesPerSession {
            /**
             * Set maximum size of the capture output.
             *
             * @param totalBytesPerSession Maximum size of the capture output
             * @return the next stage
             */
            Update withTotalBytesPerSession(int totalBytesPerSession);
        }

        interface WithTimeLimitInSeconds {
            /**
             * Set maximum duration of the capture session in seconds.
             *
             * @param timeLimitInSeconds Maximum duration of the capture session in seconds.
             * @return the next stage
             */
            Update withTimeLimitInSeconds(int timeLimitInSeconds);
        }

        interface WithStorageLocation {
            /**
             * The ID of the storage account to save the packet capture session.
             * Required if no local file path is provided.
             *
             * @param storageId The ID of the storage account to save the packet capture session
             */
            Update withStorageAccountId(String storageId);

            /**
             * Specify storage account to save the packet capture session.
             * Required if no local file path is provided.
             *
             * @param storageAccount The storage account to save the packet capture session
             */
            Update withStorageAccount(StorageAccount storageAccount);

            /**
             * A valid local path on the targeting VM. Must include the name of the
             * capture file (*.cap). For linux virtual machine it must start with
             * /var/captures. Required if no storage ID is provided, otherwise
             * optional.
             *
             * @param filePath A valid local path on the targeting VM
             * @return the next stage
             */
            Update withFilePath(String filePath);
        }

        interface DefineFilters {
            /**
             * the description
             * @param param the param
             * @return the next stage
             */
//            PCFilter.Update<Update> updatePacketCaptureFilter(String param);
        }
    }

}
