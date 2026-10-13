package org.briarproject.briar.android.settings;

import java.util.Objects;

/**
 * Local-only observations about transport runtimes and connected peers.
 * No contact identity, address, message content, or packet counters.
 *
 * ACTIVE means Briar's plugin can make or receive connections, not that any
 * particular remote person or Internet destination can be reached.
 */
public final class NetworkWitnessState {

    public enum Mode {
        UNAVAILABLE, STARTING_STOPPING, DISABLED, ENABLING,
        ACTIVE, INACTIVE
    }

    public static final class Road {
        public final Mode mode;
        public final int connectedContacts;

        public Road(Mode mode, int connectedContacts) {
            this.mode = Objects.requireNonNull(mode);
            if (connectedContacts < 0) throw new IllegalArgumentException("contacts");
            this.connectedContacts = connectedContacts;
        }

        public boolean hasConnectedContacts() {
            return connectedContacts > 0;
        }
    }

    public static final class Snapshot {
        public final Road bluetooth;
        public final Road wifi;
        public final Road tor;

        public Snapshot(Road bluetooth, Road wifi, Road tor) {
            this.bluetooth = Objects.requireNonNull(bluetooth);
            this.wifi = Objects.requireNonNull(wifi);
            this.tor = Objects.requireNonNull(tor);
        }
    }

    private NetworkWitnessState() {
    }

    /**
     * A null plugin yields UNAVAILABLE, not an invented DISABLED value.
     * Callers must supply Briar's own plugin state string and current registry
     * contact count, without external network probing.
     */
    public static Road fromPlugin(String state, int connectedContacts) {
        Mode mode = state == null ? Mode.UNAVAILABLE : Mode.valueOf(state);
        return new Road(mode, connectedContacts);
    }
}
