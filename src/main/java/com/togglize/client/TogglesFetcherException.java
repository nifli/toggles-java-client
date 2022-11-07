package com.togglize.client;

import com.togglize.client.TogglesException;

public class TogglesFetcherException
extends TogglesException
{
	private static final long serialVersionUID = -7332781700724233007L;
	
	private Integer httpStatus;

	public TogglesFetcherException(Exception e)
	{
		super(e);
	}

	public TogglesFetcherException(int status, String message)
	{
		super(message);
		this.httpStatus = status;
	}

	public Integer getHttpStatus()
	{
		return httpStatus;
	}
}
