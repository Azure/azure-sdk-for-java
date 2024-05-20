// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>This package contains utility classes and interfaces for handling long-running operations in the
 * Azure client libraries.</p>
 *
 * <p>Long-running operations are operations such as the creation or deletion of a resource, which take a significant
 * amount of time to complete. These operations are typically handled asynchronously, with the client initiating the
 * operation and then polling the service at intervals to determine whether the operation has completed.</p>
 *
 * <p>This package provides a standard mechanism for initiating, tracking, and retrieving the results of long-running
 * operations</p>
 *
 * <p><strong>Code Sample: Asynchronously wait for polling to complete and then retrieve the final result</strong></p>
 *
 * <!-- src_embed com.azure.core.util.polling.poller.getResult -->
 * <pre>
 * LocalDateTime timeToReturnFinalResponse = LocalDateTime.now&#40;&#41;.plus&#40;Duration.ofMinutes&#40;5&#41;&#41;;
 *
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;String, String&gt; poller = new PollerFlux&lt;&gt;&#40;Duration.ofMillis&#40;100&#41;,
 *     &#40;context&#41; -&gt; Mono.empty&#40;&#41;,
 *     &#40;context&#41; -&gt;  &#123;
 *         if &#40;LocalDateTime.now&#40;&#41;.isBefore&#40;timeToReturnFinalResponse&#41;&#41; &#123;
 *             System.out.println&#40;&quot;Returning intermediate response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.IN_PROGRESS,
 *                     &quot;Operation in progress.&quot;&#41;&#41;;
 *         &#125; else &#123;
 *             System.out.println&#40;&quot;Returning final response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
 *                     &quot;Operation completed.&quot;&#41;&#41;;
 *         &#125;
 *     &#125;,
 *     &#40;activationResponse, context&#41; -&gt; Mono.just&#40;&quot;FromServer:OperationIsCancelled&quot;&#41;,
 *     &#40;context&#41; -&gt; Mono.just&#40;&quot;FromServer:FinalOutput&quot;&#41;&#41;;
 *
 * poller.take&#40;Duration.ofMinutes&#40;30&#41;&#41;
 *         .last&#40;&#41;
 *         .flatMap&#40;asyncPollResponse -&gt; &#123;
 *             if &#40;asyncPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *                 &#47;&#47; operation completed successfully, retrieving final result.
 *                 return asyncPollResponse
 *                         .getFinalResult&#40;&#41;;
 *             &#125; else &#123;
 *                 return Mono.error&#40;new RuntimeException&#40;&quot;polling completed unsuccessfully with status:&quot;
 *                         + asyncPollResponse.getStatus&#40;&#41;&#41;&#41;;
 *             &#125;
 *         &#125;&#41;.block&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.getResult -->
 *
 * <p><strong>Code Sample: Using a SimpleSyncPoller to poll until the operation is successfully completed</strong></p>
 *
 * <!-- src_embed com.azure.core.util.polling.simpleSyncPoller.instantiationAndPoll -->
 * <pre>
 * LongRunningOperationStatus operationStatus = syncPoller.poll&#40;&#41;.getStatus&#40;&#41;;
 * while &#40;operationStatus != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     System.out.println&#40;&quot;Polling status: &quot; + operationStatus.toString&#40;&#41;&#41;;
 *     System.out.println&#40;&quot;Polling response: &quot; + operationStatus.toString&#40;&#41;&#41;;
 *     operationStatus = syncPoller.poll&#40;&#41;.getStatus&#40;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.polling.simpleSyncPoller.instantiationAndPoll -->
 *
 *
 * @see com.azure.core.util.polling.PollerFlux
 * @see com.azure.core.util.polling.SimpleSyncPoller
 */
package com.azure.core.util.polling;
