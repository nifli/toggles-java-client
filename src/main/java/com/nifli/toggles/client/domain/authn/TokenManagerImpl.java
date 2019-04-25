package com.nifli.toggles.client.domain.authn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nifli.toggles.client.TogglesConfiguration;

/**
 * Acquires 'client_credentials' tokens (JWT) from the OAuth2 token endpoint. It does not validate the token or refresh it.
 * Instead, when clients use the token to make API calls and receive a 401 in response must call newAccessToken()
 * then use the new token by calling getAccessToken().
 * 
 * To use the token in an API request, simply set the 'Authentication' header using the value returned by getAccessToken().
 * 
 * @author tfredrich
 */
public class TokenManagerImpl
implements TokenManager
{
	private static final String GRANT_TYPE = "client_credentials";
	private static final String SCOPE = "programmatic_client";

	private TogglesConfiguration config;
	private String accessToken;

	/**
	 * Create a new RemoteTokenManager using the provided TogglesConfiguration for settings.
	 * 
	 * @param configuration a TogglesConfiguration instance with the desired settings.
	 */
	public TokenManagerImpl(TogglesConfiguration configuration)
	{
		super();
		this.config = configuration;
	}

	/**
	 * Returns the internally-assigned access token for use in subsequent API calls. If the token was not previously acquired, calls newAccessToken() to acquire one.
	 * 
	 * @return the internally-acquired access token, including the 'Bearer' prefix.
	 * @throws TokenManagerException if newAccesstoken() was called to initialize the access token and an error occurred.
	 */
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

	/**
	 * Calls the OAuth2 token endpoint to acquire an access token. On retryable failure, performs an exponential-backoff retry using config.getRetryDelayMillis().
	 * On success, calls setAccesstoken() with the new access token, which can be accessed via a call to getAccessToken().
	 * 
	 * @throws TokenManagerException if an unrecoverable error occurs or all retries fail.
	 */
	@Override
	public void newAccessToken()
	throws TokenManagerException
	{
		int retries = config.getMaxRetries();
		HttpResponse<TokenResponse> response = null;

		try
		{
			while (retries-- >= 0)
			{
				response = Unirest.post(config.getTokenEndpoint())
					.basicAuth(new String(config.getClientId()), new String(config.getClientSecret()))
					.field("grant_type", GRANT_TYPE)
					.field("scope", SCOPE)
					.asObject(TokenResponse.class);
	
				if (isSuccessful(response))
				{
					setAccessToken(response.getBody().getAccessToken());
					break;
				}
				else if (isFatal(response)) // Don't retry
				{
					throwException(response);
				}

				try
				{
					Thread.sleep(config.getRetryDelayMillis() * (config.getMaxRetries() - retries));
				}
				catch (InterruptedException e)
				{
					// Retry now.
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
		return response.getStatus() == 401 || response.getStatus() == 403 || response.getStatus() == 500;
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
