package org.briarproject.briar.android.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NetworkWitnessStateTest {

    @Test
    public void pluginModesRemainDistinctFromConnectedPeers() {
        for (NetworkWitnessState.Mode mode : NetworkWitnessState.Mode.values()) {
            String key = mode == NetworkWitnessState.Mode.UNAVAILABLE ? null : mode.name();
            NetworkWitnessState.Road empty = NetworkWitnessState.fromPlugin(key, 0);
            assertEquals(mode, empty.mode);
            assertFalse(empty.hasConnectedContacts());
            NetworkWitnessState.Road connected = NetworkWitnessState.fromPlugin(key, 2);
            assertEquals(mode, connected.mode);
            assertEquals(2, connected.connectedContacts);
            assertTrue(connected.hasConnectedContacts());
        }
    }

    @Test
    public void invalidPeerCountIsRejected() {
        try {
            NetworkWitnessState.fromPlugin("ACTIVE", -1);
            fail("Negative peer counts must be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("contacts", expected.getMessage());
        }
    }

    @Test
    public void snapshotDoesNotPromoteRuntimeToDelivery() {
        NetworkWitnessState.Snapshot s = new NetworkWitnessState.Snapshot(
                NetworkWitnessState.fromPlugin("ACTIVE", 0),
                NetworkWitnessState.fromPlugin("INACTIVE", 0),
                NetworkWitnessState.fromPlugin("ACTIVE", 1));
        assertFalse(s.bluetooth.hasConnectedContacts());
        assertEquals(NetworkWitnessState.Mode.INACTIVE, s.wifi.mode);
        assertTrue(s.tor.hasConnectedContacts());
    }
}
