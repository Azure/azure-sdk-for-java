/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.netapp.v2020_09_01;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.VolumeInner;
import com.microsoft.azure.arm.model.Indexable;
import com.microsoft.azure.arm.model.Refreshable;
import com.microsoft.azure.arm.model.Updatable;
import com.microsoft.azure.arm.model.Appliable;
import com.microsoft.azure.arm.model.Creatable;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.NetAppManager;
import java.util.List;
import java.util.Map;

/**
 * Type representing Volume.
 */
public interface Volume extends HasInner<VolumeInner>, Indexable, Refreshable<Volume>, Updatable<Volume.Update>, HasManager<NetAppManager> {
    /**
     * @return the backupId value.
     */
    String backupId();

    /**
     * @return the baremetalTenantId value.
     */
    String baremetalTenantId();

    /**
     * @return the creationToken value.
     */
    String creationToken();

    /**
     * @return the dataProtection value.
     */
    VolumePropertiesDataProtection dataProtection();

    /**
     * @return the exportPolicy value.
     */
    VolumePropertiesExportPolicy exportPolicy();

    /**
     * @return the fileSystemId value.
     */
    String fileSystemId();

    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the isRestoring value.
     */
    Boolean isRestoring();

    /**
     * @return the kerberosEnabled value.
     */
    Boolean kerberosEnabled();

    /**
     * @return the location value.
     */
    String location();

    /**
     * @return the mountTargets value.
     */
    List<MountTargetProperties> mountTargets();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the protocolTypes value.
     */
    List<String> protocolTypes();

    /**
     * @return the provisioningState value.
     */
    String provisioningState();

    /**
     * @return the securityStyle value.
     */
    SecurityStyle securityStyle();

    /**
     * @return the serviceLevel value.
     */
    ServiceLevel serviceLevel();

    /**
     * @return the smbContinuouslyAvailable value.
     */
    Boolean smbContinuouslyAvailable();

    /**
     * @return the smbEncryption value.
     */
    Boolean smbEncryption();

    /**
     * @return the snapshotDirectoryVisible value.
     */
    Boolean snapshotDirectoryVisible();

    /**
     * @return the snapshotId value.
     */
    String snapshotId();

    /**
     * @return the subnetId value.
     */
    String subnetId();

    /**
     * @return the tags value.
     */
    Map<String, String> tags();

    /**
     * @return the throughputMibps value.
     */
    Double throughputMibps();

    /**
     * @return the type value.
     */
    String type();

    /**
     * @return the usageThreshold value.
     */
    long usageThreshold();

    /**
     * @return the volumeType value.
     */
    String volumeType();

    /**
     * The entirety of the Volume definition.
     */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithCapacityPool, DefinitionStages.WithCreationToken, DefinitionStages.WithLocation, DefinitionStages.WithSubnetId, DefinitionStages.WithUsageThreshold, DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Volume definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Volume definition.
         */
        interface Blank extends WithCapacityPool {
        }

        /**
         * The stage of the volume definition allowing to specify CapacityPool.
         */
        interface WithCapacityPool {
           /**
            * Specifies resourceGroupName, accountName, poolName.
            * @param resourceGroupName The name of the resource group
            * @param accountName The name of the NetApp account
            * @param poolName The name of the capacity pool
            * @return the next definition stage
            */
            WithCreationToken withExistingCapacityPool(String resourceGroupName, String accountName, String poolName);
        }

        /**
         * The stage of the volume definition allowing to specify CreationToken.
         */
        interface WithCreationToken {
           /**
            * Specifies creationToken.
            * @param creationToken A unique file path for the volume. Used when creating mount targets
            * @return the next definition stage
            */
            WithLocation withCreationToken(String creationToken);
        }

        /**
         * The stage of the volume definition allowing to specify Location.
         */
        interface WithLocation {
           /**
            * Specifies location.
            * @param location the location parameter value
            * @return the next definition stage
            */
            WithSubnetId withLocation(String location);
        }

        /**
         * The stage of the volume definition allowing to specify SubnetId.
         */
        interface WithSubnetId {
           /**
            * Specifies subnetId.
            * @param subnetId The Azure Resource URI for a delegated subnet. Must have the delegation Microsoft.NetApp/volumes
            * @return the next definition stage
            */
            WithUsageThreshold withSubnetId(String subnetId);
        }

        /**
         * The stage of the volume definition allowing to specify UsageThreshold.
         */
        interface WithUsageThreshold {
           /**
            * Specifies usageThreshold.
            * @param usageThreshold Maximum storage quota allowed for a file system in bytes. This is a soft quota used for alerting only. Minimum size is 100 GiB. Upper limit is 100TiB. Specified in bytes
            * @return the next definition stage
            */
            WithCreate withUsageThreshold(long usageThreshold);
        }

