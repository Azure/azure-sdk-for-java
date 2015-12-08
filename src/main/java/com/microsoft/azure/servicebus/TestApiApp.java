package com.microsoft.azure.servicebus;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;
import org.apache.qpid.proton.reactor.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestApiApp 
{
    public static void main	(String[] args) throws Exception
    {
    	/*FileHandler fh = new FileHandler("c:\\amqpframes.log", false);
    	Logger l = Logger.getLogger("");
    	fh.setFormatter(new SimpleFormatter());
    	l.addHandler(fh);
    	l.setLevel(Level.ALL);
    	
    	String username = "RootManageSharedAccessKey";
		String password = "LHbmplGdVC7Lo7A1RAXXDgeHSM9WHIRvZmIt7m1y5w0=";
		String hostname = "firstehub-ns.servicebus.windows.net";
		*/
		String username = "sendrecv";
		String password = "lLR6KcSXQZQJbyMvZYRJFAZMCw+s9cQ2poSIqtY582s=";
		String hostname = "standard.servicebus.windows.net";
		
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(hostname, username, password);
		MessagingFactory factory = MessagingFactory.createFromConnectionString(connStr.toString());
		MessageSender sender = MessageSender.Create(factory, "sender0", "testwithjava/partitions/0");
		
		MessageReceiver receiver = MessageReceiver.Create(
				factory,
				"receiver0",
				"testwithjava/ConsumerGroups/$default/Partitions/0",
				"13583534", //4579928", // offset
				1000);
		
		Message msg = Proton.message();
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("some", "test msg");
		
		Gson gson = new GsonBuilder().create();
		
		Person p = new Person();
		p.Address = "12334 345th Pl NE Redmond";
		p.Name = "Jon";
		p.Age = 43;
		p.Details = "rock";
		// System.out.println(gson.toJson(p));
		
		Binary bodyData = new Binary(gson.toJson(p).getBytes(Charset.defaultCharset()));
		// sender.Send(msg); // async send
		
		
		Collection<Message> msgs = receiver.receive().get();
		for(Message recvdMessage: msgs) {
			System.out.println(recvdMessage.getMessageAnnotations());
			// System.out.println(gson.fromJson(recvdMessage.getBody()));
		}
	}
    
    public static CompletableFuture<Void> RecvAll(MessagingFactory factory) throws Exception {
    	MessageReceiver receiver = MessageReceiver.Create(
				factory,
				"receiver0",
				"hackit/ConsumerGroups/$default/Partitions/0",
				"-1", //4579928", // offset
				1000);
		
		
		MessageReceiver receiver1 = MessageReceiver.Create(
				factory,
				"receiver1",
				"hackit/ConsumerGroups/$default/Partitions/1",
				"-1", //4579928", // offset
				1000);
		
		MessageReceiver receiver2 = MessageReceiver.Create(
				factory,
				"receiver2",
				"hackit/ConsumerGroups/$default/Partitions/2",
				"-1", //4579928", // offset
				1000);
		
		MessageReceiver receiver3 = MessageReceiver.Create(
				factory,
				"receiver3",
				"hackit/ConsumerGroups/$default/Partitions/3",
				"-1", //4579928", // offset
				1000);
		
		while(true){
			
			CompletableFuture.allOf(
			
			receiver1.receive().thenAcceptAsync(new Consumer<Collection<Message>>() {
				// @Override
				public void accept(Collection<Message> msgs) {
					if(msgs != null && !msgs.isEmpty()) {
						LinkedList<Message> messages = new LinkedList<Message>();
						messages.addAll(msgs);
						System.out.println("Receiver1: " + messages.getLast().getMessageAnnotations());
						}
					}
				
			})
			/*,
			receiver.receive().thenAcceptAsync(new Consumer<Collection<Message>>() {
				@Override
				public void accept(Collection<Message> msgs) {
					if(msgs != null && !msgs.isEmpty()) {
						LinkedList<Message> messages = new LinkedList<Message>();
						messages.addAll(msgs);
						System.out.println("Receiver0: " + messages.getLast().getMessageAnnotations());
						}
					}
				
			}),
			
			receiver2.receive().thenAcceptAsync(new Consumer<Collection<Message>>() {
				@Override
				public void accept(Collection<Message> msgs) {
					if(msgs != null && !msgs.isEmpty()) {
						LinkedList<Message> messages = new LinkedList<Message>();
						messages.addAll(msgs);
						System.out.println("Receiver2: " + messages.getLast().getMessageAnnotations());
						}
					}
				
			}),
			
			receiver3.receive().thenAcceptAsync(new Consumer<Collection<Message>>() {
				@Override
				public void accept(Collection<Message> msgs) {
					if(msgs != null && !msgs.isEmpty()) {
						LinkedList<Message> messages = new LinkedList<Message>();
						messages.addAll(msgs);
						System.out.println("Receiver3: " + messages.getLast().getMessageAnnotations());
						}
					}
				})*/
			).get();
		}
	
    }
    
    public static CompletableFuture<Void> Send(MessagingFactory connection, int groupIndex) throws Exception {
    
    	MessageSender sender = MessageSender.Create(connection, "sender0", "hackit/partitions/0");
		MessageSender sender1 = MessageSender.Create(connection, "sender1", "hackit/partitions/1");
		MessageSender sender2 = MessageSender.Create(connection, "sender2", "hackit/partitions/2");
		MessageSender sender3 = MessageSender.Create(connection, "sender3", "hackit/partitions/3");
		
		return CompletableFuture.allOf(
			keepSending(groupIndex + 0, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 1, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 2, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 3, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 4, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 5, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 6, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 7, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 8, sender, sender1, sender2, sender3, 0),
			keepSending(groupIndex + 9, sender, sender1, sender2, sender3, 0)
			
			/*keepSending(10, sender, sender1, sender2, sender3, 0),
			keepSending(11, sender, sender1, sender2, sender3, 0),
			keepSending(12, sender, sender1, sender2, sender3, 0),
			keepSending(13, sender, sender1, sender2, sender3, 0),
			keepSending(14, sender, sender1, sender2, sender3, 0),
			keepSending(15, sender, sender1, sender2, sender3, 0),
			keepSending(16, sender, sender1, sender2, sender3, 0),
			keepSending(17, sender, sender1, sender2, sender3, 0),
			keepSending(18, sender, sender1, sender2, sender3, 0),
			keepSending(19, sender, sender1, sender2, sender3, 0),
			keepSending(20, sender, sender1, sender2, sender3, 0),
			keepSending(21, sender, sender1, sender2, sender3, 0),
			keepSending(22, sender, sender1, sender2, sender3, 0),
			keepSending(23, sender, sender1, sender2, sender3, 0),
			keepSending(24, sender, sender1, sender2, sender3, 0),
			keepSending(25, sender, sender1, sender2, sender3, 0),
			keepSending(26, sender, sender1, sender2, sender3, 0),
			keepSending(27, sender, sender1, sender2, sender3, 0),
			keepSending(28, sender, sender1, sender2, sender3, 0),
			keepSending(29, sender, sender1, sender2, sender3, 0),
			keepSending(30, sender, sender1, sender2, sender3, 0),
			keepSending(31, sender, sender1, sender2, sender3, 0),
			keepSending(32, sender, sender1, sender2, sender3, 0),
			keepSending(33, sender, sender1, sender2, sender3, 0),
			keepSending(34, sender, sender1, sender2, sender3, 0),
			keepSending(35, sender, sender1, sender2, sender3, 0),
			keepSending(36, sender, sender1, sender2, sender3, 0),
			keepSending(37, sender, sender1, sender2, sender3, 0),
			keepSending(38, sender, sender1, sender2, sender3, 0),
			keepSending(39, sender, sender1, sender2, sender3, 0),
			keepSending(40, sender, sender1, sender2, sender3, 0),
			keepSending(41, sender, sender1, sender2, sender3, 0),
			keepSending(42, sender, sender1, sender2, sender3, 0),
			keepSending(43, sender, sender1, sender2, sender3, 0),
			keepSending(44, sender, sender1, sender2, sender3, 0),
			keepSending(45, sender, sender1, sender2, sender3, 0),
			keepSending(46, sender, sender1, sender2, sender3, 0),
			keepSending(47, sender, sender1, sender2, sender3, 0),
			keepSending(48, sender, sender1, sender2, sender3, 0),
			keepSending(49, sender, sender1, sender2, sender3, 0),
			keepSending(50, sender, sender1, sender2, sender3, 0),
			keepSending(51, sender, sender1, sender2, sender3, 0),
			keepSending(52, sender, sender1, sender2, sender3, 0),
			keepSending(53, sender, sender1, sender2, sender3, 0),
			keepSending(54, sender, sender1, sender2, sender3, 0),
			keepSending(55, sender, sender1, sender2, sender3, 0),
			keepSending(56, sender, sender1, sender2, sender3, 0),
			keepSending(57, sender, sender1, sender2, sender3, 0),
			keepSending(58, sender, sender1, sender2, sender3, 0),
			keepSending(59, sender, sender1, sender2, sender3, 0),
			keepSending(60, sender, sender1, sender2, sender3, 0),
			keepSending(61, sender, sender1, sender2, sender3, 0),
			keepSending(62, sender, sender1, sender2, sender3, 0),
			keepSending(63, sender, sender1, sender2, sender3, 0),
			keepSending(64, sender, sender1, sender2, sender3, 0),
			keepSending(65, sender, sender1, sender2, sender3, 0),
			keepSending(66, sender, sender1, sender2, sender3, 0),
			keepSending(67, sender, sender1, sender2, sender3, 0),
			keepSending(68, sender, sender1, sender2, sender3, 0),
			keepSending(69, sender, sender1, sender2, sender3, 0),
			keepSending(70, sender, sender1, sender2, sender3, 0),
			keepSending(71, sender, sender1, sender2, sender3, 0),
			keepSending(72, sender, sender1, sender2, sender3, 0),
			keepSending(73, sender, sender1, sender2, sender3, 0),
			keepSending(74, sender, sender1, sender2, sender3, 0),
			keepSending(75, sender, sender1, sender2, sender3, 0),
			keepSending(76, sender, sender1, sender2, sender3, 0),
			keepSending(77, sender, sender1, sender2, sender3, 0),
			keepSending(78, sender, sender1, sender2, sender3, 0),
			keepSending(79, sender, sender1, sender2, sender3, 0),
			keepSending(80, sender, sender1, sender2, sender3, 0),
			keepSending(81, sender, sender1, sender2, sender3, 0),
			keepSending(82, sender, sender1, sender2, sender3, 0),
			keepSending(83, sender, sender1, sender2, sender3, 0),
			keepSending(84, sender, sender1, sender2, sender3, 0),
			keepSending(85, sender, sender1, sender2, sender3, 0),
			keepSending(86, sender, sender1, sender2, sender3, 0),
			keepSending(87, sender, sender1, sender2, sender3, 0),
			keepSending(88, sender, sender1, sender2, sender3, 0),
			keepSending(89, sender, sender1, sender2, sender3, 0)*/
			);
    }
    
    public static CompletableFuture<Void> keepSending(
    		final int index,
    		final MessageSender sender, 
    		final MessageSender sender1,
    		final MessageSender sender2,
    		final MessageSender sender3,
    		final long startingCount) throws InterruptedException, ExecutionException {
    	
    		System.out.println(String.format("sent msg %s on SendGroup %s", startingCount, index));
    		return CompletableFuture.allOf(sender.Send(BuildTestMessage()),
				sender1.Send(BuildTestMessage()),
				sender2.Send(BuildTestMessage()),
				sender3.Send(BuildTestMessage())).thenRunAsync(new Runnable() {
					
					public void run()  {
						long count = startingCount + 1;
						try {
							keepSending(index, sender, sender1, sender2, sender3, count);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
					
				);
			
    }
    
    public static Message BuildTestMessage(){
    	Message msg = Proton.message();
        HashMap<String, String> props = new HashMap<String, String>();
		props.put("some", "test msg");
		ApplicationProperties applicationProperties = new ApplicationProperties(props);
		msg.setApplicationProperties(applicationProperties);
		Binary bodyData = new Binary(Base64.getEncoder().encode("SentFromPOC".getBytes()));
		Section amqpSection = new Data(bodyData);
		msg.setBody(amqpSection);
		return msg;
    }
    
	private static void print(long i, Message msg, boolean verbose) {
        StringBuilder b = new StringBuilder();
        if (verbose) 
        {
        	b.append("message: ");
        	b.append(i).append("\n");
            b.append("Address: ").append(msg.getAddress()).append("\n");
            b.append("Subject: ").append(msg.getSubject()).append("\n");
            b.append("Props:     ").append(msg.getProperties()).append("\n");
            b.append("App Props: ").append(msg.getApplicationProperties()).append("\n");
            b.append("Msg Anno:  ").append(msg.getMessageAnnotations()).append("\n");
            b.append("Del Anno:  ").append(msg.getDeliveryAnnotations()).append("\n");
            b.append(msg.getBody()).append("\n");
            b.append("END").append("\n");
            }
        else 
        {
            /*ApplicationProperties p = msg.getApplicationProperties();
            String s = (p == null) ? "null" : safe(p.getValue());
            b.append("Headers: ").append(s).append("\n");*/
        	b.append("Msg Anno:  ").append(msg.getMessageAnnotations());
        }
        
        System.out.println(b.toString());
    }
	
	private static String safe(Object o) {
        return String.valueOf(o);
    }
	
	static class Person
	{
		Person(){}
		public String Name;
		public int Age;
		public String Address;
		public String Details;
	}
}
