package com.nifli.toggles.client;

public class TokenManagerException
extends Exception
{
	private static final long serialVersionUID = -7332781700724233007L;
	
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
