package com.togglize.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.togglize.client.authn.TokenManager;
import com.togglize.client.domain.StageToggles;
import com.togglize.client.event.ErrorEvent;
import com.togglize.client.event.Events;
import com.togglize.client.event.FetchedEvent;

public class TogglesFetcher
{
	private TokenManager tokens;
	private TogglesConfiguration config;

	public TogglesFetcher(TokenManager tokens, TogglesConfiguration config)
	{
		super();
		this.tokens = tokens;
		this.config = config;
	}

	public StageToggles fetch()
	throws TogglesException
	{
		int retries = config.getMaxRetries();
		HttpResponse<StageToggles> response = null;

		try
		{
			while (retries-- >= 0)
			{
				response = Unirest.get(config.getTogglesEndpoint())
					.header(HttpHeaders.AUTHORIZATION, tokens.getAccessToken())
				    .header("accept", "application/json")
				    .header("Content-Type", "application/json")
					.asObject(StageToggles.class);
	
				if (response.getStatus() == 401) // assume needs a token refresh
				{
					tokens.newAccessToken();
				}
				else if (isSuccessful(response))
				{
					StageToggles allToggles = response.getBody();
					Events.publish(new FetchedEvent(allToggles));
					return allToggles;
				}
				else
				{
					throwException(response);
				}
			}
		}
		catch (UnirestException e)
		{
			Events.publish(new ErrorEvent(e));
			throw new TogglesException(e);
		}

		return null;
	}

	private boolean isSuccessful(HttpResponse<StageToggles> response)
	{
		return response.getStatus() >= 200 && response.getStatus() <= 299;
	}

	private void throwException(HttpResponse<StageToggles> response)
	throws TogglesException
	{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getRawBody())))
		{
			TogglesFetcherException e = new TogglesFetcherException(response.getStatus(), br.lines().collect(Collectors.joining(System.lineSeparator())));
			Events.publish(new ErrorEvent(e));
			throw e;
		}
		catch (IOException e)
		{
			throw new TogglesException(e);
		}
	}
}
