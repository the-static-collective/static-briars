import org.briarproject.briar.android.settings.NetworkWitnessState;

public class NetworkWitnessSmoke {

    private static int assertions = 0;
    private static void check(boolean truth) {
        assertions++;
        if (!truth) throw new AssertionError("Network witness mismatch at " + assertions);
    }

    public static void main(String[] args) {
        for (NetworkWitnessState.Mode expected : NetworkWitnessState.Mode.values()) {
            String state = expected == NetworkWitnessState.Mode.UNAVAILABLE
                    ? null : expected.name();
            for (int count : new int[]{0, 1, 5}) {
                NetworkWitnessState.Road observed =
                        NetworkWitnessState.fromPlugin(state, count);
                check(observed.mode == expected);
                check(observed.connectedContacts == count);
                check(observed.hasConnectedContacts() == (count > 0));
            }
        }
        try {
            NetworkWitnessState.fromPlugin("ACTIVE", -1);
            throw new AssertionError("Accepted invalid count");
        } catch (IllegalArgumentException expected) {
            assertions++;
        }
        NetworkWitnessState.Snapshot s = new NetworkWitnessState.Snapshot(
                NetworkWitnessState.fromPlugin("DISABLED", 0),
                NetworkWitnessState.fromPlugin("ACTIVE", 2),
                NetworkWitnessState.fromPlugin(null, 0));
        check(s.wifi.hasConnectedContacts());
        check(s.tor.mode == NetworkWitnessState.Mode.UNAVAILABLE);
        System.out.println("PASS: " + assertions + " runtime/peer witness checks");
    }
}
