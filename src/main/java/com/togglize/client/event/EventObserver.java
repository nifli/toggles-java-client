package com.togglize.client.event;

public interface EventObserver
{
	void onError(ErrorEvent exception);
	void onReady(ReadyEvent ready);
	void onFetched(FetchedEvent fetched);
	void onEvalutated(EvaluatedEvent evaluated);
	void onMetrics(MetricsEvent metrics);
	void onAuthenticated(AuthenticatedEvent authenticated);
}
