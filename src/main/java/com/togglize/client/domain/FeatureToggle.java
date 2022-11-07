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
package com.togglize.client.domain;

/**
 * @author toddf
 * @since Apr 26, 2019
 */
public class FeatureToggle
extends AbstractToggleResponse
{
	private Release release;
	private Boolean featureEnabled;
	private Boolean releaseEnabled;

	public String getReleaseId()
	{
		return (release != null ? release.getId() : null);
	}

	public boolean isFeatureEnabled()
	{
		return (featureEnabled != null ? featureEnabled : false);
	}

	public boolean isReleaseEnabled()
	{
		return (releaseEnabled != null ? releaseEnabled: false);
	}
}
