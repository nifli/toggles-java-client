/*
    Copyright 2019, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.nifli.toggles.client.event;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class EventMonitor
extends Thread
{
	// SECTION: CONSTANTS

	private static final Executor EVENT_EXECUTOR = Executors.newCachedThreadPool();

	
	// SECTION: INSTANCE METHODS

	private Map<Class<?>, List<EventHandler>> handlersByEvent = new ConcurrentHashMap<Class<?>, List<EventHandler>>();
	private Set<EventHandler> handlers = new LinkedHashSet<EventHandler>();
	private boolean shouldShutDown = false;
	private boolean shouldReRaiseOnError = true;
	private LocalEventBus eventBus;
	private long delay;


	// SECTION: CONSTRUCTORS

	public EventMonitor(LocalEventBus eventBus, long pollDelayMillis)
	{
		super();
		setDaemon(true);
		this.delay = pollDelayMillis;
		this.eventBus = eventBus;
	}

	
	// SECTION: INSTANCE METHODS

	public synchronized boolean register(EventHandler handler)
	{
		boolean result = handlers.add(handler);
		handlersByEvent.clear();
		return result;
	}

	public synchronized boolean unregister(EventHandler handler)
	{
		if (handlers.remove(handler))
		{
			handlersByEvent.clear();
			return true;
		}
		
		return false;
	}

	public void shutdown()
	{
		shouldShutDown = true;
		System.out.println("Event monitor notified for shutdown.");

		synchronized(eventBus)
		{
			eventBus.notifyAll();
		}
	}

	public void setReRaiseOnError(boolean value)
	{
		this.shouldReRaiseOnError = value;
	}

	
	// SECTION: RUNNABLE/THREAD

	@Override
	public void run()
	{
		System.out.println("Event monitor starting...");

		while(!shouldShutDown)
		{
			try
			{
				synchronized (eventBus)
				{
					if (eventBus.isEmpty())
					{
						eventBus.wait(delay);		// Support wake-up via eventQueue.notify()
					}
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				System.err.println("Interrupted (use shutdown() to terminate).  Continuing...");
				continue;
			}

			TogglesEvent event = null;

			while ((event = eventBus.poll()) != null)
			{
				processEvent(event);
			}
		}
		
		System.out.println("Event monitor exiting...");
		clearAllHandlers();
	}

	/**
	 * Runs each appropriate EventHandler in an Executor.
	 * 
	 * @param event
	 */
	private void processEvent(final TogglesEvent event)
    {
	    System.out.println("Processing event: " + event.toString());
	    for (final EventHandler handler : getConsumersFor(event.getClass()))
	    {
    		EVENT_EXECUTOR.execute(new Runnable(){
				@Override
                public void run()
                {
			    	try
			    	{
			    		handler.handle(event);
			    	}
			    	catch(Exception e)
			    	{
			    		e.printStackTrace();
			    		
			    		if (shouldReRaiseOnError)
			    		{
			    			System.out.println("Event handler failed. Re-publishing event: " + event.toString());
			    			eventBus.publish(event);
			    		}
			    	}
                }
    		});
	    }
    }

	
	// SECTION: UTILITY - PRIVATE

	private void clearAllHandlers()
    {
	    handlers.clear();
		handlersByEvent.clear();
    }

	private synchronized List<EventHandler> getConsumersFor(Class<? extends TogglesEvent> eventClass)
	{
		List<EventHandler> result = handlersByEvent.get(eventClass);
		
		if (result == null)
		{
			result = new ArrayList<EventHandler>();
			handlersByEvent.put(eventClass, result);
			
			for (EventHandler consumer : handlers)
			{
				if (consumer.handles(eventClass))
				{
					result.add(consumer);
				}
			}
		}

		return result;
	}
}