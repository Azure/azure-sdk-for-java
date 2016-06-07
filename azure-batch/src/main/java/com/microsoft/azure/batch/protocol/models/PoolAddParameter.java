/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.Period;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A pool in the Azure Batch service to add.
 */
public class PoolAddParameter {
    /**
     * A string that uniquely identifies the pool within the account. The id
     * can contain any combination of alphanumeric characters including
     * hyphens and underscores, and cannot contain more than 64 characters.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * The display name for the pool.
     */
    private String displayName;

    /**
     * The size of virtual machines in the pool. All virtual machines in a
     * pool are the same size.
     */
    @JsonProperty(required = true)
    private String vmSize;

    /**
     * The cloud service configuration for the pool. This property and
     * VirtualMachineConfiguration are mutually exclusive and one of the
     * properties must be specified.
     */
    private CloudServiceConfiguration cloudServiceConfiguration;

    /**
     * The virtual machine configuration for the pool. This property and
     * CloudServiceConfiguration are mutually exclusive and one of the
     * properties must be specified.
     */
    private VirtualMachineConfiguration virtualMachineConfiguration;

    /**
     * The timeout for allocation of compute nodes to the pool. In a Get Pool
     * operation, this is the timeout for the most recent resize operation.
     * The default value is 10 minutes.
     */
    private Period resizeTimeout;

    /**
     * The desired number of compute nodes in the pool. This property must
     * have the default value if EnableAutoScale is true. It is required if
     * EnableAutoScale is false.
     */
    private Integer targetDedicated;

    /**
     * Whether the pool size should automatically adjust over time. If true,
     * the AutoScaleFormula property must be set. If false, the
     * TargetDedicated property must be set.
     */
    private Boolean enableAutoScale;

    /**
     * A formula for the desired number of compute nodes in the pool.
     */
    private String autoScaleFormula;

    /**
     * A time interval for the desired autoscale evaluation period in the pool.
     */
    private Period autoScaleEvaluationInterval;

    /**
     * Whether the pool permits direct communication between nodes.
     */
    private Boolean enableInterNodeCommunication;

    /**
     * A task specified to run on each compute node as it joins the pool.
     */
    private StartTask startTask;

    /**
     * The list of certificates to be installed on each compute node in the
     * pool.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * The list of application packages to be installed on each compute node
     * in the pool.
     */
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * The maximum number of tasks that can run concurrently on a single
     * compute node in the pool.
     */
    private Integer maxTasksPerNode;

    /**
     * How the Batch service distributes tasks between compute nodes in the
     * pool.
     */
    private TaskSchedulingPolicy taskSchedulingPolicy;

    /**
     * A list of name-value pairs associated with the pool as metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String vmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withVmSize(String vmSize) {
        this.vmSize = vmSize;
        return this;
    }

    /**
     * Get the cloudServiceConfiguration value.
     *
     * @return the cloudServiceConfiguration value
     */
    public CloudServiceConfiguration cloudServiceConfiguration() {
        return this.cloudServiceConfiguration;
    }

    /**
     * Set the cloudServiceConfiguration value.
     *
     * @param cloudServiceConfiguration the cloudServiceConfiguration value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withCloudServiceConfiguration(CloudServiceConfiguration cloudServiceConfiguration) {
        this.cloudServiceConfiguration = cloudServiceConfiguration;
        return this;
    }

    /**
     * Get the virtualMachineConfiguration value.
     *
     * @return the virtualMachineConfiguration value
     */
    public VirtualMachineConfiguration virtualMachineConfiguration() {
        return this.virtualMachineConfiguration;
    }

    /**
     * Set the virtualMachineConfiguration value.
     *
     * @param virtualMachineConfiguration the virtualMachineConfiguration value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration) {
        this.virtualMachineConfiguration = virtualMachineConfiguration;
        return this;
    }

    /**
     * Get the resizeTimeout value.
     *
     * @return the resizeTimeout value
     */
    public Period resizeTimeout() {
        return this.resizeTimeout;
    }

    /**
     * Set the resizeTimeout value.
     *
     * @param resizeTimeout the resizeTimeout value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withResizeTimeout(Period resizeTimeout) {
        this.resizeTimeout = resizeTimeout;
        return this;
    }

    /**
     * Get the targetDedicated value.
     *
     * @return the targetDedicated value
     */
    public Integer targetDedicated() {
        return this.targetDedicated;
    }

