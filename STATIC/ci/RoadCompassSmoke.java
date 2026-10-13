import org.briarproject.briar.android.settings.RoadCompassState;
import org.briarproject.briar.android.settings.RoadCompassState.Mode;

public class RoadCompassSmoke {
    private static int checked = 0;

    private static void require(Mode actual, Mode expected) {
        checked++;
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        require(RoadCompassState.localMode(null), Mode.LOADING);
        require(RoadCompassState.localMode(true), Mode.ENABLED);
        require(RoadCompassState.localMode(false), Mode.DISABLED);
        Boolean[] values = {null, false, true};
        for (Boolean enabled : values) {
            for (Boolean mobile : values) {
                for (Boolean charging : values) {
                    Mode expected;
                    if (enabled == null) expected = Mode.LOADING;
                    else if (!enabled) expected = Mode.DISABLED;
                    else if (mobile == null || charging == null) expected = Mode.LOADING;
                    else if (!mobile && charging) expected = Mode.WIFI_AND_CHARGING;
                    else if (!mobile) expected = Mode.WIFI_ONLY;
                    else if (charging) expected = Mode.CHARGING_ONLY;
                    else expected = Mode.ENABLED;
                    require(RoadCompassState.torMode(enabled, mobile, charging),
                            expected);
                }
            }
        }
        System.out.println("PASS: " + checked + " Road Compass state witnesses");
    }
}
