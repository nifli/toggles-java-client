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

public class TogglesConfiguration
{
	private static final String DEFAULT_BASE_TOKEN_URL = "https://api.nifli.com";
	private static final String DEFAULT_BASE_TOGGLES_URL = "https://api.nifli.com";
	private static final String TOKEN_PATH = "/token";
	private static final String TOGGLES_PATH_TEMPLATE = "/stages/%s/features";
	private static final int DEFAULT_RETRIES = 5;
	private static final long DEFAULT_RETRY_DELAY_MILLIS = 30l;
	private static final String DEFAULT_STAGE = "development";
	private static final long DEFAULT_CACHE_TTL_MILLIS = 100000l;
	private static final long DEFAULT_CONNECTION_TIMEOUT = 10000l;
	private static final long DEFAULT_SOCKET_TIMEOUT = 60000l;

	private char[] clientId;
	private char[] clientSecret;
	private String instanceId;
	private String baseTokenUrl = DEFAULT_BASE_TOKEN_URL;
	private String baseTogglesUrl = DEFAULT_BASE_TOGGLES_URL;
	private String tokenEndpoint;			// Computed using baseTokenUrl;
	private String togglesEndpointTemplate; // Computed using baseTogglesUrl;
	private String togglesEndpoint;			// Computed using baseTogglesUrl;
	private int maxRetries = DEFAULT_RETRIES;
	private long retryDelayMillis = DEFAULT_RETRY_DELAY_MILLIS;
	private String stage = DEFAULT_STAGE;
	private long cacheTtlMillis = DEFAULT_CACHE_TTL_MILLIS;
	private boolean shouldFetchOnStartup = true;
	private long connectionTimeoutMillis = DEFAULT_CONNECTION_TIMEOUT;
	private long socketTimeoutMillis = DEFAULT_SOCKET_TIMEOUT;

	/**
	 * Create a new feature flag configuration instance using the clientId and secret for this application.
	 * 
	 * @param clientId a client ID acquired from registering an application.
	 * @param clientSecret the secret for the associated client ID, provided when registering an application.
	 */
	public TogglesConfiguration(String clientId, String clientSecret)
	{
		super();
		setClientId(clientId);
		setClientSecret(clientSecret);
		refresh();
	}

	/**
	 * Facilitates testing.
	 * 
	 * @param baseUrl
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setBaseTokenUrl(String baseUrl)
	{
		assert(baseUrl != null);
		this.baseTokenUrl = baseUrl;
		refresh();
		return this;
	}

	/**
	 * Facilitates testing.
	 * 
	 * @param baseUrl
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setBaseTogglesUrl(String baseUrl)
	{
		assert(baseUrl != null);
		this.baseTogglesUrl = baseUrl;
		refresh();
		return this;
	}

	/**
	 * Set a unique ID for this application instance. When multiple instances of a particular application
	 * are running, it is sometime useful to distinguish the instances from one another in logs and metrics.
	 * If set, this value is passed along to the Nifli Toggles API with metrics publishing for analytics review.
	 * 
	 * Examples for values are hostname, podId, IP address, etc.
	 * 
	 * @param instanceId a customer-defined string. Possibly null.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
		return this;
	}

	/**
	 * By default, Toggles will fetch the feature toggles settings from the remote API on the first
	 * call to isEnabled(). This first call can have considerable latency due to the JSON deserialization
	 * classes that must be loaded by the JVM class loader.
	 * 
	 * To reduce this latency, TogglesClient can load the feature toggles upon startup, moving the latency
	 * to application startup instead of the initial call to isEnabled(). 
	 * 
	 * @param value true to load feature toggles on application startup.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setShouldFetchOnStartup(boolean value)
	{
		this.shouldFetchOnStartup = value;
		return this;
	}

	/**
	 * Set the length of time in milliseconds that the cached feature flag data is considered active.
	 * Note that this TTL only causes the client to request an update instead of deleting the cache values.
	 * This enables the client to operate consistently even if the remote API is unreachable.
	 * 
	 * @param cacheTtlMillis a long value greater-than or equal-to zero indicating how frequently to refresh the cache.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setCacheTtlMillis(long cacheTtlMillis)
	{
		assert(cacheTtlMillis >= 0l);
		this.cacheTtlMillis = cacheTtlMillis;
		return this;
	}

	/**
	 * Set the maximum number of retries the client will attempt retry-able requests before considering the request a failure.
	 * 
	 * @param maxRetries a long value greater-than or equal-to zero.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setMaxRetries(int maxRetries)
	{
		assert(maxRetries >= 0);
		this.maxRetries = maxRetries;
		return this;
	}

	/**
	 * Set the initial retry delay in milliseconds. This value is used for the first delay before retry.
	 * Subsequent delay values (for this request) will be multiples of this value.
	 * 
	 * @param retryDelayMillis a long value greater-than or equal-to zero.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setRetryDelayMillis(long retryDelayMillis)
	{
		assert(retryDelayMillis >= 0l);
		this.retryDelayMillis = retryDelayMillis;
		return this;
	}

	public TogglesConfiguration setConnectionTimeoutMillis(long connectionTimeoutMillis)
	{
		assert(connectionTimeoutMillis >= 0l);
		this.connectionTimeoutMillis = connectionTimeoutMillis;
		return this;
	}

	public TogglesConfiguration setSocketTimeoutMillis(long socketTimeoutMillis)
	{
		assert(socketTimeoutMillis >= 0l);
		this.socketTimeoutMillis = socketTimeoutMillis;
		return this;
	}

	/**
	 * Set the stage (e.g. development, test, production) that this client is working against.
	 * The default value is 'development'.
	 * 
	 * @param stage a string name of a valid stage in this account. Never null.
	 * @return this TogglesConfiguration instance for method chaining.
	 */
	public TogglesConfiguration setStage(String stage)
	{
		assert(stage != null);
		this.stage = stage;
		refresh();
		return this;
	}

	public String getClientId()
	{
		return new String(clientId);
	}

	public String getClientSecret()
	{
		return new String(clientSecret);
	}

	public String getTokenEndpoint()
	{
		return tokenEndpoint;
	}

	public String getTogglesEndpoint()
	{
		return togglesEndpoint;
	}

	public int getMaxRetries()
	{
		return maxRetries;
	}

	public long getRetryDelayMillis()
	{
		return retryDelayMillis;
	}

	public String getStage()
	{
		return stage;
	}

	public TogglesClient newClient()
	throws ClientException
	{
		return new TogglesClient(this);
	}

	public long getCacheTtlMillis()
	{
		return cacheTtlMillis;
	}

	public String getInstanceId()
	{
		return instanceId;
	}

	public boolean shouldFetchOnStartup()
	{
		return shouldFetchOnStartup;
	}

	public long getConnectionTimeoutMillis()
	{
		return connectionTimeoutMillis;
	}

	public long getSocketTimeoutMillis()
	{
		return socketTimeoutMillis;
	}

	private void setClientId(String clientId)
	{
		assert(clientId != null);
		this.clientId = clientId.toCharArray();
	}

	private void setClientSecret(String clientSecret)
	{
		assert(clientSecret != null);
		this.clientSecret = clientSecret.toCharArray();
	}

	/**
	 * Recomputes the endpoint instance variables whenever something related within this configuration changes.
	 */
	private void refresh()
	{
		this.tokenEndpoint = baseTokenUrl + TOKEN_PATH;
		this.togglesEndpointTemplate = baseTogglesUrl + TOGGLES_PATH_TEMPLATE;
		this.togglesEndpoint = String.format(this.togglesEndpointTemplate, getStage());
	}
}
