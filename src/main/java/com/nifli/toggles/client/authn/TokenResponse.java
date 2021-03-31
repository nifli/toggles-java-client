package com.nifli.toggles.client.authn;

public class TokenResponse
{
	private String token_type = "bearer";
	private String access_token;
	private String id_token;
	private String code;
	private int expires_in;
	private String refresh_token;
	private String scope;
	private String state;

	public String getTokenType()
	{
		return token_type;
	}

	public String getAccessToken()
	{
		return access_token;
	}

	public String getIdToken()
	{
		return id_token;
	}

	public String getCode()
	{
		return code;
	}

	public int getExpiresIn()
	{
		return expires_in;
	}

	public String getRefreshToken()
	{
		return refresh_token;
	}

	public String getScope()
	{
		return scope;
	}

	public String getState()
	{
		return state;
	}
}
