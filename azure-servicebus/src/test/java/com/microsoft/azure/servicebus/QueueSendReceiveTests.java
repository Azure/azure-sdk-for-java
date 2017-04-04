package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class QueueSendReceiveTests extends SendReceiveTests
{
//	private int counter = 0; 
	@Override
	public ConnectionStringBuilder getSenderConnectionStringBuilder() {
		return TestUtils.getQueueConnectionStringBuilder();
	}

	@Override
	public ConnectionStringBuilder getReceiverConnectionStringBuilder() {
		return TestUtils.getQueueConnectionStringBuilder();
	}
	
//	@org.junit.Test
//	public void testConcurrentSend() throws InterruptedException, ServiceBusException, ExecutionException
//	{		
//		CompletableFuture<Void> send1 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - First Current thread : " + Thread.currentThread().toString());
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
//		CompletableFuture<Void> send2 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});		
//		
//		CompletableFuture<Void> send6 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		CompletableFuture<Void> send3 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		CompletableFuture<Void> send4 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());
//			
//		});
//		CompletableFuture<Void> send5 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		
//		CompletableFuture<Void> send7 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		CompletableFuture<Void> send8 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		CompletableFuture<Void> send9 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		CompletableFuture<Void> send10 = this.sender.sendAsync(new BrokeredMessage("AMQPMessage")).thenRun(() -> {
//			System.out.println(Instant.now() + " - Second Current thread : " + Thread.currentThread().toString());			
//		});
//		
//		CompletableFuture.allOf(send1, send2, send3, send4, send5, send6, send7, send8, send9, send10).get();
//	}
	
//	@org.junit.Test
//	public void testAsyncRecursion() throws InterruptedException, ExecutionException
//	{
//		printThreadRecursively().get();
//	}
//	
//	private CompletableFuture<Void> printThreadRecursively()
//	{
//		System.out.println(Instant.now() + " - Current thread : " + Thread.currentThread().toString() + "- Counter:" + (++counter));
//		return CompletableFuture.completedFuture(null).thenComposeAsync((v) -> this.printThreadRecursively());
//	}
}
