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
     * Gets or sets a string that uniquely identifies the pool within the
     * account. The id can contain any combination of alphanumeric characters
     * including hyphens and underscores, and cannot contain more than 64
     * characters.
     */
    private String id;

    /**
     * Gets or sets the display name for the pool.
     */
    private String displayName;

    /**
     * Gets or sets the URL of the pool.
     */
    private String url;

    /**
     * Gets or sets the ETag of the pool.
     */
    private String eTag;

    /**
     * Gets or sets the last modified time of the pool.
     */
    private DateTime lastModified;

    /**
     * Gets or sets the creation time of the pool.
     */
    private DateTime creationTime;

    /**
     * Gets or sets the current state of the pool. Possible values include:
     * 'active', 'deleting', 'upgrading'.
     */
    private PoolState state;

    /**
     * Gets or sets the time at which the pool entered its current state.
     */
    private DateTime stateTransitionTime;

    /**
     * Gets or sets whether the pool is resizing. Possible values include:
     * 'steady', 'resizing', 'stopping'.
     */
    private AllocationState allocationState;

    /**
     * Gets or sets the time at which the pool entered its current allocation
     * state.
     */
    private DateTime allocationStateTransitionTime;

    /**
     * Gets or sets the size of virtual machines in the pool.  All VMs in a
     * pool are the same size.
     */
    private String vmSize;

    /**
     * Gets or sets the Azure Guest OS family to be installed on the virtual
     * machines in the pool.
     */
    private String osFamily;

    /**
     * Gets or sets the Azure Guest OS version to be installed on the virtual
     * machines in the pool. The default value is * which specifies the
     * latest operating system version for the specified family.
     */
    private String targetOSVersion;

    /**
     * Gets or sets the Azure Guest OS Version currently installed on the
     * virtual machines in the pool. This may differ from TargetOSVersion if
     * the pool state is Upgrading.
     */
    private String currentOSVersion;

    /**
     * Gets or sets the timeout for allocation of compute nodes to the pool.
     * In a Get Pool operation, this is the timeout for the most recent
     * resize operation. The default value is 10 minutes.
     */
    private Period resizeTimeout;

    /**
     * Gets or sets details of any error encountered while performing the last
     * resize on the pool. This property is set only if an error occurred
     * during the last pool resize, and only when the pool AllocationState is
     * Steady.
     */
    private ResizeError resizeError;

    /**
     * Gets or sets the number of compute nodes currently in the pool.
     */
    private Integer currentDedicated;

    /**
     * Gets or sets the desired number of compute nodes in the pool. This
     * property must have the default value if EnableAutoScale is true. It is
     * required if EnableAutoScale is false.
     */
    private Integer targetDedicated;

    /**
     * Gets or sets whether the pool size should automatically adjust over
     * time. If true, the AutoScaleFormula property must be set. If false,
     * the TargetDedicated property must be set.
     */
    private Boolean enableAutoScale;

    /**
     * Gets or sets a formula for the desired number of compute nodes in the
     * pool.
     */
    private String autoScaleFormula;

    /**
     * Gets or sets a time interval for the desired AutoScale evaluation
     * period in the pool.
     */
    private Period autoScaleEvaluationInterval;

    /**
     * Gets or sets the results and errors from the last execution of the
     * autoscale formula.
     */
    private AutoScaleRun autoScaleRun;

    /**
     * Gets or sets whether the pool permits direct communication between
     * nodes.
     */
    private Boolean enableInterNodeCommunication;

    /**
     * Gets or sets a task specified to run on each compute node as it joins
     * the pool.
     */
    private StartTask startTask;

    /**
     * Gets or sets the list of certificates to be installed on each compute
     * node in the pool.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * Gets or sets the list of application packages to be installed on each
     * compute node in the pool.
     */
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * Gets or sets the maximum number of tasks that can run concurrently on a
     * single compute node in the pool.
     */
    private Integer maxTasksPerNode;

    /**
     * Gets or sets how the Batch service distributes tasks between compute
     * nodes in the pool.
     */
    private TaskSchedulingPolicy taskSchedulingPolicy;

    /**
     * Gets or sets a list of name-value pairs associated with the pool as
     * metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * Gets or sets utilization and resource usage statistics for the entire
     * lifetime of the pool.
     */
    private PoolStatistics stats;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     */
    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime getCreationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     */
    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public PoolState getState() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     */
    public void setState(PoolState state) {
        this.state = state;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime getStateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     */
    public void setStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
    }

    /**
     * Get the allocationState value.
     *
     * @return the allocationState value
     */
    public AllocationState getAllocationState() {
        return this.allocationState;
    }

    /**
     * Set the allocationState value.
     *
     * @param allocationState the allocationState value to set
     */
    public void setAllocationState(AllocationState allocationState) {
        this.allocationState = allocationState;
    }

    /**
     * Get the allocationStateTransitionTime value.
     *
     * @return the allocationStateTransitionTime value
     */
    public DateTime getAllocationStateTransitionTime() {
        return this.allocationStateTransitionTime;
    }

    /**
     * Set the allocationStateTransitionTime value.
     *
     * @param allocationStateTransitionTime the allocationStateTransitionTime value to set
     */
    public void setAllocationStateTransitionTime(DateTime allocationStateTransitionTime) {
        this.allocationStateTransitionTime = allocationStateTransitionTime;
    }

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String getVmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     */
    public void setVmSize(String vmSize) {
        this.vmSize = vmSize;
    }

    /**
     * Get the osFamily value.
     *
     * @return the osFamily value
     */
    public String getOsFamily() {
        return this.osFamily;
    }

    /**
     * Set the osFamily value.
     *
     * @param osFamily the osFamily value to set
     */
    public void setOsFamily(String osFamily) {
        this.osFamily = osFamily;
    }

    /**
     * Get the targetOSVersion value.
     *
     * @return the targetOSVersion value
     */
    public String getTargetOSVersion() {
        return this.targetOSVersion;
    }

    /**
     * Set the targetOSVersion value.
     *
     * @param targetOSVersion the targetOSVersion value to set
     */
    public void setTargetOSVersion(String targetOSVersion) {
        this.targetOSVersion = targetOSVersion;
    }

    /**
     * Get the currentOSVersion value.
     *
     * @return the currentOSVersion value
     */
    public String getCurrentOSVersion() {
        return this.currentOSVersion;
    }

    /**
     * Set the currentOSVersion value.
     *
     * @param currentOSVersion the currentOSVersion value to set
     */
    public void setCurrentOSVersion(String currentOSVersion) {
        this.currentOSVersion = currentOSVersion;
    }

    /**
     * Get the resizeTimeout value.
     *
     * @return the resizeTimeout value
     */
    public Period getResizeTimeout() {
        return this.resizeTimeout;
    }

    /**
     * Set the resizeTimeout value.
     *
     * @param resizeTimeout the resizeTimeout value to set
     */
    public void setResizeTimeout(Period resizeTimeout) {
        this.resizeTimeout = resizeTimeout;
    }

    /**
     * Get the resizeError value.
     *
     * @return the resizeError value
     */
    public ResizeError getResizeError() {
        return this.resizeError;
    }

    /**
     * Set the resizeError value.
     *
     * @param resizeError the resizeError value to set
     */
    public void setResizeError(ResizeError resizeError) {
        this.resizeError = resizeError;
    }

    /**
     * Get the currentDedicated value.
     *
     * @return the currentDedicated value
     */
    public Integer getCurrentDedicated() {
        return this.currentDedicated;
    }

    /**
     * Set the currentDedicated value.
     *
     * @param currentDedicated the currentDedicated value to set
     */
    public void setCurrentDedicated(Integer currentDedicated) {
        this.currentDedicated = currentDedicated;
    }

    /**
     * Get the targetDedicated value.
     *
     * @return the targetDedicated value
     */
    public Integer getTargetDedicated() {
        return this.targetDedicated;
    }

    /**
     * Set the targetDedicated value.
     *
     * @param targetDedicated the targetDedicated value to set
     */
    public void setTargetDedicated(Integer targetDedicated) {
        this.targetDedicated = targetDedicated;
    }

    /**
     * Get the enableAutoScale value.
     *
     * @return the enableAutoScale value
     */
    public Boolean getEnableAutoScale() {
        return this.enableAutoScale;
    }

    /**
     * Set the enableAutoScale value.
     *
     * @param enableAutoScale the enableAutoScale value to set
     */
    public void setEnableAutoScale(Boolean enableAutoScale) {
        this.enableAutoScale = enableAutoScale;
    }

    /**
     * Get the autoScaleFormula value.
     *
     * @return the autoScaleFormula value
     */
    public String getAutoScaleFormula() {
        return this.autoScaleFormula;
    }

    /**
     * Set the autoScaleFormula value.
     *
     * @param autoScaleFormula the autoScaleFormula value to set
     */
    public void setAutoScaleFormula(String autoScaleFormula) {
        this.autoScaleFormula = autoScaleFormula;
    }

    /**
     * Get the autoScaleEvaluationInterval value.
     *
     * @return the autoScaleEvaluationInterval value
     */
    public Period getAutoScaleEvaluationInterval() {
        return this.autoScaleEvaluationInterval;
    }

    /**
     * Set the autoScaleEvaluationInterval value.
     *
     * @param autoScaleEvaluationInterval the autoScaleEvaluationInterval value to set
     */
    public void setAutoScaleEvaluationInterval(Period autoScaleEvaluationInterval) {
        this.autoScaleEvaluationInterval = autoScaleEvaluationInterval;
    }

    /**
     * Get the autoScaleRun value.
     *
     * @return the autoScaleRun value
     */
    public AutoScaleRun getAutoScaleRun() {
        return this.autoScaleRun;
    }

    /**
     * Set the autoScaleRun value.
     *
     * @param autoScaleRun the autoScaleRun value to set
     */
    public void setAutoScaleRun(AutoScaleRun autoScaleRun) {
        this.autoScaleRun = autoScaleRun;
    }

    /**
     * Get the enableInterNodeCommunication value.
     *
     * @return the enableInterNodeCommunication value
     */
    public Boolean getEnableInterNodeCommunication() {
        return this.enableInterNodeCommunication;
    }

    /**
     * Set the enableInterNodeCommunication value.
     *
     * @param enableInterNodeCommunication the enableInterNodeCommunication value to set
     */
    public void setEnableInterNodeCommunication(Boolean enableInterNodeCommunication) {
        this.enableInterNodeCommunication = enableInterNodeCommunication;
    }

    /**
     * Get the startTask value.
     *
     * @return the startTask value
     */
    public StartTask getStartTask() {
        return this.startTask;
    }

    /**
     * Set the startTask value.
     *
     * @param startTask the startTask value to set
     */
    public void setStartTask(StartTask startTask) {
        this.startTask = startTask;
    }

    /**
     * Get the certificateReferences value.
     *
     * @return the certificateReferences value
     */
    public List<CertificateReference> getCertificateReferences() {
        return this.certificateReferences;
    }

    /**
     * Set the certificateReferences value.
     *
     * @param certificateReferences the certificateReferences value to set
     */
    public void setCertificateReferences(List<CertificateReference> certificateReferences) {
        this.certificateReferences = certificateReferences;
    }

    /**
     * Get the applicationPackageReferences value.
     *
     * @return the applicationPackageReferences value
     */
    public List<ApplicationPackageReference> getApplicationPackageReferences() {
        return this.applicationPackageReferences;
    }

    /**
     * Set the applicationPackageReferences value.
     *
     * @param applicationPackageReferences the applicationPackageReferences value to set
     */
    public void setApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
        this.applicationPackageReferences = applicationPackageReferences;
    }

    /**
     * Get the maxTasksPerNode value.
     *
     * @return the maxTasksPerNode value
     */
    public Integer getMaxTasksPerNode() {
        return this.maxTasksPerNode;
    }

    /**
     * Set the maxTasksPerNode value.
     *
     * @param maxTasksPerNode the maxTasksPerNode value to set
     */
    public void setMaxTasksPerNode(Integer maxTasksPerNode) {
        this.maxTasksPerNode = maxTasksPerNode;
    }

    /**
     * Get the taskSchedulingPolicy value.
     *
     * @return the taskSchedulingPolicy value
     */
    public TaskSchedulingPolicy getTaskSchedulingPolicy() {
        return this.taskSchedulingPolicy;
    }

    /**
     * Set the taskSchedulingPolicy value.
     *
     * @param taskSchedulingPolicy the taskSchedulingPolicy value to set
     */
    public void setTaskSchedulingPolicy(TaskSchedulingPolicy taskSchedulingPolicy) {
        this.taskSchedulingPolicy = taskSchedulingPolicy;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     */
    public void setMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the stats value.
     *
     * @return the stats value
     */
    public PoolStatistics getStats() {
        return this.stats;
    }

    /**
     * Set the stats value.
     *
     * @param stats the stats value to set
     */
    public void setStats(PoolStatistics stats) {
        this.stats = stats;
    }

}
