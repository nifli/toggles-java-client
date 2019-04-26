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
	private static final int DEFAULT_RETRIES = 3;
	private static final long DEFAULT_RETRY_DELAY_MILLIS = 10l;
	private static final String DEFAULT_STAGE = "development";

	private char[] clientId;
	private char[] clientSecret;
	private String baseTokenUrl = DEFAULT_BASE_TOKEN_URL;
	private String baseTogglesUrl = DEFAULT_BASE_TOGGLES_URL;
	private String tokenEndpoint;			// Computed using baseTokenUrl;
	private String togglesEndpointTemplate; // Computed using baseTogglesUrl;
	private String togglesEndpoint;			// Computed using baseTogglesUrl;
	private int maxRetries = DEFAULT_RETRIES;
	private long retryDelayMillis = DEFAULT_RETRY_DELAY_MILLIS;
	private String stage = DEFAULT_STAGE;


	public TogglesConfiguration()
	{
		super();
		refresh();
	}

	public TogglesConfiguration setBaseTokenUrl(String baseUrl)
	{
		assert(baseUrl != null);
		this.baseTokenUrl = baseUrl;
		refresh();
		return this;
	}

	public TogglesConfiguration setBaseTogglesUrl(String baseUrl)
	{
		assert(baseUrl != null);
		this.baseTogglesUrl = baseUrl;
		refresh();
		return this;
	}

	public TogglesConfiguration setClientId(String clientId)
	{
		assert(clientId != null);
		this.clientId = clientId.toCharArray();
		return this;
	}

	public TogglesConfiguration setClientSecret(String clientSecret)
	{
		assert(clientSecret != null);
		this.clientSecret = clientSecret.toCharArray();
		return this;
	}

	public TogglesConfiguration setMaxRetries(int maxRetries)
	{
		assert(maxRetries >= 0);
		this.maxRetries = maxRetries;
		return this;
	}

	public TogglesConfiguration setRetryDelay(long retryDelayMillis)
	{
		assert(retryDelayMillis >= 10l);
		this.retryDelayMillis = retryDelayMillis;
		return this;
	}

	public TogglesConfiguration setStage(String stage)
	{
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
	{
		return new TogglesClient(this);
	}

	private void refresh()
	{
		this.tokenEndpoint = baseTokenUrl + TOKEN_PATH;
		this.togglesEndpointTemplate = baseTogglesUrl + TOGGLES_PATH_TEMPLATE;
		this.togglesEndpoint = String.format(this.togglesEndpointTemplate, getStage());
	}
}
