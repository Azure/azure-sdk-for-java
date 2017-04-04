package com.microsoft.azure.servicebus.primitives;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

// To complete futures using a different thread. Otherwise every future is completed on the single reactor thread
// which badly affects perf and a client can potentially kill the thread or lock the thread.
class AsyncUtil {
	// Use default asynchronous executor of the CompletableFuture
	private static ExecutorService executorService = ForkJoinPool.commonPool();
	
	public static <T> boolean completeFutureAndGetStatus(CompletableFuture<T> future, T result)
	{
		try {
			return executorService.submit(new CompleteCallable<T>(future, result)).get();
		} catch (InterruptedException | ExecutionException e) {			
			e.printStackTrace();
			return false;
		}				
	}
	
	public static <T> void completeFuture(CompletableFuture<T> future, T result)
	{
		executorService.submit(new CompleteCallable<T>(future, result));			
	}
	
	public static <T> boolean completeFutureExceptionallyAndGetStatus(CompletableFuture<T> future, Throwable exception)
	{
		try {
			return executorService.submit(new CompleteExceptionallyCallable<T>(future, exception)).get();
		} catch (InterruptedException | ExecutionException e) {			
			e.printStackTrace();
			return false;
		}				
	}
	
	public static <T> void completeFutureExceptionally(CompletableFuture<T> future, Throwable exception)
	{
		executorService.submit(new CompleteExceptionallyCallable<T>(future, exception));
	}
	
	public static void run(Runnable runnable)
	{
		executorService.submit(runnable);
	}
	
	private static class CompleteCallable<T> implements Callable<Boolean>
	{
		private CompletableFuture<T> future;
		private T result;
		
		CompleteCallable(CompletableFuture<T> future, T result)
		{
			this.future = future;
			this.result = result;
		}
		
		@Override
		public Boolean call() throws Exception {
			return this.future.complete(this.result);
		}		
	}
	
	private static class CompleteExceptionallyCallable<T> implements Callable<Boolean>
	{
		private CompletableFuture<T> future;
		private Throwable exception;
		
		CompleteExceptionallyCallable(CompletableFuture<T> future, Throwable exception)
		{
			this.future = future;
			this.exception = exception;
		}
		
		@Override
		public Boolean call() throws Exception {
			return this.future.completeExceptionally(this.exception);
		}		
	}
}
