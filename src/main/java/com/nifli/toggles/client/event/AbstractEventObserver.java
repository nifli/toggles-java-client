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
 * An empty, do-nothing implementation of TogglesEventObserver.
 * 
 * @author toddf
 * @since Aug 23, 2019
 * @see EventObserver
 */
public abstract class AbstractEventObserver
implements EventObserver
{
	@Override
	public void onAuthenticated(AuthenticatedEvent authenticatedEvent)
	{
	}

	@Override
	public void onError(ErrorEvent exception)
	{
	}

	@Override
	public void onReady(ReadyEvent ready)
	{
	}

	@Override
	public void onFetched(FetchedEvent fetched)
	{
	}

	@Override
	public void onEvalutated(EvaluatedEvent evaluated)
	{
	}

	@Override
	public void onMetrics(MetricsEvent metrics)
	{
	}
}
