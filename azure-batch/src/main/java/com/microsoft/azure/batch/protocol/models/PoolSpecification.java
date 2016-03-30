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
     * Gets or sets the display name for the pool.
     */
    private String displayName;

    /**
     * Gets or sets the size of the virtual machines in the pool. All VMs in a
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
     * machines in the pool.
     */
    private String targetOSVersion;

    /**
     * Gets or sets the maximum number of tasks that can run concurrently on a
     * single compute node in the pool.
     */
    private Integer maxTasksPerNode;

    /**
     * Gets or sets how tasks are distributed among compute nodes in the pool.
     */
    private TaskSchedulingPolicy taskSchedulingPolicy;

    /**
     * Gets or sets the timeout for allocation of compute nodes to the pool.
     */
    private Period resizeTimeout;

    /**
     * Gets or sets the desired number of compute nodes in the pool.
     */
    private Integer targetDedicated;

    /**
     * Gets or sets whether the pool size should automatically adjust over
     * time.
     */
    private Boolean enableAutoScale;

    /**
     * Gets or sets the formula for the desired number of compute nodes in the
     * pool.
     */
    private String autoScaleFormula;

    /**
     * Gets or sets a time interval for the desired AutoScale evaluation
     * period in the pool.
     */
    private Period autoScaleEvaluationInterval;

    /**
     * Gets or sets whether the pool permits direct communication between
     * nodes.
     */
    private Boolean enableInterNodeCommunication;

    /**
     * Gets or sets a task to run on each compute node as it joins the pool.
     * The task runs when the node is added to the pool or when the node is
     * restarted.
     */
    private StartTask startTask;

    /**
     * Gets or sets a list of certificates to be installed on each compute
     * node in the pool.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * Gets or sets the list of application packages to be installed on each
     * compute node in the pool.
     */
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * Gets or sets a list of name-value pairs associated with the pool as
     * metadata.
     */
    private List<MetadataItem> metadata;

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

}
