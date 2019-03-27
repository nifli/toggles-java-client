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
	private static final String DEFAULT_TOKEN_ENDPOINT = "https://api.nifli.com/oauth/token";

	private char[] clientId;
	private char[] clientSecret;
	private String tokenEndpoint = DEFAULT_TOKEN_ENDPOINT;

	public TogglesConfiguration()
	{
		super();
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

	public TogglesConfiguration setTokenEndpoint(String tokenUrl)
	{
		assert(tokenUrl != null);
		this.tokenEndpoint = tokenUrl;
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

	public TogglesClient newClient()
	{
		return new TogglesClient(this);
	}
}
