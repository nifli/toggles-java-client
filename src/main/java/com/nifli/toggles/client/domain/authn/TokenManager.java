package com.nifli.toggles.client.domain.authn;

public interface TokenManager
{
	/**
	 * Retrieve the Access Token assigned within this TokenManager. If the current token is null,
	 * a new one is acquired before returning it.
	 * 
	 * @return an Access Token.
	 * @throws TokenManagerException if an error occurs.
	 */
	public String getAccessToken() throws TokenManagerException;

	/**
	 * Acquires a new Access Token and assigns it internally within the TokenManager.
	 * Retrieve the token via a call to getAccessToken().
	 * 
	 * @throws TokenManagerException if an error occurs.
	 */
	public void newAccessToken() throws TokenManagerException;
}