    /**
     * Set the targetDedicated value.
     *
     * @param targetDedicated the targetDedicated value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withTargetDedicated(Integer targetDedicated) {
        this.targetDedicated = targetDedicated;
        return this;
    }

    /**
     * Get the enableAutoScale value.
     *
     * @return the enableAutoScale value
     */
    public Boolean enableAutoScale() {
        return this.enableAutoScale;
    }

    /**
     * Set the enableAutoScale value.
     *
     * @param enableAutoScale the enableAutoScale value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withEnableAutoScale(Boolean enableAutoScale) {
        this.enableAutoScale = enableAutoScale;
        return this;
    }

    /**
     * Get the autoScaleFormula value.
     *
     * @return the autoScaleFormula value
     */
    public String autoScaleFormula() {
        return this.autoScaleFormula;
    }

    /**
     * Set the autoScaleFormula value.
     *
     * @param autoScaleFormula the autoScaleFormula value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withAutoScaleFormula(String autoScaleFormula) {
        this.autoScaleFormula = autoScaleFormula;
        return this;
    }

    /**
     * Get the autoScaleEvaluationInterval value.
     *
     * @return the autoScaleEvaluationInterval value
     */
    public Period autoScaleEvaluationInterval() {
        return this.autoScaleEvaluationInterval;
    }

    /**
     * Set the autoScaleEvaluationInterval value.
     *
     * @param autoScaleEvaluationInterval the autoScaleEvaluationInterval value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withAutoScaleEvaluationInterval(Period autoScaleEvaluationInterval) {
        this.autoScaleEvaluationInterval = autoScaleEvaluationInterval;
        return this;
    }

    /**
     * Get the enableInterNodeCommunication value.
     *
     * @return the enableInterNodeCommunication value
     */
    public Boolean enableInterNodeCommunication() {
        return this.enableInterNodeCommunication;
    }

    /**
     * Set the enableInterNodeCommunication value.
     *
     * @param enableInterNodeCommunication the enableInterNodeCommunication value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withEnableInterNodeCommunication(Boolean enableInterNodeCommunication) {
        this.enableInterNodeCommunication = enableInterNodeCommunication;
        return this;
    }

    /**
     * Get the startTask value.
     *
     * @return the startTask value
     */
    public StartTask startTask() {
        return this.startTask;
    }

    /**
     * Set the startTask value.
     *
     * @param startTask the startTask value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withStartTask(StartTask startTask) {
        this.startTask = startTask;
        return this;
    }

    /**
     * Get the certificateReferences value.
     *
     * @return the certificateReferences value
     */
    public List<CertificateReference> certificateReferences() {
        return this.certificateReferences;
    }

    /**
     * Set the certificateReferences value.
     *
     * @param certificateReferences the certificateReferences value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withCertificateReferences(List<CertificateReference> certificateReferences) {
        this.certificateReferences = certificateReferences;
        return this;
    }

    /**
     * Get the applicationPackageReferences value.
     *
     * @return the applicationPackageReferences value
     */
    public List<ApplicationPackageReference> applicationPackageReferences() {
        return this.applicationPackageReferences;
    }

    /**
     * Set the applicationPackageReferences value.
     *
     * @param applicationPackageReferences the applicationPackageReferences value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
        this.applicationPackageReferences = applicationPackageReferences;
        return this;
    }

    /**
     * Get the maxTasksPerNode value.
     *
     * @return the maxTasksPerNode value
     */
    public Integer maxTasksPerNode() {
        return this.maxTasksPerNode;
    }

    /**
     * Set the maxTasksPerNode value.
     *
     * @param maxTasksPerNode the maxTasksPerNode value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withMaxTasksPerNode(Integer maxTasksPerNode) {
        this.maxTasksPerNode = maxTasksPerNode;
        return this;
    }

    /**
     * Get the taskSchedulingPolicy value.
     *
     * @return the taskSchedulingPolicy value
     */
    public TaskSchedulingPolicy taskSchedulingPolicy() {
        return this.taskSchedulingPolicy;
    }

    /**
     * Set the taskSchedulingPolicy value.
     *
     * @param taskSchedulingPolicy the taskSchedulingPolicy value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withTaskSchedulingPolicy(TaskSchedulingPolicy taskSchedulingPolicy) {
        this.taskSchedulingPolicy = taskSchedulingPolicy;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the PoolAddParameter object itself.
     */
    public PoolAddParameter withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
