package org.briarproject.briar.android.settings;

import org.junit.Test;

import static org.briarproject.briar.android.settings.RoadCompassState.Mode.CHARGING_ONLY;
import static org.briarproject.briar.android.settings.RoadCompassState.Mode.DISABLED;
import static org.briarproject.briar.android.settings.RoadCompassState.Mode.ENABLED;
import static org.briarproject.briar.android.settings.RoadCompassState.Mode.LOADING;
import static org.briarproject.briar.android.settings.RoadCompassState.Mode.WIFI_AND_CHARGING;
import static org.briarproject.briar.android.settings.RoadCompassState.Mode.WIFI_ONLY;
import static org.junit.Assert.assertEquals;

public class RoadCompassStateTest {

	@Test
	public void localPreferencesNeverClaimConnectivity() {
		assertEquals(LOADING, RoadCompassState.localMode(null));
		assertEquals(DISABLED, RoadCompassState.localMode(false));
		assertEquals(ENABLED, RoadCompassState.localMode(true));
	}

	@Test
	public void torOffAlwaysWins() {
		for (Boolean allowed : new Boolean[]{null, false, true}) {
			for (Boolean charging : new Boolean[]{null, false, true}) {
				assertEquals(DISABLED, RoadCompassState.torMode(
						false, allowed, charging));
			}
		}
	}

	@Test
	public void torDistinguishesRestrictionsWithoutInventingCircuitStatus() {
		assertEquals(LOADING, RoadCompassState.torMode(null, true, false));
		assertEquals(LOADING, RoadCompassState.torMode(true, null, false));
		assertEquals(LOADING, RoadCompassState.torMode(true, true, null));
		assertEquals(ENABLED, RoadCompassState.torMode(true, true, false));
		assertEquals(WIFI_ONLY, RoadCompassState.torMode(true, false, false));
		assertEquals(CHARGING_ONLY, RoadCompassState.torMode(true, true, true));
		assertEquals(WIFI_AND_CHARGING, RoadCompassState.torMode(
				true, false, true));
	}
}
