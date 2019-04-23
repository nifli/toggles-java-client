package com.nifli.toggles.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nifli.toggles.client.domain.TokenManager;
import com.nifli.toggles.client.domain.authn.TokenResponse;

public class RemoteTokenManager
implements TokenManager
{
	private static final String GRANT_TYPE = "client_credentials";
	private static final String SCOPES = "programmatic_client";

	private TogglesConfiguration config;
	private String accessToken;

	public RemoteTokenManager(TogglesConfiguration configuration)
	{
		super();
		this.config = configuration;
	}

	@Override
	public String getAccessToken()
	throws TokenManagerException
	{
		if (accessToken == null)
		{
			newAccessToken();
		}

		return accessToken;
	}

	@Override
	public void newAccessToken()
	throws TokenManagerException
	{
		int retries = config.getMaxRetries();
		HttpResponse<TokenResponse> response = null;

		try
		{
			while (--retries >= 0)
			{
				response = Unirest.post(config.getTokenEndpoint())
					.basicAuth(new String(config.getClientId()), new String(config.getClientSecret()))
					.field("grant_type", GRANT_TYPE)
					.field("scope", SCOPES)
					.asObject(TokenResponse.class);
	
				if (isSuccessful(response))
				{
					setAccessToken(response.getBody().getAccessToken());
				}
				else if (isFatal(response))
				{
					throwException(response);
				}
			}
		}
		catch (UnirestException e)
		{
			throw new TokenManagerException(e);
		}

		throwException(response);
	}

	private void setAccessToken(String token)
	{
		this.accessToken = "Bearer " + token;
	}

	private boolean isSuccessful(HttpResponse<TokenResponse> response)
	{
		return response.getStatus() >= 200 && response.getStatus() <= 299;
	}

	private boolean isFatal(HttpResponse<TokenResponse> response)
	{
		return response.getStatus() == 401 || response.getStatus() == 403;
	}

	private void throwException(HttpResponse<TokenResponse> response)
	throws TokenManagerException
	{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getRawBody())))
		{
			throw new TokenManagerException(response.getStatus(), br.lines().collect(Collectors.joining(System.lineSeparator())));
		}
		catch (IOException e)
		{
			throw new TokenManagerException(e);
		}
	}
}
