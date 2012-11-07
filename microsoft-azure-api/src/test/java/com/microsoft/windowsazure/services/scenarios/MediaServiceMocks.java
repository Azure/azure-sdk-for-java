package com.microsoft.windowsazure.services.scenarios;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.FileInfo;

class MediaServiceMocks {

    // TODO: Replace with real media contract
    static class MockMediaContract {
        public List<FileInfo> getAssetFiles(String id) {
            return new ArrayList<FileInfo>();
        }

        public JobInfo createJob(CreateJobOptions jobOptions) {
            return new JobInfo();
        }

        public JobInfo getJob(String id) {
            return new JobInfo();
        }

        public List<AssetInfo> getJobOutputMediaAssets(String id) {
            return new ArrayList<AssetInfo>();
        }

        public void cancelJob(String id) {
        }

        public List<FileInfo> getFiles() {
            return new ArrayList<FileInfo>();
        }
    }

    // TODO: Replace with real JobState
    static enum JobState {
        Finished, Canceled, Error
    }

    // TODO: Replace with real JobInfo
    static class JobInfo {

        public String getId() {
            return null;
        }

        public JobState getState() {
            return JobState.Finished;
        }
    }

    // TODO: Replace with real CreateTaskOptions
    static class CreateTaskOptions {

        public CreateTaskOptions setName(String string) {
            return this;
        }

        public CreateTaskOptions setProcessorId(String id) {
            return this;
        }

        public CreateTaskOptions setConfiguration(String string) {
            return this;
        }

        public CreateTaskOptions setTaskBody(String string) {
            return this;
        }

        public CreateTaskOptions setTaskCreationOptions(TaskCreationOptions options) {
            return this;
        }
    }

    // TODO: Replace with real CreateJobOptions
    static class CreateJobOptions {

        public CreateJobOptions setName(String jobName) {
            return this;
        }

        public CreateJobOptions addInputMediaAsset(String id) {
            return this;
        }

        public CreateJobOptions addTask(CreateTaskOptions task) {
            return this;
        }
    }

    // TODO: Replace with real TaskCreationOptions
    static enum TaskCreationOptions {
        ProtectedConfiguration
    }

}
