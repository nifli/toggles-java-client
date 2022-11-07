package com.togglize.client.authn;

import com.togglize.client.TogglesException;

public class TokenManagerException
extends TogglesException
{
	private static final long serialVersionUID = 8042743696336541838L;

	private Integer httpStatus;

	public TokenManagerException(Exception e)
	{
		super(e);
	}

	public TokenManagerException(int status, String message)
	{
		super(message);
		this.httpStatus = status;
	}

	public Integer getHttpStatus()
	{
		return httpStatus;
	}
}
