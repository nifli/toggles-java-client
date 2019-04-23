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
package com.nifli.toggles.client;

import org.apache.http.HttpHeaders;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nifli.toggles.client.domain.Toggles;
import com.nifli.toggles.client.domain.TokenManager;

/**
 * @author tfredrich
 *
 */
public class TogglesClient
{
	private TogglesConfiguration config;
	private TokenManager tokens;

	public TogglesClient(char[] clientId, char[] clientSecret)
	{
		this(new TogglesConfiguration()
			.setClientId(clientId)
			.setClientSecret(clientSecret)
		);
	}

	public TogglesClient(TogglesConfiguration togglesConfiguration)
	{
		super();
		this.config = togglesConfiguration;
		this.tokens = new RemoteTokenManager(togglesConfiguration);
	}

	public TogglesClient setStage(String stage)
	{
		this.config.setStage(stage);
		return this;
	}

	public boolean isEnabled(String featureId, boolean defaultValue)
	{
		return isEnabled(featureId, null, defaultValue);
	}

	public boolean isEnabled(String featureId, TogglesContext context, boolean defaultValue)
	{
		int retries = config.getMaxRetries();
		HttpResponse<Toggles> response = null;

		try
		{
			while (--retries >= 0)
			{
				response = Unirest.get(config.getTogglesEndpoint())
					.header(HttpHeaders.AUTHORIZATION, tokens.getAccessToken())
					.asObject(Toggles.class);
	
				if (response.getStatus() == 401) // assume needs a token refresh
				{
					tokens.newAccessToken();
				}
				else if (isSuccessful(response))
				{
					return processContext(response.getBody(), context, defaultValue);
				}
			}
		}
		catch (UnirestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TokenManagerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return defaultValue;
	}

	private boolean processContext(Toggles body, TogglesContext context, boolean defaultValue)
	{
		// TODO Auto-generated method stub
		return defaultValue;
	}

	private boolean isSuccessful(HttpResponse<Toggles> response)
	{
		return response.getStatus() >= 200 && response.getStatus() <= 299;
	}
}
