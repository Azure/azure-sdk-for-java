
We're now using GitHub for all our development activities; it is important for everyone to use a consistent process. Hence we have this guideline. 

> [!NOTE]
> 1. Although this doc uses the term 'issue', this guideline applies to both GitHub issues and Pull Requests. 
> 2. We are an open-source project, GitHub issues and PRs are not your personal notebook, it is open to the public, and we use these tool to communicate and collaborate. 

## GitHub Issue Management 

High-quality issues are at the core of helping a project to succeed. While some may see issues as merely a big list of problems you have to tend to, well-managed, triaged, and labeled issues can provide incredible insight into your code, your community, and where the problem spots are.    

### What is an issue

* Issue queues should mirror your thought process. Issues are where you structure your ideas with others or alone. 
* Issues should focus on ideas, problems, and solutions; they are not activity trackers. While you want to share your ideas as they unfold, you may not want to comment on every little activity you register on an issue; for this, IM, commit messages, or a Scrum meeting may be better suited. 
* The issue queue is where real collaboration is done. Other forms of communication (verbal discussions, email, IM…) should happen after exploring an issue. This ensured that everyone involved had a chance to make and review the arguments cleanly, structured, and transparently. The more real-time the discussion, the more likely it is to be charged with emotions.   

### How to write issues (and PRs and everything else) 

**Avoid duplication** 
 
The first thing you should do when writing a GitHub issue is to use the search feature to make sure an issue doesn’t already exist. 

