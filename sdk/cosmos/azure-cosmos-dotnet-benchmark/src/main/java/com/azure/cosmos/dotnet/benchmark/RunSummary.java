// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

class RunSummary {

    @JsonProperty("AccountName")
    private String accountName;
    @JsonProperty("AverageRps")
    private double averageRps;
    @JsonProperty("BranchName")
    private String branchName;
    @JsonProperty("Commit")
    private String commit;
    @JsonProperty("CommitDate")
    private String commitDate;
    @JsonProperty("CommitTime")
    private String commitTime;
    @JsonProperty("Concurrency")
    private int concurrency;
    @JsonProperty("ConsistencyLevel")
    private String consistencyLevel;
    @JsonProperty("Container")
    private String container;
    @JsonProperty("Cores")
    private int cores;
    @JsonProperty("Database")
    private String database;
    @JsonProperty("Date")
    private String date;
    @JsonProperty("id")
    private String id;
    @JsonProperty("MachineName")
    private String machineName;
    @JsonProperty("OS")
    private String os;
    @JsonProperty("OSVersion")
    private String osVersion;
    @JsonProperty("pk")
    private String pk;
    @JsonProperty("Remarks")
    private String remarks;
    @JsonProperty("RuntimeVersion")
    private String runtimeVersion;
    @JsonProperty("Time")
    private String time;
    @JsonProperty("Top10PercentAverageRps")
    private double top10PercentAverageRps;
    @JsonProperty("Top20PercentAverageRps")
    private double top20PercentAverageRps;
    @JsonProperty("Top30PercentAverageRps")
    private double top30PercentAverageRps;
    @JsonProperty("Top40PercentAverageRps")
    private double top40PercentAverageRps;
    @JsonProperty("Top50PercentAverageRps")
    private double top50PercentAverageRps;
    @JsonProperty("Top50PercentLatencyInMs")
    private Double top50PercentLatencyInMs;
    @JsonProperty("Top60PercentAverageRps")
    private double top60PercentAverageRps;

    @JsonProperty("Top70PercentAverageRps")
    private double top70PercentAverageRps;
    @JsonProperty("Top75PercentLatencyInMs")
    private Double top75PercentLatencyInMs;
    @JsonProperty("Top80PercentAverageRps")
    private double top80PercentAverageRps;
    @JsonProperty("Top90PercentAverageRps")
    private double top90PercentAverageRps;
    @JsonProperty("Top90PercentLatencyInMs")
    private Double top90PercentLatencyInMs;
    @JsonProperty("Top95PercentAverageRps")
    private double top95PercentAverageRps;
    @JsonProperty("Top95PercentLatencyInMs")
    private Double top95PercentLatencyInMs;
    @JsonProperty("Top99PercentLatencyInMs")
    private Double top99PercentLatencyInMs;
    @JsonProperty("TotalOps")
    private int totalOps;
    @JsonProperty("WorkloadType")
    private String workloadType;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getAverageRps() {
        return averageRps;
    }

    public void setAverageRps(double averageRps) {
        this.averageRps = averageRps;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTop10PercentAverageRps() {
        return top10PercentAverageRps;
    }

    public void setTop10PercentAverageRps(double top10PercentAverageRps) {
        this.top10PercentAverageRps = top10PercentAverageRps;
    }

    public double getTop20PercentAverageRps() {
        return top20PercentAverageRps;
    }

    public void setTop20PercentAverageRps(double top20PercentAverageRps) {
        this.top20PercentAverageRps = top20PercentAverageRps;
    }

    public double getTop30PercentAverageRps() {
        return top30PercentAverageRps;
    }

    public void setTop30PercentAverageRps(double top30PercentAverageRps) {
        this.top30PercentAverageRps = top30PercentAverageRps;
    }

    public double getTop40PercentAverageRps() {
        return top40PercentAverageRps;
    }

    public void setTop40PercentAverageRps(double top40PercentAverageRps) {
        this.top40PercentAverageRps = top40PercentAverageRps;
    }

    public double getTop50PercentAverageRps() {
        return top50PercentAverageRps;
    }

    public void setTop50PercentAverageRps(double top50PercentAverageRps) {
        this.top50PercentAverageRps = top50PercentAverageRps;
    }

    public Double getTop50PercentLatencyInMs() {
        return top50PercentLatencyInMs;
    }

    public void setTop50PercentLatencyInMs(Double top50PercentLatencyInMs) {
        this.top50PercentLatencyInMs = top50PercentLatencyInMs;
    }

    public double getTop60PercentAverageRps() {
        return top60PercentAverageRps;
    }

    public void setTop60PercentAverageRps(double top60PercentAverageRps) {
        this.top60PercentAverageRps = top60PercentAverageRps;
    }

    public double getTop70PercentAverageRps() {
        return top70PercentAverageRps;
    }

    public void setTop70PercentAverageRps(double top70PercentAverageRps) {
        this.top70PercentAverageRps = top70PercentAverageRps;
    }

    public Double getTop75PercentLatencyInMs() {
        return top75PercentLatencyInMs;
    }

    public void setTop75PercentLatencyInMs(Double top75PercentLatencyInMs) {
        this.top75PercentLatencyInMs = top75PercentLatencyInMs;
    }

    public double getTop80PercentAverageRps() {
        return top80PercentAverageRps;
    }

    public void setTop80PercentAverageRps(double top80PercentAverageRps) {
        this.top80PercentAverageRps = top80PercentAverageRps;
    }

    public double getTop90PercentAverageRps() {
        return top90PercentAverageRps;
    }

    public void setTop90PercentAverageRps(double top90PercentAverageRps) {
        this.top90PercentAverageRps = top90PercentAverageRps;
    }

    public Double getTop90PercentLatencyInMs() {
        return top90PercentLatencyInMs;
    }

    public void setTop90PercentLatencyInMs(Double top90PercentLatencyInMs) {
        this.top90PercentLatencyInMs = top90PercentLatencyInMs;
    }

    public double getTop95PercentAverageRps() {
        return top95PercentAverageRps;
    }

    public void setTop95PercentAverageRps(double top95PercentAverageRps) {
        this.top95PercentAverageRps = top95PercentAverageRps;
    }

    public Double getTop95PercentLatencyInMs() {
        return top95PercentLatencyInMs;
    }

    public void setTop95PercentLatencyInMs(Double top95PercentLatencyInMs) {
        this.top95PercentLatencyInMs = top95PercentLatencyInMs;
    }

    public Double getTop99PercentLatencyInMs() {
        return top99PercentLatencyInMs;
    }

    public void setTop99PercentLatencyInMs(Double top99PercentLatencyInMs) {
        this.top99PercentLatencyInMs = top99PercentLatencyInMs;
    }

    public int getTotalOps() {
        return totalOps;
    }

    public void setTotalOps(int totalOps) {
        this.totalOps = totalOps;
    }

    public String getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(String workloadType) {
        this.workloadType = workloadType;
    }
}
