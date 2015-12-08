Contribute to EventHubs Java SDK
================================

Developer Environment
---------------------






Design
------

Goals:
-----
1- Common layer to have EventBased - Recv



Arch:
----
					--------------			-------------
					|	EventHubs|			|  Q/T		|
					-------------------------------------
									|			
					---------------------------------
					|			SB library 			|
					---------------------------------				
									|
					---------------------------------
					|	Amqp library abstraction	|
					---------------------------------
									|
					----------------
					|	ProtonJ	|		. .  . .    ...   ...
					-------------

Reactor-SBLibrary interaction:
-----------------------------




Exceptions:
----------
amqp:unauthorized-access			UnauthorizedAccessException
amqp:not-allowed					InvalidOperationException
amqp:not-implemented				NotImplementedException
amqp:not-found						EntityNotFoundException
amqp:resource-limit-exceeded		QuotaExceededException
amqp:internal-error					MessagingException