When duplicate issues are found, one of them should be closed — typically the less thorough one. Be sure the closing comment contains a link to the issue remaining open (e.g. “Duplicate of #123”), and that any helpful details from the closed issue are consolidated into the open one. 

**Titles are important** 

Keep your titles short and descriptive; please note that the title is your issue's most visible part. 

When someone reads your title, it should provide enough information about this issue without opening the issue details page. 
 
You should NEVER just drop two or three words as an issue title so that nobody understands what this issue is for. 

When requesting features and changes, clearly describe the problem to be solved instead of prescribing a solution. 

   * Bad — "Make the button red." 
   * Good — "The user doesn’t understand that deletion is irreversible." 

Sometimes we add prefixes to the issue. In general, we should only use these prefixes in the issue title. 

   * [BUG] 
   * [FEATURE REQ] 
   * [QUESTION] 
   * [QUERY] 
   * [Epic] 

**Some bad examples**: 

* Delete set_identifier_uris.sh  

   * https://github.com/Azure/azure-sdk-for-java/issues/27766 
   * This is just too short, and no one would understand what this is for 
   * Delete unneeded set_identifier_uris.sh from azure-spring-samples project" would better 

* Spring App Configuration 

   * https://github.com/Azure/azure-sdk-for-java/issues/21772 
   * Same as the first one; it just doesn't provide much information. 
   * [Epic] Improve Spring App Configuration 

* [Enhance Spring integration for storage]Support AppendBlobResource and PageableBlobResource in azure-spring-cloud-storage  

   * https://github.com/Azure/azure-sdk-for-java/issues/21461 

   * Non-standard prefix and seems not much useful 

   * [FEATURE REQ]Support Spring's AppendBlobResource and PageableBlobResource API in azure-spring-cloud-storage  

* resource-server (Write sample about AAD B2C + spring-security + web-flux) 

   * https://github.com/Azure/azure-sdk-for-java/issues/26333 

   * This is not a valid sentence, and I can't tell what this issue wants to do. 

* [QUERY] should we consider fix the vulnerability hinted by the spot bug plugin in IDEA 

   * https://github.com/Azure/azure-sdk-for-java/issues/27194 
   * This is a misunderstanding about the issue type. This should be more of a feature request than a query. 

* [FEATURE REQ] Azure Messaging Listener Support Reference Doc 

   * https://github.com/Azure/azure-sdk-for-java/issues/26999 
   * Can't understand what this issue wants 

**Issue Description**

First and foremost, you should NEVER create an issue without a description of this issue or throw two or three words no one would understand, like this one https://github.com/Azure/azure-sdk-for-java/issues/26333. Just gave an 'as subject'. 

* The issue's description should stick to the following structure: 

   Context: explain the conditions which led you to write this issue. 

   For example: “Since we’ve moved to the latest version of Express.js, we’ve experienced a few performance issues (#14 and #15) on production.” 

* Problem or idea: the context should lead to something, an idea, or a problem you’re facing. 

   For example: “We’ve had no way of easily seeing the performance impact before releasing our changes to production.” 

  

* Solution or next step: this is where you move forward. You can engage others (request feedback), assign somebody else to the issue, or simply leave it for further investigation. Still, you absolutely need to propose a next step towards solving the issue. 

**Formatting**

A well-formatted issue is easier to read, which helps the reader to focus and understand the content. This is another way to improve the quality of our issues. 

Markdown (https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax) is very simple to learn. 

 

* Links — Include links to the specific application views you’re describing. 
* Screenshots — A picture is worth 1,000 words (a GIF is worth 1,000,000). A screenshot can help clarify the issue you’re seeing. 
* @me — If you know certain people should be alerted, be sure to mention them in the issue. 

### Issue Label
We use the label in many places, and ensuring the issue/pull request has the right labels is critical for our daily project management. 

We should make sure: 

1. Issues / PRs created by ourselves have the correct label 

1. Correct issues during our daily issue triage  

In the meanwhile, we should investigate how to leverage GitHub actions to automate this work `TODO add issue link`.

For every issue / PR, it should contain these kinds of labels: `{mandatory labels}` + `{issue type label}` + `{area labels}` 

* **Mandatory** – All Spring related issues / PRs MUST have ALL of the following labels 

   * Client 
   * Azure-spring 

* **Issue Type Labels** – All Spring-related issues / PRs MUST have ONE of the following labels since we only have three types of issues. 

   * Feature-request 
   * Bug 
   * Question 

* **Area Labels** – Any Spring-related issues / PRs may belong to an area, for example, docs or samples related, or an EventHub bug, etc., then it should be labeled with the area label. Please note this is a FIXED list. Please DO NOT create new ones without talking to your lead first. 

   * Azure-spring-bi    TODO – we should probably avoid making BI-related public, using internal ADO to track BI issues. 
   * Azure-spring-docs 
   * Azure-spring-sample 
   * Azure-spring-aad 
   * Azure-spring-eventhubs 
   * Azure-spring-servicebus 
   * Azure-spring-storage 

### GitHub Project

When you create a GitHub issue or do issue triage, ensure the issue is added to the Spring Cloud Azure GitHub project(https://github.com/orgs/Azure/projects/94/views/1). 

We may get this done automatically in the future, but before we get there, please make sure that manually.

### GitHub Issue Milestone 

In general, all newly created issues should be put into the backlog. 

We don’t want to have new issues interrupt our sprint planning. 

If there is something urgent request happening, talk to your lead or scrum master first. 

### GitHub Issue Priority 

If you don't know the priority, just leave it blank, do not modify this without talk to your lead or scrum first. 

To keep things simple, we use [0-3] to prioritize our issues. 

   * 0 – Means this issue MUST be resolved within the current sprint 
   * 1 – Means this issue SHOULD be resolved within the current sprint, or it will be promoted to P-0 in the next sprint 
   * 2 – Means this issue SHOULD be resolved within the current semester  
   * 3 – This means this issue will NOT be considered in the current semester  

An issue with an empty priority field means one is not triaged yet.

### GitHub Issue Workflow 

Again, we use GitHub issues for communication and collaboration, not only inside the team but outside as well. 

We expect you to update the progress and information on the GitHub issue you are working on. 

For example, if you're working on an investigation issue like "Investigate how to support OpenTelemetry in Azure Spring Cloud" 

**You should NEVER** : 

1. Close the issue after a couple of days or weeks without anything 
2. Close the issue after a couple of days or weeks with a link to your findings. 

**What we expect**:  

When you start to work on this issue, keep updating it with your plan/thoughts/findings/references, then provide an investigation with a conclusion about this issue and close it. 
      