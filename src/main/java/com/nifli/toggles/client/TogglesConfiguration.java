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
	private static final String DEFAULT_BASE_URL = "https://api.nifli.com";
	private static final String TOKEN_PATH = "/oauth/token";
	private static final String TOGGLES_PATH_TEMPLATE = "/stages/%s/features";
	private static final int DEFAULT_RETRIES = 3;
	private static final long DEFAULT_RETRY_DELAY_MILLIS = 10l;
	private static final String DEFAULT_STAGE = "development";

	private char[] clientId;
	private char[] clientSecret;
	private String baseUrl = DEFAULT_BASE_URL;
	private String tokenEndpoint;			// Computed using baseUrl;
	private String togglesEndpointTemplate; // Computed using baseUrl;
	private String togglesEndpoint;			// Computed using baseUrl;
	private int maxRetries = DEFAULT_RETRIES;
	private long retryDelayMillis = DEFAULT_RETRY_DELAY_MILLIS;
	private String stage = DEFAULT_STAGE;


	public TogglesConfiguration()
	{
		super();
		computeUrls();
	}

	public TogglesConfiguration setBaseUrl(String baseUrl)
	{
		assert(baseUrl != null);
		this.baseUrl = baseUrl;
		computeUrls();
		return this;
	}

	public TogglesConfiguration setClientId(char[] clientId)
	{
		assert(clientId != null);
		this.clientId = clientId;
		return this;
	}

	public TogglesConfiguration setClientSecret(char[] clientSecret)
	{
		assert(clientSecret != null);
		this.clientSecret = clientSecret;
		return this;
	}

	private void computeUrls()
	{
		this.tokenEndpoint = baseUrl + TOKEN_PATH;
		this.togglesEndpointTemplate = baseUrl + TOGGLES_PATH_TEMPLATE;
		this.togglesEndpoint = String.format(this.togglesEndpointTemplate, getStage());
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
		togglesEndpoint = String.format(togglesEndpointTemplate, stage);
		return this;
	}

	public char[] getClientId()
	{
		return clientId;
	}

	public char[] getClientSecret()
	{
		return clientSecret;
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
}
