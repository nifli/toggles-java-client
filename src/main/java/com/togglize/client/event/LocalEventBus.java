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
package com.togglize.client.event;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class LocalEventBus
{
	private Queue<TogglesEvent> queue = new ConcurrentLinkedQueue<TogglesEvent>();
	private EventMonitor monitor;

	public LocalEventBus(Collection<EventHandler> handlers, boolean shouldReraiseOnError, long pollDelayMillis)
	{
		super();
		initializeMonitor(handlers, shouldReraiseOnError, pollDelayMillis);
	}

	/**
	 * @param handlers
	 */
	private void initializeMonitor(Collection<EventHandler> handlers, boolean shouldReraiseOnError, long pollDelayMillis)
	{
		monitor = new EventMonitor(this, pollDelayMillis);

		for (EventHandler handler : handlers)
		{
			monitor.register(handler);
		}

		retryOnError(shouldReraiseOnError);
		monitor.start();
	}

	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	public TogglesEvent poll()
	{
		return queue.poll();
	}

	public void publish(TogglesEvent event)
	{
		queue.add(event);

		synchronized (this)
		{
			notifyAll();
		}
	}

	/**
	 * @param value
	 */
    public void retryOnError(boolean value)
    {
		monitor.setReRaiseOnError(value);
    }

	public void shutdown()
	{
		queue.clear();
		queue = null;
	}

    public boolean subscribe(EventHandler handler)
    {
		return monitor.register(handler);
    }

    public boolean unsubscribe(EventHandler handler)
    {
    	return monitor.unregister(handler);
    }
}