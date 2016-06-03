/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import org.joda.time.Period;
import java.util.List;

/**
 * A pool in the Azure Batch service.
 */
public class CloudPool {
    /**
     * A string that uniquely identifies the pool within the account. The id
     * can contain any combination of alphanumeric characters including
     * hyphens and underscores, and cannot contain more than 64 characters.
     */
    private String id;

    /**
     * The display name for the pool.
     */
    private String displayName;

    /**
     * The URL of the pool.
     */
    private String url;

    /**
     * The ETag of the pool.
     */
    private String eTag;

    /**
     * The last modified time of the pool.
     */
    private DateTime lastModified;

    /**
     * The creation time of the pool.
     */
    private DateTime creationTime;

    /**
     * The current state of the pool. Possible values include: 'active',
     * 'deleting', 'upgrading'.
     */
    private PoolState state;

    /**
     * The time at which the pool entered its current state.
     */
    private DateTime stateTransitionTime;

    /**
     * Whether the pool is resizing. Possible values include: 'steady',
     * 'resizing', 'stopping'.
     */
    private AllocationState allocationState;

    /**
     * The time at which the pool entered its current allocation state.
     */
    private DateTime allocationStateTransitionTime;

    /**
     * The size of virtual machines in the pool. All virtual machines in a
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
     * The timeout for allocation of compute nodes to the pool. In a Get Pool
     * operation, this is the timeout for the most recent resize operation.
     * The default value is 10 minutes.
     */
    private Period resizeTimeout;

    /**
     * Details of any error encountered while performing the last resize on
     * the pool. This property is set only if an error occurred during the
     * last pool resize, and only when the pool AllocationState is Steady.
     */
    private ResizeError resizeError;

    /**
     * The number of compute nodes currently in the pool.
     */
    private Integer currentDedicated;

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
     * A time interval for the desired AutoScale evaluation period in the pool.
     */
    private Period autoScaleEvaluationInterval;

    /**
     * The results and errors from the last execution of the autoscale formula.
     */
    private AutoScaleRun autoScaleRun;

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
     * Utilization and resource usage statistics for the entire lifetime of
     * the pool.
     */
    private PoolStatistics stats;

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
     * @return the CloudPool object itself.
     */
    public CloudPool withId(String id) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public PoolState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withState(PoolState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime stateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
        return this;
    }

    /**
     * Get the allocationState value.
     *
     * @return the allocationState value
     */
    public AllocationState allocationState() {
        return this.allocationState;
    }

    /**
     * Set the allocationState value.
     *
     * @param allocationState the allocationState value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withAllocationState(AllocationState allocationState) {
        this.allocationState = allocationState;
        return this;
    }

    /**
     * Get the allocationStateTransitionTime value.
     *
     * @return the allocationStateTransitionTime value
     */
    public DateTime allocationStateTransitionTime() {
        return this.allocationStateTransitionTime;
    }

    /**
     * Set the allocationStateTransitionTime value.
     *
     * @param allocationStateTransitionTime the allocationStateTransitionTime value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withAllocationStateTransitionTime(DateTime allocationStateTransitionTime) {
        this.allocationStateTransitionTime = allocationStateTransitionTime;
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
     * @return the CloudPool object itself.
     */
    public CloudPool withVmSize(String vmSize) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withCloudServiceConfiguration(CloudServiceConfiguration cloudServiceConfiguration) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withResizeTimeout(Period resizeTimeout) {
        this.resizeTimeout = resizeTimeout;
        return this;
    }

    /**
     * Get the resizeError value.
     *
     * @return the resizeError value
     */
    public ResizeError resizeError() {
        return this.resizeError;
    }

    /**
     * Set the resizeError value.
     *
     * @param resizeError the resizeError value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withResizeError(ResizeError resizeError) {
        this.resizeError = resizeError;
        return this;
    }

    /**
     * Get the currentDedicated value.
     *
     * @return the currentDedicated value
     */
    public Integer currentDedicated() {
        return this.currentDedicated;
    }

    /**
     * Set the currentDedicated value.
     *
     * @param currentDedicated the currentDedicated value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withCurrentDedicated(Integer currentDedicated) {
        this.currentDedicated = currentDedicated;
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
     * @return the CloudPool object itself.
     */
    public CloudPool withTargetDedicated(Integer targetDedicated) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withEnableAutoScale(Boolean enableAutoScale) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withAutoScaleFormula(String autoScaleFormula) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withAutoScaleEvaluationInterval(Period autoScaleEvaluationInterval) {
        this.autoScaleEvaluationInterval = autoScaleEvaluationInterval;
        return this;
    }

    /**
     * Get the autoScaleRun value.
     *
     * @return the autoScaleRun value
     */
    public AutoScaleRun autoScaleRun() {
        return this.autoScaleRun;
    }

    /**
     * Set the autoScaleRun value.
     *
     * @param autoScaleRun the autoScaleRun value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withAutoScaleRun(AutoScaleRun autoScaleRun) {
        this.autoScaleRun = autoScaleRun;
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
     * @return the CloudPool object itself.
     */
    public CloudPool withEnableInterNodeCommunication(Boolean enableInterNodeCommunication) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withStartTask(StartTask startTask) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withCertificateReferences(List<CertificateReference> certificateReferences) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withMaxTasksPerNode(Integer maxTasksPerNode) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withTaskSchedulingPolicy(TaskSchedulingPolicy taskSchedulingPolicy) {
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
     * @return the CloudPool object itself.
     */
    public CloudPool withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the stats value.
     *
     * @return the stats value
     */
    public PoolStatistics stats() {
        return this.stats;
    }

    /**
     * Set the stats value.
     *
     * @param stats the stats value to set
     * @return the CloudPool object itself.
     */
    public CloudPool withStats(PoolStatistics stats) {
        this.stats = stats;
        return this;
    }

}
