package com.microsoft.windowsazure.services.media.implementation;

import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

public class CreateTaskOperation implements Operation {

    private TaskType taskType;

    public CreateTaskOperation setTask(TaskType taskType) {
        this.taskType = taskType;
        return this;
    }

    public TaskType getTask() {
        return this.taskType;
    }

}
