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

import com.nifli.toggles.client.domain.StageToggles;

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class FetchedEvent
extends TogglesEvent
{
	private StageToggles toggles;

	public FetchedEvent(StageToggles toggles)
	{
		super();
		this.toggles = toggles;
	}

	@Override
	public void observe(EventObserver observer)
	{
		observer.onFetched(this);
	}

	public StageToggles getToggles()
	{
		return toggles;
	}
}
