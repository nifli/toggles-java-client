package com.nifli.toggles.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

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
	{
		if (accessToken == null)
		{
			accessToken = newAccessToken();
		}

		return accessToken;
	}

	@Override
	public String newAccessToken()
	{
		int retries = config.getMaxRetries();
		HttpResponse<TokenResponse> response = null;

		try
		{
			while (--retries >= 0)
			{
				response = Unirest.post(config.getTokenEndpoint())
					.basicAuth(new String(config.getClientId()), new String(config.getClientSecret()))
					.header(HttpHeaders.CONTENT_TYPE, "application/json")
					.field("grant_type", GRANT_TYPE)
					.field("scopes", SCOPES)
					.asObject(TokenResponse.class);
	
				if (isSuccessful(response))
				{
					return response.getBody().getAccessToken();
				}
				else if (isFatal(response))
				{
//					LOG.fatal(String.format("Unable to get a token. Status: %d - %s", response.getStatus(), response.getBody().getMessage()));
					throwException(response);
				}
			}
		}
		catch (UnirestException e)
		{
			throw new TokenManagerException(e);
		}
	}

	private boolean isSuccessful(HttpResponse<TokenResponse> response)
	{
		return response.getStatus() >= 200 && response.getStatus() <= 299;
	}

	private void throwException(HttpResponse<TokenResponse> response)
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
