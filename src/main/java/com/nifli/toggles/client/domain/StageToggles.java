package com.nifli.toggles.client.domain;

import java.util.Map;

public class StageToggles
{
	private Stage stage;
	private Map<String, ReleaseToggle> releases;
	private Map<String, FeatureToggle> features;

	public FeatureToggle getFeatureToggle(String name)
	{
		return (features != null ? features.get(name) : null);
	}

	public ReleaseToggle getReleaseToggle(String releaseId)
	{
		if (releaseId == null) return null;

		return (releases != null ? releases.get(releaseId) : null);
	}

	public Boolean isFeatureEnabled(String name)
	{
		FeatureToggle ft = getFeatureToggle(name);

		if (ft == null) return null;

		if (ft.isEnabled()) return true;

		ReleaseToggle rt = getReleaseToggle(ft.getReleaseId());

		return (rt != null ? rt.isEnabled() : false);
	}
}
