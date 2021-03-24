package org.asf.rats.events.util;

public interface ISyntaxSensitivePromotedEvent extends IPromotedEventProvider {
	public int minimalParameterCount();

	public default int maximalParameterCount() {
		return minimalParameterCount();
	}

	public Class<?>[] parameterTypes();
}