        /**
         * The stage of the volume definition allowing to specify BackupId.
         */
        interface WithBackupId {
            /**
             * Specifies backupId.
             * @param backupId UUID v4 or resource identifier used to identify the Backup
             * @return the next definition stage
             */
            WithCreate withBackupId(String backupId);
        }

        /**
         * The stage of the volume definition allowing to specify DataProtection.
         */
        interface WithDataProtection {
            /**
             * Specifies dataProtection.
             * @param dataProtection DataProtection type volumes include an object containing details of the replication
             * @return the next definition stage
             */
            WithCreate withDataProtection(VolumePropertiesDataProtection dataProtection);
        }

        /**
         * The stage of the volume definition allowing to specify ExportPolicy.
         */
        interface WithExportPolicy {
            /**
             * Specifies exportPolicy.
             * @param exportPolicy Set of export policy rules
             * @return the next definition stage
             */
            WithCreate withExportPolicy(VolumePropertiesExportPolicy exportPolicy);
        }

        /**
         * The stage of the volume definition allowing to specify IsRestoring.
         */
        interface WithIsRestoring {
            /**
             * Specifies isRestoring.
             * @param isRestoring Restoring
             * @return the next definition stage
             */
            WithCreate withIsRestoring(Boolean isRestoring);
        }

        /**
         * The stage of the volume definition allowing to specify KerberosEnabled.
         */
        interface WithKerberosEnabled {
            /**
             * Specifies kerberosEnabled.
             * @param kerberosEnabled Describe if a volume is KerberosEnabled. To be use with swagger version 2020-05-01 or later
             * @return the next definition stage
             */
            WithCreate withKerberosEnabled(Boolean kerberosEnabled);
        }

        /**
         * The stage of the volume definition allowing to specify MountTargets.
         */
        interface WithMountTargets {
            /**
             * Specifies mountTargets.
             * @param mountTargets List of mount targets
             * @return the next definition stage
             */
            WithCreate withMountTargets(List<MountTargetProperties> mountTargets);
        }

        /**
         * The stage of the volume definition allowing to specify ProtocolTypes.
         */
        interface WithProtocolTypes {
            /**
             * Specifies protocolTypes.
             * @param protocolTypes Set of protocol types
             * @return the next definition stage
             */
            WithCreate withProtocolTypes(List<String> protocolTypes);
        }

        /**
         * The stage of the volume definition allowing to specify SecurityStyle.
         */
        interface WithSecurityStyle {
            /**
             * Specifies securityStyle.
             * @param securityStyle The security style of volume. Possible values include: 'ntfs', 'unix'
             * @return the next definition stage
             */
            WithCreate withSecurityStyle(SecurityStyle securityStyle);
        }

        /**
         * The stage of the volume definition allowing to specify ServiceLevel.
         */
        interface WithServiceLevel {
            /**
             * Specifies serviceLevel.
             * @param serviceLevel The service level of the file system. Possible values include: 'Standard', 'Premium', 'Ultra'
             * @return the next definition stage
             */
            WithCreate withServiceLevel(ServiceLevel serviceLevel);
        }

        /**
         * The stage of the volume definition allowing to specify SmbContinuouslyAvailable.
         */
        interface WithSmbContinuouslyAvailable {
            /**
             * Specifies smbContinuouslyAvailable.
             * @param smbContinuouslyAvailable Enables continuously available share property for smb volume. Only applicable for SMB volume
             * @return the next definition stage
             */
            WithCreate withSmbContinuouslyAvailable(Boolean smbContinuouslyAvailable);
        }

        /**
         * The stage of the volume definition allowing to specify SmbEncryption.
         */
        interface WithSmbEncryption {
            /**
             * Specifies smbEncryption.
             * @param smbEncryption Enables encryption for in-flight smb3 data. Only applicable for SMB/DualProtocol volume. To be used with swagger version 2020-08-01 or later
             * @return the next definition stage
             */
            WithCreate withSmbEncryption(Boolean smbEncryption);
        }

        /**
         * The stage of the volume definition allowing to specify SnapshotDirectoryVisible.
         */
        interface WithSnapshotDirectoryVisible {
            /**
             * Specifies snapshotDirectoryVisible.
             * @param snapshotDirectoryVisible If enabled (true) the volume will contain a read-only .snapshot directory which provides access to each of the volume's snapshots (default to true)
             * @return the next definition stage
             */
            WithCreate withSnapshotDirectoryVisible(Boolean snapshotDirectoryVisible);
        }

