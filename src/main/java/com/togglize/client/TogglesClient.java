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
package com.togglize.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.togglize.client.authn.TokenManager;
import com.togglize.client.authn.TokenManagerException;
import com.togglize.client.authn.TokenManagerImpl;
import com.togglize.client.domain.StageToggles;
import com.togglize.client.event.Events;
import com.togglize.client.event.LocalEventBus;
import com.togglize.client.metrics.MetricsEventHandler;
import com.togglize.client.metrics.MetricsPublisher;

/**
 * The Root class for the Toggles Java API Client. It is the controlling class for all feature flag decisions.
 * 
 * This is a "confidential" client, in that it can only be used on the server-side of an application, for example
 * in a web server-based application.
 * 
 * @author tfredrich
 */
public class TogglesClient
{
	private static final Logger LOG = LogManager.getLogger(TogglesClient.class);

	private static final String TOGGLES_CACHE_NAME = "com.togglize.client.cache";
	private static final String TOGGLES_CLIENT_NAME = "toggles-client-java";

	private Date createdAt = new Date(System.currentTimeMillis());
	private String version; //TODO: inject pom version number here.

	private TogglesConfiguration config;
	private TokenManager tokens;
	private TogglesFetcher toggles;
	private MetricsPublisher metrics;
	private CacheManager cacheManager;
	private long cacheExpiresAt;
	private Cache<String, StageToggles> togglesByClientId;

	/**
	 * Create a new feature flag client with default configuration, using the clientId and secret for this application.
	 * 
	 * @param clientId a client ID acquired from registering an application.
	 * @param clientSecret the secret for the associated client ID, provided when registering an application.
	 */
	public TogglesClient(String clientId, String clientSecret)
	throws TogglesException, TokenManagerException
	{
		this(new TogglesConfiguration(clientId, clientSecret));
	}

	/**
	 * Create a new feature flag client, specifying configuration details in a TogglesConfiguration instance.
	 * 
	 * @param togglesConfiguration a TogglesConfiguration instance. Never null.
	 * @throws TokenManagerException 
	 * @throws TogglesException if an error occurs during fetching of the remote toggles.
	 * @see {@link TogglesConfiguration.newClient()}
	 */
	public TogglesClient(TogglesConfiguration togglesConfiguration)
	throws TogglesException
	{
		super();
		this.config = togglesConfiguration;
		this.tokens = new TokenManagerImpl(togglesConfiguration);
		this.toggles = new TogglesFetcher(tokens, togglesConfiguration);
		this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		this.cacheManager.init();
		Unirest.setTimeouts(config.getConnectionTimeoutMillis(), config.getSocketTimeoutMillis());
		configureJacksonObjectMapper();
		configureEventing(config.getEventPollIntervalMillis());

		if (config.shouldFetchOnStartup())
		{
			fetchToggles();
		}
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
			StageToggles toggles = fetchToggles();

			if (toggles == null) return defaultValue;

			return processContext(featureName, toggles, context, defaultValue);
		}
		catch (TogglesException e)
		{
			// TODO log exceptions
			e.printStackTrace();
		}

		return defaultValue;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public String getVersion()
	{
		return String.format("%s:%s", TOGGLES_CLIENT_NAME, version);
	}

	private StageToggles fetchToggles()
	throws TogglesException
	{
		if (togglesByClientId == null)
		{
			this.togglesByClientId = cacheManager.createCache(TOGGLES_CACHE_NAME,
				CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
					StageToggles.class,
					ResourcePoolsBuilder.heap(1))
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
	throws TogglesException
	{
		StageToggles allToggles = toggles.fetch();

		if (allToggles != null)
		{
			togglesByClientId.put(config.getClientId(), allToggles);
			cacheExpiresAt = System.currentTimeMillis() + config.getCacheTtlMillis();
		}

		return allToggles;
	}

	private boolean processContext(String featureName, StageToggles toggles, TogglesContext context, boolean defaultValue)
	{
		Boolean enabled = toggles.isFeatureEnabled(featureName);

		return (enabled != null ? enabled : defaultValue);
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

	private void configureEventing(long eventPollIntervalMillis)
	{
		LocalEventBus eventBus = new LocalEventBus(Collections.emptyList(), false, eventPollIntervalMillis);
		eventBus.subscribe(config.getEventHandler());
		eventBus.subscribe(new MetricsEventHandler());
		Events.setEventBus(eventBus);
	}
}
