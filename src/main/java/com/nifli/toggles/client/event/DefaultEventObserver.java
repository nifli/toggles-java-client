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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author toddf
 * @since Aug 23, 2019
 */
public class DefaultEventObserver
implements EventObserver
{
	private static final Logger LOG = LogManager.getLogger(DefaultEventObserver.class);

	public DefaultEventObserver()
	{
	}

	@Override
	public void onError(ErrorEvent exception)
	{
		LOG.warn(exception.getMessage(), exception.getThrowable());
	}

	@Override
	public void onReady(ReadyEvent ready)
	{
		LOG.info("Toggles client reported ready at: " + SimpleDateFormat.getDateTimeInstance().format(ready.getReadyAt()));
	}

	@Override
	public void onFetched(FetchedEvent fetched)
	{
		LOG.info("Remote toggles fetched at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
	}

	@Override
	public void onEvalutated(EvaluatedEvent evaluated)
	{
		if (evaluated.hasResult())
		{
			LOG.info(String.format("Toggles %s evaluated to: %s", evaluated.getToggle(), (evaluated.hasResult() ? evaluated.getResult() : evaluated.getVariant())));
		}
	}

	@Override
	public void onMetrics(MetricsEvent metrics)
	{
		LOG.info("Metrics published to remote at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
	}

	@Override
	public void onAuthenticated(AuthenticatedEvent authenticatedEvent)
	{
		LOG.info(String.format("Toggles client %s authenticated at %s ", authenticatedEvent.getClientId(), SimpleDateFormat.getDateTimeInstance().format(authenticatedEvent.getAuthenticatedAt())));
	}
}
