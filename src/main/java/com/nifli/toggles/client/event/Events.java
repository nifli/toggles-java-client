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

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class Events
{
	private static final Events INSTANCE = new Events();

	private LocalEventBus eventBus;

	private Events()
	{
		super();
	}

	/**
	 * Get the Singleton instance of DomainEvents.
	 */
	public static Events instance()
	{
		return INSTANCE;
	}

	public static void setEventBus(LocalEventBus bus)
	{
		instance().eventBus = bus;
	}

	/**
	 * Publish an event, passing it to applicable consumers asynchronously.
	 * 
	 * Event publishing can only occur after event busses are setup.
	 * 
	 * @param event the Object as an event to publish.
	 */
	public static void publish(TogglesEvent event)
	{
		instance().publishEvent(event);
	}
	
	/**
	 * Shutdown all the even busses, releasing their resources cleanly.
	 * <p/>
	 * shutdown() should be called at application termination to cleanly release
	 * all consumed resources.
	 */
	public static void shutdown()
	{
		instance().shutdownEventBusses();
	}

	/**
	 * Raise an event on all event busses, passing it to applicable consumers asynchronously.
	 * 
	 * @param event
	 */
	private void publishEvent(TogglesEvent event)
	{
		eventBus.publish(event);
	}

	private void shutdownEventBusses()
	{
		eventBus.shutdown();
	}
}