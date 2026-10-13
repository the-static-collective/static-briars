package org.briarproject.briar.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.briarproject.briar.R;
import org.briarproject.briar.android.mailbox.MailboxActivity;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.util.Map;

import javax.inject.Inject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.os.Build.VERSION.SDK_INT;
import static org.briarproject.briar.android.AppModule.getAndroidComponent;
import static org.briarproject.briar.android.settings.SettingsActivity.enableAndPersist;
import static org.briarproject.briar.android.util.PermissionUtils.areBluetoothPermissionsGranted;
import static org.briarproject.briar.android.util.PermissionUtils.requestBluetoothPermissions;
import static org.briarproject.briar.android.util.PermissionUtils.showDenialDialog;
import static org.briarproject.briar.android.util.PermissionUtils.showRationale;
import static org.briarproject.briar.android.util.PermissionUtils.wasGrantedBluetoothPermissions;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ConnectionsFragment extends PreferenceFragmentCompat {

	static final String PREF_KEY_BLUETOOTH = "pref_key_bluetooth";
	static final String PREF_KEY_WIFI = "pref_key_wifi";
	static final String PREF_KEY_TOR_ENABLE = "pref_key_tor_enable";
	static final String PREF_KEY_TOR_NETWORK = "pref_key_tor_network";
	static final String PREF_KEY_TOR_MOBILE_DATA =
			"pref_key_tor_mobile_data";
	static final String PREF_KEY_TOR_ONLY_WHEN_CHARGING =
			"pref_key_tor_only_when_charging";

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private SettingsViewModel viewModel;
	private ConnectionsManager connectionsManager;

	private SwitchPreferenceCompat enableBluetooth;
	private SwitchPreferenceCompat enableWifi;
	private SwitchPreferenceCompat enableTor;
	private ListPreference torNetwork;
	private SwitchPreferenceCompat torMobile;
	private SwitchPreferenceCompat torOnlyWhenCharging;
	private Preference compassBluetooth;
	private Preference compassWifi;
	private Preference compassTor;
	private Preference witnessBluetooth;
	private Preference witnessWifi;
	private Preference witnessTor;

	@RequiresApi(31)
	private final ActivityResultLauncher<String[]> requestPermissionLauncher =
			registerForActivityResult(new RequestMultiplePermissions(),
					this::handleBtPermissionResult);

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		getAndroidComponent(context).inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(SettingsViewModel.class);
		connectionsManager = viewModel.connectionsManager;
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_connections);

		enableBluetooth = findPreference(PREF_KEY_BLUETOOTH);
		enableWifi = findPreference(PREF_KEY_WIFI);
		enableTor = findPreference(PREF_KEY_TOR_ENABLE);
		torNetwork = findPreference(PREF_KEY_TOR_NETWORK);
		torMobile = findPreference(PREF_KEY_TOR_MOBILE_DATA);
		torOnlyWhenCharging = findPreference(PREF_KEY_TOR_ONLY_WHEN_CHARGING);


		compassBluetooth = findPreference("pref_key_static_compass_bluetooth");
		compassWifi = findPreference("pref_key_static_compass_wifi");
		compassTor = findPreference("pref_key_static_compass_tor");
		witnessBluetooth = findPreference("pref_key_static_witness_bluetooth");
		witnessWifi = findPreference("pref_key_static_witness_wifi");
		witnessTor = findPreference("pref_key_static_witness_tor");
		Preference guide = findPreference("pref_key_static_compass_explain");
		guide.setOnPreferenceClickListener(preference -> {
			new MaterialAlertDialogBuilder(requireContext())
					.setTitle(R.string.static_compass_explain_title)
					.setMessage(R.string.static_compass_explain_body)
					.setPositiveButton(android.R.string.ok, null)
					.show();
			return true;
		});
		Preference mailbox = findPreference("pref_key_static_compass_mailbox");
		mailbox.setOnPreferenceClickListener(preference -> {
			startActivity(new Intent(requireContext(), MailboxActivity.class));
			return true;
		});

		torNetwork.setSummaryProvider(viewModel.torSummaryProvider);

		if (SDK_INT >= 31) {
			enableBluetooth.setOnPreferenceChangeListener((p, value) -> {
				FragmentActivity ctx = requireActivity();
				if (areBluetoothPermissionsGranted(ctx)) {
					return true;
				} else if (shouldShowRequestPermissionRationale(
						BLUETOOTH_CONNECT)) {
					showRationale(ctx, R.string.permission_bluetooth_title,
							R.string.permission_bluetooth_body,
							this::requestBtPermissions);
					// we don't update the preference directly,
					// but do it via the launcher, if we got the permissions
					return false;
				} else {
					requestBtPermissions();
					return false;
				}
			});
		}
		enableBluetooth.setPreferenceDataStore(connectionsManager.btStore);
		enableWifi.setPreferenceDataStore(connectionsManager.wifiStore);
		enableTor.setPreferenceDataStore(connectionsManager.torStore);
		torNetwork.setPreferenceDataStore(connectionsManager.torStore);
		torMobile.setPreferenceDataStore(connectionsManager.torStore);
		torOnlyWhenCharging.setPreferenceDataStore(connectionsManager.torStore);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getRoadRuntime().observe(getViewLifecycleOwner(), snapshot -> {
			witnessBluetooth.setSummary(runtimeSummary(snapshot.bluetooth));
			witnessWifi.setSummary(runtimeSummary(snapshot.wifi));
			witnessTor.setSummary(runtimeSummary(snapshot.tor));
		});

		// persist changes after setting initial value and enabling
		LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
		connectionsManager.btEnabled().observe(lifecycleOwner, enabled -> {
			enableBluetooth.setChecked(enabled);
			enableAndPersist(enableBluetooth);
			updateRoadCompass();
		});
		connectionsManager.wifiEnabled().observe(lifecycleOwner, enabled -> {
			enableWifi.setChecked(enabled);
			enableAndPersist(enableWifi);
			updateRoadCompass();
		});
		connectionsManager.torEnabled().observe(lifecycleOwner, enabled -> {
			enableTor.setChecked(enabled);
			enableAndPersist(enableTor);
			updateRoadCompass();
		});
		connectionsManager.torNetwork().observe(lifecycleOwner, value -> {
			torNetwork.setValue(value);
			enableAndPersist(torNetwork);
		});
		connectionsManager.torMobile().observe(lifecycleOwner, enabled -> {
			torMobile.setChecked(enabled);
			enableAndPersist(torMobile);
			updateRoadCompass();
		});
		connectionsManager.torCharging().observe(lifecycleOwner, enabled -> {
			torOnlyWhenCharging.setChecked(enabled);
			enableAndPersist(torOnlyWhenCharging);
			updateRoadCompass();
		});
	}

	/**
	 * Read-only summaries of local preferences, never live transport health.
	 * Tor/network reachability and message delivery cannot be inferred here.
	 */
	private void updateRoadCompass() {
		compassBluetooth.setSummary(localSummary(
				RoadCompassState.localMode(connectionsManager.btEnabled().getValue())));
		compassWifi.setSummary(localSummary(
				RoadCompassState.localMode(connectionsManager.wifiEnabled().getValue())));
		RoadCompassState.Mode torMode = RoadCompassState.torMode(
				connectionsManager.torEnabled().getValue(),
				connectionsManager.torMobile().getValue(),
				connectionsManager.torCharging().getValue());
		compassTor.setSummary(torSummary(torMode));
	}

	private int localSummary(RoadCompassState.Mode mode) {
		switch (mode) {
			case DISABLED:
				return R.string.static_compass_state_off;
			case ENABLED:
				return R.string.static_compass_state_on;
			default:
				return R.string.static_compass_state_loading;
		}
	}

	private int torSummary(RoadCompassState.Mode mode) {
		switch (mode) {
			case DISABLED:
				return R.string.static_compass_state_off;
			case ENABLED:
				return R.string.static_compass_state_on;
			case WIFI_ONLY:
				return R.string.static_compass_tor_wifi_only;
			case CHARGING_ONLY:
				return R.string.static_compass_tor_charging_only;
			case WIFI_AND_CHARGING:
				return R.string.static_compass_tor_wifi_and_charging;
			default:
				return R.string.static_compass_state_loading;
		}
	}

	private String runtimeSummary(NetworkWitnessState.Road road) {
		int res;
		switch (road.mode) {
			case UNAVAILABLE:
				res = R.string.static_witness_unavailable;
				break;
			case STARTING_STOPPING:
				res = R.string.static_witness_starting;
				break;
			case DISABLED:
				res = R.string.static_witness_disabled;
				break;
			case ENABLING:
				res = R.string.static_witness_enabling;
				break;
			case ACTIVE:
				res = R.string.static_witness_active;
				break;
			case INACTIVE:
				res = R.string.static_witness_inactive;
				break;
			default:
				throw new AssertionError(road.mode);
		}
		return getString(res, road.connectedContacts);
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.refreshRoadRuntime();
		requireActivity().setTitle(R.string.network_settings_title);
	}

	@RequiresApi(31)
	private void requestBtPermissions() {
		requestBluetoothPermissions(requestPermissionLauncher);
	}

	@RequiresApi(31)
	private void handleBtPermissionResult(Map<String, Boolean> grantedMap) {
		if (wasGrantedBluetoothPermissions(requireActivity(), grantedMap)) {
			enableBluetooth.setChecked(true);
		} else {
			showDenialDialog(requireActivity(),
					R.string.permission_bluetooth_title,
					R.string.permission_bluetooth_denied_body);
		}
	}
}
