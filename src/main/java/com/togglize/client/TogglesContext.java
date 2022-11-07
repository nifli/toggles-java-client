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
package com.togglize.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.togglize.client.domain.Account;
import com.togglize.client.domain.Organization;
import com.togglize.client.domain.User;

public class TogglesContext
{
	private User user;
	private Organization organization;
	private Account account;
	private Map<String, String> values;

	public TogglesContext()
	{
		super();
	}

	public User getUser()
	{
		return user;
	}

	public TogglesContext setUser(User user)
	{
		this.user = user;
		return this;
	}

	public Organization getOrganization()
	{
		return organization;
	}

	public TogglesContext setOrganization(Organization organization)
	{
		this.organization = organization;
		return this;
	}

	public Account getAccount()
	{
		return account;
	}

	public TogglesContext setAccount(Account account)
	{
		this.account = account;
		return this;
	}

	public String get(String name)
	{
		if (values == null) return null;
		return values.get(name);
	}

	public TogglesContext put(String name, String value)
	{
		if (values == null)
		{
			values = new HashMap<>();
		}

		values.put(name, value);
		return this;
	}

	public Map<String, String> getValues()
	{
		if (values == null) return Collections.emptyMap();
		return Collections.unmodifiableMap(values);
	}
}
