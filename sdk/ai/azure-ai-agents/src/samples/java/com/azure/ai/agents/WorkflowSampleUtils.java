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
            + invokeAction("student_agent", "The student node", studentName, "Local.StudentConversationId")
            + invokeAction("teacher_agent", "The teacher node", teacherName, "Local.TeacherConversationId")
            + "    - kind: SendActivity\n"
            + "      id: send_teacher_reply\n"
            + "      activity: '{{Last(Local.LatestMessage).Text}}'\n"
            + "    - kind: SetVariable\n"
            + "      id: set_turn_count\n"
            + "      variable: Local.TurnCount\n"
            + "      value: '=Local.TurnCount + 1'\n"
            + "    - kind: ConditionGroup\n"
            + "      id: completion_check\n"
            + "      conditions:\n"
            + "        - condition: '=!IsBlank(Find(\"[COMPLETE]\", Upper(Last(Local.LatestMessage).Text)))'\n"
            + "          id: check_done\n"
            + "          actions:\n"
            + "            - kind: EndConversation\n"
            + "              id: end_workflow\n"
            + "        - condition: '=Local.TurnCount >= 4'\n"
            + "          id: check_turn_count_exceeded\n"
            + "          actions:\n"
            + "            - kind: SendActivity\n"
            + "              id: send_tired_reply\n"
            + "              activity: 'Let us try again later.'\n"
            + "      elseActions:\n"
            + "        - kind: GotoAction\n"
            + "          id: goto_student_agent\n"
            + "          actionId: student_agent\n";
    }

    private static String invokeAction(String id, String description, String agentName, String conversationId) {
        return "    - kind: InvokeAzureAgent\n"
            + "      id: " + id + "\n"
            + "      description: " + description + "\n"
            + "      conversationId: '=" + conversationId + "'\n"
            + "      agent:\n"
            + "        name: " + agentName + "\n"
            + "      input:\n"
            + "        messages: '=Local.LatestMessage'\n"
            + "      output:\n"
            + "        messages: Local.LatestMessage\n";
    }
}
