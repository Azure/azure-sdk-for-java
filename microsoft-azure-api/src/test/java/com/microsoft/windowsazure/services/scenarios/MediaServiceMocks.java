/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.scenarios;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;

// TODO: Remove when no longer needed.
// Tracked by https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/457
class MediaServiceMocks {

    static class MockMediaContract {
        public List<AssetFileInfo> getAssetFiles(String id) {
            return new ArrayList<AssetFileInfo>();
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

        public List<AssetFileInfo> getFiles() {
            return new ArrayList<AssetFileInfo>();
        }

        public AssetFileInfo createFileInfo(AssetFileInfo fi) {
            return null;
        }
    }

    static enum JobState {
        Finished, Canceled, Error
    }

    static class JobInfo {

        public String getId() {
            return null;
        }

        public JobState getState() {
            return JobState.Finished;
        }
    }

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

    static enum TaskCreationOptions {
        ProtectedConfiguration
    }
}