        /**
         * The stage of the volume definition allowing to specify SnapshotId.
         */
        interface WithSnapshotId {
            /**
             * Specifies snapshotId.
             * @param snapshotId UUID v4 or resource identifier used to identify the Snapshot
             * @return the next definition stage
             */
            WithCreate withSnapshotId(String snapshotId);
        }

        /**
         * The stage of the volume definition allowing to specify Tags.
         */
        interface WithTags {
            /**
             * Specifies tags.
             * @param tags the tags parameter value
             * @return the next definition stage
             */
            WithCreate withTags(Map<String, String> tags);
        }

        /**
         * The stage of the volume definition allowing to specify ThroughputMibps.
         */
        interface WithThroughputMibps {
            /**
             * Specifies throughputMibps.
             * @param throughputMibps the throughputMibps parameter value
             * @return the next definition stage
             */
            WithCreate withThroughputMibps(Double throughputMibps);
        }

        /**
         * The stage of the volume definition allowing to specify VolumeType.
         */
        interface WithVolumeType {
            /**
             * Specifies volumeType.
             * @param volumeType What type of volume is this
             * @return the next definition stage
             */
            WithCreate withVolumeType(String volumeType);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<Volume>, DefinitionStages.WithBackupId, DefinitionStages.WithDataProtection, DefinitionStages.WithExportPolicy, DefinitionStages.WithIsRestoring, DefinitionStages.WithKerberosEnabled, DefinitionStages.WithMountTargets, DefinitionStages.WithProtocolTypes, DefinitionStages.WithSecurityStyle, DefinitionStages.WithServiceLevel, DefinitionStages.WithSmbContinuouslyAvailable, DefinitionStages.WithSmbEncryption, DefinitionStages.WithSnapshotDirectoryVisible, DefinitionStages.WithSnapshotId, DefinitionStages.WithTags, DefinitionStages.WithThroughputMibps, DefinitionStages.WithVolumeType {
        }
    }
    /**
     * The template for a Volume update operation, containing all the settings that can be modified.
     */
    interface Update extends Appliable<Volume>, UpdateStages.WithDataProtection, UpdateStages.WithExportPolicy, UpdateStages.WithServiceLevel, UpdateStages.WithTags, UpdateStages.WithThroughputMibps, UpdateStages.WithUsageThreshold {
    }

    /**
     * Grouping of Volume update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the volume update allowing to specify DataProtection.
         */
        interface WithDataProtection {
            /**
             * Specifies dataProtection.
             * @param dataProtection DataProtection type volumes include an object containing details of the replication
             * @return the next update stage
             */
            Update withDataProtection(VolumePatchPropertiesDataProtection dataProtection);
        }

        /**
         * The stage of the volume update allowing to specify ExportPolicy.
         */
        interface WithExportPolicy {
            /**
             * Specifies exportPolicy.
             * @param exportPolicy Set of export policy rules
             * @return the next update stage
             */
            Update withExportPolicy(VolumePatchPropertiesExportPolicy exportPolicy);
        }

        /**
         * The stage of the volume update allowing to specify ServiceLevel.
         */
        interface WithServiceLevel {
            /**
             * Specifies serviceLevel.
             * @param serviceLevel The service level of the file system. Possible values include: 'Standard', 'Premium', 'Ultra'
             * @return the next update stage
             */
            Update withServiceLevel(ServiceLevel serviceLevel);
        }

        /**
         * The stage of the volume update allowing to specify Tags.
         */
        interface WithTags {
            /**
             * Specifies tags.
             * @param tags the tags parameter value
             * @return the next update stage
             */
            Update withTags(Map<String, String> tags);
        }

        /**
         * The stage of the volume update allowing to specify ThroughputMibps.
         */
        interface WithThroughputMibps {
            /**
             * Specifies throughputMibps.
             * @param throughputMibps the throughputMibps parameter value
             * @return the next update stage
             */
            Update withThroughputMibps(Double throughputMibps);
        }

        /**
         * The stage of the volume update allowing to specify UsageThreshold.
         */
        interface WithUsageThreshold {
            /**
             * Specifies usageThreshold.
             * @param usageThreshold Maximum storage quota allowed for a file system in bytes. This is a soft quota used for alerting only. Minimum size is 100 GiB. Upper limit is 100TiB. Specified in bytes
             * @return the next update stage
             */
            Update withUsageThreshold(Long usageThreshold);
        }

    }
}
