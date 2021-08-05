## Cross-Entity Transaction for SerivceBus SDKs 

Currently, the "send via" mechanism offered by ServiceBus SDKs is offered as a work around to achieve cross-entity transactions. The objective of this changeset is to deprecate this "send via" API in all ServiceBus SDKs, where the user can create a transfer sender by supplying the path of the "send via" entity. However, the functionalities of achieving cross-entity transactions will be maintained by implicitly inferring the send via entity from the server side. This is how the new model would work: 
  
 
1. Declaring the intent of a transaction: 
    1. The user must instantiate the transaction object before creating any sender or receiver they intend to use for transactional send via. 
    2. The user must instantiate a transactional sender/receiver (introduced by this new model) to perform any transactional actions (send, complete, deadletter, etc.) that they intend to perform later, instead of a regular sender/receiver. 
  
2. Transactional Sender/Receiver: 
    1. A transactional sender/receiver must take a transaction object as a parameter for its constructor, and this transaction object should be readonly or effectively readonly. 
    2. All actions performed from the transactional sender/receiver will be associated with the transactionId of the transaction. Non-transactional actions should not be performed with transactional sender/receiver. 
    3. The existing "send via" entity path parameter should not be set by the user anymore. This value will be inferred by the service side instead. Details will be shown below. 
  
3. Deciding the "via" sender: 
    1. When the first transactional sender/receiver is created with an associated transaction, the target entity of this sender/receiver will be chosen by ServiceBus server side as the " via" entity implicitly. 
    2. The "via" entity must be a top level entity (queue or topic). If the target entity is a subscription, then the corresponding topic will be chosen to be the "send via" entity. 
    3. All subsequent transactional senders under the same transaction will "send via" the "via" entity chosen above. 
  
4. Constraints of this new model are same as existing model except c.: 
    1. Either transactional sender or transactional receiver can be the first link to be created with a specific transaction and have its target entity chosen as the "via" entity for that transaction. All subsequent links created with that transaction must be senders (otherwise we are doing "receive via"), this is the same as today. Violations will result in exceptions thrown from server. 
    2. As an exception to rule a. above, transactional receivers on the same transaction are still allowed after the "via" entity is established as long as its top level entity is the same as the "via" entity. This scenario is acceptable because it will no longer be a cross-entity transaction. 
    3. All transactional actions performed using the transactional senders/receivers described above will be transactional with the same transactionId and sending via the "via" entity whenever applicable. There is no option to turn this off. 

5. AMQP implementation changes to support this new model: 
    1. The current AMQP model has 1 to 1 mapping between all AMQP links and sessions, and each sender/receiver/transaction will allocate its individual AMQP session+link upon creation. The proposed model will have each new transaction create their own AMQP session and coordinator link, and all transactional senders/receivers associated with that transaction will only allocate their AMQP links, but share the same AMQP session with the associated transaction. (Please see diagrams below). 
  
  
5. ViaPartitionKey: 
    1. ViaPartitionKey will remain the same as now with no changes at all. It is still required by partitioned entities during a transaction. 
  
  
 <img width="899" alt="Screen Shot 2021-08-05 at 10 21 22 AM" src="https://user-images.githubusercontent.com/1471612/128393795-68af7fd2-ec5a-4cf7-9587-3d08e1e31987.png">