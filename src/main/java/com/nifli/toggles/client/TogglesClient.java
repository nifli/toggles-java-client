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

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpHeaders;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nifli.toggles.client.authn.TokenManager;
import com.nifli.toggles.client.authn.TokenManagerException;
import com.nifli.toggles.client.authn.TokenManagerImpl;
import com.nifli.toggles.client.domain.StageToggles;

/**
 * The controlling class for all feature flag decisions.
 * 
 * @author tfredrich
 */
public class TogglesClient
{
	private static final String TOGGLES_CACHE_NAME = "com.nifli.toggles.client.cache";

	private TogglesConfiguration config;
	private TokenManager tokens;
	private CacheManager cacheManager;
	private long cacheExpiresAt;
	private Cache<String, StageToggles> togglesByClientId;

	/**
	 * Create a new feature flag client with default configuration, using the clientId and secret for this application.
	 * 
	 * @param clientId
	 * @param clientSecret
	 */
	public TogglesClient(String clientId, String clientSecret)
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
		this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		this.cacheManager.init();
		configureJacksonObjectMapper();
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
	 * from the remote API, returns the false (as the default value).
	 * 
	 * Same as calling isEnabled(featureName, false).
	 * 
	 * @param featureName the textual name of the feature.
	 * @return true if the feature is enabled for this application in the stage. Otherwise, false. If the feature flag
	 * setting was unable to be retrieved from the remote API, returns false (as the default value).
	 */
	public boolean isEnabled(String featureName)
	{
		return isEnabled(featureName, null);
	}

	/**
	 * Answer whether this feature is enabled in this stage for this application. If the flag is not able to be retrieved
	 * from the remote API, returns the defaultValue.
	 * 
	 * @param featureName the textual name of the feature.
	 * @param defaultValue boolean value to return if unable to retrieve the setting from the API.
	 * @return true if the feature is enabled for this application in the stage.
	 */
	public boolean isEnabled(String featureName, boolean defaultValue)
	{
		return isEnabled(featureName, null, defaultValue);
	}

	/**
	 * Answer whether this feature is enabled in this stage for this application, using the additional context to test against
	 * feature activation strategies. If the flag is not able to be retrieved from the remote API, returns false.
	 * 
	 * Same as calling isEnabled(featureName, context, false)
	 * 
	 * @param featureName the textual name of the feature.
	 * @param context additional contextual values to test against feature-activation strategies. Possibly null.
	 * @return true if the feature is enabled for this application in the stage, given the context. Otherwise, false.
	 */
	public boolean isEnabled(String featureName, TogglesContext context)
	{
		return isEnabled(featureName, context, false);
	}

	/**
	 * Answer whether this feature is enabled in this stage for this application, using the additional context to test against
	 * feature activation strategies. If the flag is not able to be retrieved from the remote API, returns the defaultValue.
	 * 
	 * @param featureName the textual name of the feature.
	 * @param context additional contextual values to test against feature-activation strategies. Possibly null.
	 * @param defaultValue boolean value to return if unable to retrieve the setting from the API.
	 * @return true if the feature is enabled for this application in the stage, given the context.
	 */
	public boolean isEnabled(String featureName, TogglesContext context, boolean defaultValue)
	{
		try
		{
			StageToggles toggles = getToggles();

			if (toggles == null) return defaultValue;

			return processContext(featureName, toggles, context, defaultValue);
		}
		catch (UnirestException | TokenManagerException e1)
		{
			// TODO log exceptions
			e1.printStackTrace();
		}

		return defaultValue;
	}

	private StageToggles getToggles()
	throws UnirestException, TokenManagerException
	{
		if (togglesByClientId == null)
		{
			this.togglesByClientId = cacheManager.createCache(TOGGLES_CACHE_NAME,
				CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
					StageToggles.class,
					ResourcePoolsBuilder.heap(10))
				.build()); 
		}

		StageToggles toggles = togglesByClientId.get(config.getClientId());

		if (shouldRefresh(toggles))
		{
			toggles = refreshCache();
		}

		return toggles;
	}

	private boolean shouldRefresh(StageToggles toggles)
	{
		return (toggles == null || cacheExpiresAt == 0l || System.currentTimeMillis() > cacheExpiresAt);
	}

	private StageToggles refreshCache()
	throws UnirestException, TokenManagerException
	{
		StageToggles toggles = getRemoteToggles();

		if (toggles != null)
		{
			togglesByClientId.put(config.getClientId(), toggles);
			cacheExpiresAt = System.currentTimeMillis() + config.getCacheTtlMillis();
		}

		return toggles;
	}

	private boolean processContext(String featureName, StageToggles toggles, TogglesContext context, boolean defaultValue)
	{
		Boolean enabled = toggles.isFeatureEnabled(featureName);

		return (enabled != null ? enabled : defaultValue);
	}

	private StageToggles getRemoteToggles()
	throws UnirestException, TokenManagerException
	{
		int retries = config.getMaxRetries();
		HttpResponse<StageToggles> response = null;

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
				return response.getBody();
			}
		}

		return null;
	}

	private boolean isSuccessful(HttpResponse<StageToggles> response)
	{
		return response.getStatus() >= 200 && response.getStatus() <= 299;
	}

	private void configureJacksonObjectMapper()
	{
		Unirest.setObjectMapper(new ObjectMapper()
		{
			private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
	
				// Ignore additional/unknown properties in a payload.
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				
				// Only serialize populated properties (do no serialize nulls)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				
				// Use fields directly.
				.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
				
				// Ignore accessor and mutator methods (use fields per above).
				.setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
				.setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
				.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
				
				// Set default ISO 8601 timepoint output format.
				.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));


			public <T> T readValue(String value, Class<T> valueType)
			{
				try
				{
					return jacksonObjectMapper.readValue(value, valueType);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}

			public String writeValue(Object value)
			{
				try
				{
					return jacksonObjectMapper.writeValueAsString(value);
				}
				catch (JsonProcessingException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}
}
