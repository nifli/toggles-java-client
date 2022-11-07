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

/**
 * Defines the interface for objects that can process (handle) domain events.
 * Implementations of this interface are registered with the EventMonitor via
 * a call to Events.register() or with the EventMonitor instance directly.
 * 
 * @author toddf
 * @since Aug 23, 2019
 */
public interface EventHandler
{
	/**
	 * Process the given event. Called by the EventMonitor when an event occurs.
	 * 
	 * @param event
	 * @throws Exception if something goes wrong
	 */
	public void handle(TogglesEvent event)
	throws Exception;
	
	/**
	 * Answers whether this EventHandler can handle events of the given type.
	 * If true is returned, the EventMonitor will call handle(), otherwise,
	 * the EventMonitor will not ask this event handler to process the event.
	 * 
	 * @param eventClass
	 * @return
	 */
	public boolean handles(Class<? extends TogglesEvent> eventClass);
}