// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

final class WorkflowSampleUtils {
    private WorkflowSampleUtils() {
    }

    static String createStudentTeacherWorkflow(String studentName, String teacherName) {
        return "kind: workflow\n"
            + "trigger:\n"
            + "  kind: OnConversationStart\n"
            + "  id: student_teacher_workflow\n"
            + "  actions:\n"
            + "    - kind: SetVariable\n"
            + "      id: set_input\n"
            + "      variable: Local.LatestMessage\n"
            + "      value: '=UserMessage(System.LastMessageText)'\n"
            + "    - kind: CreateConversation\n"
            + "      id: create_student_conversation\n"
            + "      conversationId: Local.StudentConversationId\n"
            + "    - kind: CreateConversation\n"
            + "      id: create_teacher_conversation\n"
            + "      conversationId: Local.TeacherConversationId\n"
            + invokeAction("student", studentName, "Local.StudentConversationId")
            + invokeAction("teacher", teacherName, "Local.TeacherConversationId")
            + "    - kind: SendActivity\n"
            + "      id: send_teacher_reply\n"
            + "      activity: '{{Last(Local.LatestMessage).Text}}'\n"
            + "    - kind: EndConversation\n"
            + "      id: end_workflow\n";
    }

    private static String invokeAction(String id, String agentName, String conversationId) {
        return "    - kind: InvokeAzureAgent\n"
            + "      id: " + id + "\n"
            + "      conversationId: '=" + conversationId + "'\n"
            + "      agent:\n"
            + "        name: " + agentName + "\n"
            + "      input:\n"
            + "        messages: '=Local.LatestMessage'\n"
            + "      output:\n"
            + "        messages: Local.LatestMessage\n";
    }
}
