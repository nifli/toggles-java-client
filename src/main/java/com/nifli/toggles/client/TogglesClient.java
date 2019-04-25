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
import com.nifli.toggles.client.domain.authn.TokenManager;

/**
 * The controlling class for all feature flag decisions.
 * 
 * @author tfredrich
 */
public class TogglesClient
{
	private TogglesConfiguration config;
	private TokenManager tokens;

	/**
	 * Create a new feature flag client with default configuration, using the clientId and secret for this application.
	 * 
	 * @param clientId
	 * @param clientSecret
	 */
	public TogglesClient(char[] clientId, char[] clientSecret)
	{
		this(new TogglesConfiguration()
			.setClientId(clientId)
			.setClientSecret(clientSecret)
		);
	}

	/**
	 * Create a new feature flag client, specifying configuration details in a TogglesConfiguration instance.
	 * 
	 * @param togglesConfiguration
	 */
	public TogglesClient(TogglesConfiguration togglesConfiguration)
	{
		super();
		this.config = togglesConfiguration;
		this.tokens = new TokenManagerImpl(togglesConfiguration);
	}

	/**
	 * Set which development stage (e.g. dev, test, prod) this client is working against.
	 * 
	 * @param stage the 'slug' name of the desired stage. 
	 * @return this TogglesClient instance to facilitate method chaining.
	 */
	public TogglesClient setStage(String stage)
	{
		this.config.setStage(stage);
		return this;
	}

	/**
	 * Answer whether this feature is enabled in this stage for this application. If the flag is not able to be retrieved
	 * from the remote API, returns the defaultValue.
	 * 
	 * @param featureId either the UUID identifier for the feature or the 'slug' name.
	 * @param defaultValue boolean value to return if unable to retrieve the setting from the API.
	 * @return true if the feature is enabled for this application in the stage.
	 */
	public boolean isEnabled(String featureId, boolean defaultValue)
	{
		return isEnabled(featureId, null, defaultValue);
	}

	/**
	 * Answer whether this feature is enabled in this stage for this application, using the additional context to test against
	 * feature activation strategies. If the flag is not able to be retrieved from the remote API, returns the defaultValue.
	 * 
	 * @param featureId either the UUID identifier for the feature or the 'slug' name.
	 * @param context additional contextual values to test against feature-activation strategies.
	 * @param defaultValue boolean value to return if unable to retrieve the setting from the API.
	 * @return true if the feature is enabled for this application in the stage, given the context.
	 */
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
