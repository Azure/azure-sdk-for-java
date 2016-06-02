/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.Period;
import java.util.List;

/**
 * Specification for creating a new pool.
 */
public class PoolSpecification {
    /**
     * The display name for the pool.
     */
    private String displayName;

    /**
     * The size of the virtual machines in the pool. All virtual machines in a
     * pool are the same size.
     */
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
     * The maximum number of tasks that can run concurrently on a single
     * compute node in the pool.
     */
    private Integer maxTasksPerNode;

    /**
     * How tasks are distributed among compute nodes in the pool.
     */
    private TaskSchedulingPolicy taskSchedulingPolicy;

    /**
     * The timeout for allocation of compute nodes to the pool.
     */
    private Period resizeTimeout;

    /**
     * The desired number of compute nodes in the pool.
     */
    private Integer targetDedicated;

    /**
     * Whether the pool size should automatically adjust over time.
     */
    private Boolean enableAutoScale;

    /**
     * The formula for the desired number of compute nodes in the pool.
     */
    private String autoScaleFormula;

    /**
     * A time interval for the desired AutoScale evaluation period in the pool.
     */
    private Period autoScaleEvaluationInterval;

    /**
     * Whether the pool permits direct communication between nodes.
     */
    private Boolean enableInterNodeCommunication;

    /**
     * A task to run on each compute node as it joins the pool. The task runs
     * when the node is added to the pool or when the node is restarted.
     */
    private StartTask startTask;

    /**
     * A list of certificates to be installed on each compute node in the pool.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * The list of application packages to be installed on each compute node
     * in the pool.
     */
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * A list of name-value pairs associated with the pool as metadata.
     */
    private List<MetadataItem> metadata;

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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withDisplayName(String displayName) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withVmSize(String vmSize) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withCloudServiceConfiguration(CloudServiceConfiguration cloudServiceConfiguration) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration) {
        this.virtualMachineConfiguration = virtualMachineConfiguration;
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withMaxTasksPerNode(Integer maxTasksPerNode) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withTaskSchedulingPolicy(TaskSchedulingPolicy taskSchedulingPolicy) {
        this.taskSchedulingPolicy = taskSchedulingPolicy;
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withResizeTimeout(Period resizeTimeout) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withTargetDedicated(Integer targetDedicated) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withEnableAutoScale(Boolean enableAutoScale) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withAutoScaleFormula(String autoScaleFormula) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withAutoScaleEvaluationInterval(Period autoScaleEvaluationInterval) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withEnableInterNodeCommunication(Boolean enableInterNodeCommunication) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withStartTask(StartTask startTask) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withCertificateReferences(List<CertificateReference> certificateReferences) {
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
        this.applicationPackageReferences = applicationPackageReferences;
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
     * @return the PoolSpecification object itself.
     */
    public PoolSpecification withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
