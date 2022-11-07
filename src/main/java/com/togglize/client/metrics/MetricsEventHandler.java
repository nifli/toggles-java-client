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
package com.togglize.client.metrics;

import com.togglize.client.event.AbstractEventObserver;
import com.togglize.client.event.EvaluatedEvent;
import com.togglize.client.event.EventHandler;
import com.togglize.client.event.TogglesEvent;

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class MetricsEventHandler
extends AbstractEventObserver
implements EventHandler
{
	@Override
	public void handle(TogglesEvent event) throws Exception
	{
		event.observe(this);
	}

	@Override
	public boolean handles(Class<? extends TogglesEvent> eventClass)
	{
		return eventClass.isAssignableFrom(EvaluatedEvent.class);
	}

	@Override
	public void onEvalutated(EvaluatedEvent evaluated)
	{
		// TODO accumulate metrics buckets
	}
}
