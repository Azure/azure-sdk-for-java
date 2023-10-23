package com.azure.compute.batch.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.core.http.RequestConditions;

/**
 * Represents a bag of optional parameters that can be used to configure various operations.
 */
public class OptionsBag {
    private BatchJobTerminateOptions batchJobTerminateOptionsbody;
    private OffsetDateTime endtime;
    private List<String> expand;
    private String filter;
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;
    private Integer maxresults;
    private NodeDisableSchedulingOptions nodeDisableSchedulingOptionsBody;
    private NodeRebootOptions nodeRebootOptionsBody;
    private NodeReimageOptions nodeReimageOptionsBody;
    private String ocpRange;
    private Boolean recursive;
    private RequestConditions requestConditions;
    private List<String> select;
    private OffsetDateTime starttime;
    private Integer timeOutInSeconds;

    public OptionsBag() {
        // Default constructor
    }

    /**
     * Gets the options to use for terminating the Batch Job.
     *
     * These options specify the behavior when terminating a Batch Job.
     *
     * @return The options for terminating the Batch Job.
     */
    public BatchJobTerminateOptions getBatchJobTerminateOptionsBody() {
        return batchJobTerminateOptionsbody;
    }

    /**
     * Sets the options to use for terminating the Batch Job.
     *
     * These options specify the behavior when terminating a Batch Job.
     *
     * @param batchJobTerminateOptionsbody The options for terminating the Batch Job.
     */
    public void setBatchJobTerminateOptionsBody(BatchJobTerminateOptions batchJobTerminateOptionsbody) {
        this.batchJobTerminateOptionsbody = batchJobTerminateOptionsbody;
    }

    /**
     * Gets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @return The latest time from which to include metrics.
     */
    public OffsetDateTime getEndTime() {
        return endtime;
    }

    /**
     * Sets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @param endtime The latest time from which to include metrics.
     */
    public void setEndTime(OffsetDateTime endtime) {
        this.endtime = endtime;
    }

    /**
     * Gets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @return The OData $expand clause.
     */
    public List<String> getExpand() {
        return expand;
    }

    /**
     * Sets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @param expand The OData $expand clause.
     */
    public void setExpand(List<String> expand) {
        this.expand = expand;
    }

    /**
     * Gets the OData $filter clause used for filtering results.
     *
     * @return The OData $filter clause.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the OData $filter clause used for filtering results.
     *
     * @param filter The OData $filter clause.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @param ifModifiedSince A timestamp indicating the last modified time of the resource.
     */
    public void setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @param ifUnmodifiedSince A timestamp indicating the last modified time of the resource.
     */
    public void setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
    }

    /**
     * Gets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @return The maximum number of items to return in the response.
     */
    public Integer getMaxresults() {
        return maxresults;
    }

    /**
     * Sets the maximum number of items to return in the response. A maximum of 1000 applications can be returned.
     *
     * @param maxresults The maximum number of items to return in the response.
     */
    public void setMaxresults(Integer maxresults) {
        this.maxresults = maxresults;
    }

    /**
     * Gets the options to use for disabling scheduling on the Compute Node.
     *
     * @return The options for disabling scheduling on the Compute Node.
     */
    public NodeDisableSchedulingOptions getNodeDisableSchedulingOptionsBody() {
        return nodeDisableSchedulingOptionsBody;
    }

    /**
     * Sets the options to use for disabling scheduling on the Compute Node.
     *
     * @param nodeDisableSchedulingOptionsBody The options for disabling scheduling on the Compute Node.
     */
    public void setNodeDisableSchedulingOptionsBody(NodeDisableSchedulingOptions nodeDisableSchedulingOptionsBody) {
        this.nodeDisableSchedulingOptionsBody = nodeDisableSchedulingOptionsBody;
    }

    /**
     * Gets the options to use for rebooting the Compute Node.
     *
     * @return The options for rebooting the Compute Node.
     */
    public NodeRebootOptions getNodeRebootOptionsBody() {
        return nodeRebootOptionsBody;
    }

    /**
     * Sets the options to use for rebooting the Compute Node.
     *
     * @param nodeRebootOptionsBody The options for rebooting the Compute Node.
     */
    public void setNodeRebootOptionsBody(NodeRebootOptions nodeRebootOptionsBody) {
        this.nodeRebootOptionsBody = nodeRebootOptionsBody;
    }

    /**
     * Gets the options to use for reimaging the Compute Node.
     *
     * @return The options for reimaging the Compute Node.
     */
    public NodeReimageOptions getNodeReimageOptionsBody() {
        return nodeReimageOptionsBody;
    }

    /**
     * Sets the options to use for reimaging the Compute Node.
     *
     * @param nodeReimageOptionsBody The options for reimaging the Compute Node.
     */
    public void setNodeReimageOptionsBody(NodeReimageOptions nodeReimageOptionsBody) {
        this.nodeReimageOptionsBody = nodeReimageOptionsBody;
    }

    /**
     * Gets the byte range to be retrieved. The default is to retrieve the entire file. The format is bytes=startRange-endRange.
     *
     * @return The byte range to be retrieved.
     */
    public String getOcpRange() {
        return ocpRange;
    }

    /**
     * Sets the byte range to be retrieved. The default is to retrieve the entire file. The format is bytes=startRange-endRange.
     *
     * @param ocpRange The byte range to be retrieved.
     */
    public void setOcpRange(String ocpRange) {
        this.ocpRange = ocpRange;
    }

    /**
     * Gets a value indicating whether to perform a recursive operation.
     *
     * When used for different operations, this property controls different behaviors:
     * For deletion, if set to true, it will delete the directory and all of its files and subdirectories.
     * If set to false, the directory must be empty for deletion to succeed.
     * For listing children, if set to true, it includes children of a directory.
     * This parameter can be used with the filter parameter to list specific types of files.
     *
     * @return A value indicating whether the operation should be performed recursively.
     */
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Sets whether to perform a recursive operation.
     *
     * When used for different operations, this property controls different behaviors:
     * For deletion, if set to true, it will delete the directory and all of its files and subdirectories.
     * If set to false, the directory must be empty for deletion to succeed.
     * For listing children, if set to true, it includes children of a directory.
     * This parameter can be used with the filter parameter to list specific types of files.
     *
     * @param recursive A value indicating whether the operation should be performed recursively.
     */
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Gets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @return The HTTP options for conditional requests.
     */
    public RequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @param requestConditions The HTTP options for conditional requests.
     */
    public void setRequestConditions(RequestConditions requestConditions) {
        this.requestConditions = requestConditions;
    }

    /**
     * Gets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @return The OData $select clause.
     */
    public List<String> getSelect() {
        return select;
    }

    /**
     * Sets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @param select The OData $select clause.
     */
    public void setSelect(List<String> select) {
        this.select = select;
    }

    /**
     * Gets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @return The earliest time from which to include metrics.
     */
    public OffsetDateTime getStartTime() {
        return starttime;
    }

    /**
     * Sets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @param starttime The earliest time from which to include metrics.
     */
    public void setStartTime(OffsetDateTime starttime) {
        this.starttime = starttime;
    }

    /**
     * Gets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @return The maximum time that the server can spend processing the request, in seconds.
     */
    public Integer getTimeOutInSeconds() {
        return timeOutInSeconds;
    }

    /**
     * Sets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @param timeOutInSeconds The maximum time that the server can spend processing the request, in seconds.
     */
    public void setTimeOutInSeconds(Integer timeOutInSeconds) {
        this.timeOutInSeconds = timeOutInSeconds;
    }

}
