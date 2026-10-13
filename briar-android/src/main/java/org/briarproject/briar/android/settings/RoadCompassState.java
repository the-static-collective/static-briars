package org.briarproject.briar.android.settings;

/**
 * A privacy-preserving interpretation of local transport preferences.
 *
 * Settings are NOT proof of active connections, route reachability, privacy,
 * or remote message delivery. No probes, identifiers, or network logs.
 *
 * Plain Java so that the classification can be tested without Android.
 */
public final class RoadCompassState {

	public enum Mode {
		LOADING, DISABLED, ENABLED, WIFI_ONLY, CHARGING_ONLY,
		WIFI_AND_CHARGING
	}

	private RoadCompassState() {
	}

	public static Mode localMode(Boolean enabled) {
		if (enabled == null) return Mode.LOADING;
		return enabled ? Mode.ENABLED : Mode.DISABLED;
	}

	public static Mode torMode(Boolean enabled, Boolean allowMobile,
			Boolean onlyWhenCharging) {
		if (enabled == null) return Mode.LOADING;
		if (!enabled) return Mode.DISABLED;
		if (allowMobile == null || onlyWhenCharging == null) {
			return Mode.LOADING;
		}
		if (!allowMobile && onlyWhenCharging) return Mode.WIFI_AND_CHARGING;
		if (!allowMobile) return Mode.WIFI_ONLY;
		if (onlyWhenCharging) return Mode.CHARGING_ONLY;
		return Mode.ENABLED;
	}
}
