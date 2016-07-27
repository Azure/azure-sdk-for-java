/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashSet;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Selectable.Callback;

/**
 * {@link Reactor} is not thread-safe - all calls to {@link Proton} API's should be - on the Reactor Thread.
 * {@link Reactor} works out-of-box for all event driven API - ex: onReceive - which could raise upon onSocketRead.
 * {@link Reactor} didn't support API's like Send() out-of-box - which could potentially run on different thread to that of Reactor.
 * So, the following utility class is used to generate an Event to hook into {@link Reactor}'s event delegation pattern.
 * It uses a {@link Pipe} as the IO on which Reactor Listens to.
 * Cardinality: multiple {@link ReactorDispatcher}'s could be attached to 1 {@link Reactor}.
 * Each {@link ReactorDispatcher} should be initialized Synchronously - as it calls API in {@link Reactor} which is not thread-safe. 
 */
public final class ReactorDispatcher
{
	private final Reactor reactor;
	private final Pipe ioSignal;
	private final ConcurrentLinkedQueue<BaseHandler> workQueue;
	private final ScheduleHandler workScheduler;

	public ReactorDispatcher(final Reactor reactor) throws IOException
	{
		this.reactor = reactor;
		this.ioSignal = Pipe.open();
		this.workQueue = new ConcurrentLinkedQueue<BaseHandler>();
		this.workScheduler = new ScheduleHandler();
		
		initializeSelectable();
	}
	
	private void initializeSelectable()
	{
		Selectable schedulerSelectable = this.reactor.selectable();
		
		schedulerSelectable.setChannel(this.ioSignal.source());
		schedulerSelectable.onReadable(this.workScheduler);
		schedulerSelectable.onFree(new CloseHandler());
		
		schedulerSelectable.setReading(true);
		this.reactor.update(schedulerSelectable);
	}

	public void invoke(final BaseHandler timerCallback) throws IOException
	{
		this.workQueue.offer(timerCallback);
		this.signalWorkQueue();
	}
	
	public void invoke(final int delay, final BaseHandler timerCallback) throws IOException
	{
		this.workQueue.offer(new DelayHandler(this.reactor, delay, timerCallback));
		this.signalWorkQueue();
	}
	
	private void signalWorkQueue() throws IOException
	{
		this.ioSignal.sink().write(ByteBuffer.allocate(1));
	}
	
	private final class DelayHandler extends BaseHandler
	{
		final int delay;
		final BaseHandler timerCallback;
		final Reactor reactor;
		
		public DelayHandler(final Reactor reactor, final int delay, final BaseHandler timerCallback)
		{
			this.delay = delay;
			this.timerCallback = timerCallback;
			this.reactor = reactor;
		}
		
		@Override
		public void onTimerTask(Event e) 
		{
			this.reactor.schedule(this.delay, this.timerCallback);
		}
	}
	
	private final class ScheduleHandler implements Callback
	{
		@Override
		public void run(Selectable selectable)
		{
			try
			{
				ioSignal.source().read(ByteBuffer.allocate(1024));
			}
			catch(ClosedChannelException ignore)
			{
			}
			catch(IOException ioException)
			{
				throw new RuntimeException(ioException);
			}
			
			final HashSet<BaseHandler> completedWork = new HashSet<BaseHandler>();
			
			BaseHandler topWork = workQueue.poll(); 
			while (topWork != null)
			{
				if (!completedWork.contains(topWork))
				{
					topWork.onTimerTask(null);
					completedWork.add(topWork);
				}
				
				topWork = workQueue.poll();
			}
		}
	}
	
	private final class CloseHandler implements Callback
	{
		@Override public void run(Selectable selectable)
		{
			try
			{
				selectable.getChannel().close();
			}
			catch (IOException ignore)
			{
			}
			
			try
			{
				if (ioSignal.sink().isOpen())
					ioSignal.sink().close();
			}
			catch (IOException ignore)
			{
			}
			
			workScheduler.run(null);
			
			try
			{
				if (ioSignal.source().isOpen())
					ioSignal.source().close();
			}
			catch (IOException ignore)
			{
			}
		}
	}
}
