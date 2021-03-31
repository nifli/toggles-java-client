package com.nifli.toggles.client.domain;

import java.util.Map;

public class StageToggles
{
	private Stage stage;
	private Map<String, FeatureToggle> features;

	public Stage getStage()
	{
		return stage;
	}

	public FeatureToggle getFeatureToggle(String name)
	{
		return (features != null ? features.get(name) : null);
	}

	/**
	 * Returns a boolean value if the feature name exists in the toggles list, otherwise null.
	 * 
	 * @param name the name of the feature toggle to check.
	 * @return true or false showing the status of the feature toggle. Null if the feature toggle name doesn't exist in the retrieved set of stage toggles.
	 */
	public Boolean isFeatureEnabled(String name)
	{
		FeatureToggle ft = getFeatureToggle(name);
		return (ft != null ? ft.isEnabled() : null);
	}
}
