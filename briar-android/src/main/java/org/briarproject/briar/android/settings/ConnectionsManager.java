package org.briarproject.briar.android.settings;

import org.briarproject.bramble.api.connection.ConnectionRegistry;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.PluginManager;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.TorConstants;
import org.briarproject.bramble.api.settings.Settings;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static org.briarproject.bramble.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static org.briarproject.bramble.api.plugin.TorConstants.DEFAULT_PREF_TOR_MOBILE;
import static org.briarproject.bramble.api.plugin.TorConstants.DEFAULT_PREF_TOR_NETWORK;
import static org.briarproject.bramble.api.plugin.TorConstants.DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING;
import static org.briarproject.bramble.api.plugin.TorConstants.PREF_TOR_MOBILE;
import static org.briarproject.bramble.api.plugin.TorConstants.PREF_TOR_NETWORK;
import static org.briarproject.bramble.api.plugin.TorConstants.PREF_TOR_ONLY_WHEN_CHARGING;
import static org.briarproject.briar.android.settings.SettingsViewModel.BT_NAMESPACE;
import static org.briarproject.briar.android.settings.SettingsViewModel.TOR_NAMESPACE;
import static org.briarproject.briar.android.settings.SettingsViewModel.WIFI_NAMESPACE;

@NotNullByDefault
class ConnectionsManager {

	final ConnectionsStore btStore;
	final ConnectionsStore wifiStore;
	final ConnectionsStore torStore;

	private final PluginManager pluginManager;
	private final ConnectionRegistry connectionRegistry;
	private final MutableLiveData<NetworkWitnessState.Snapshot> roadRuntime = new MutableLiveData<>();

	private final MutableLiveData<Boolean> btEnabled = new MutableLiveData<>();
	private final MutableLiveData<Boolean> wifiEnabled =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> torEnabled = new MutableLiveData<>();
	private final MutableLiveData<String> torNetwork = new MutableLiveData<>();
	private final MutableLiveData<Boolean> torMobile = new MutableLiveData<>();
	private final MutableLiveData<Boolean> torCharging =
			new MutableLiveData<>();

	ConnectionsManager(SettingsManager settingsManager,
			Executor dbExecutor, PluginManager pluginManager,
			ConnectionRegistry connectionRegistry) {
		this.pluginManager = pluginManager;
		this.connectionRegistry = connectionRegistry;
		btStore =
				new ConnectionsStore(settingsManager, dbExecutor, BT_NAMESPACE);
		wifiStore = new ConnectionsStore(settingsManager, dbExecutor,
				WIFI_NAMESPACE);
		torStore = new ConnectionsStore(settingsManager, dbExecutor,
				TOR_NAMESPACE);
	}

	void updateBtSetting(Settings btSettings) {
		btEnabled.postValue(btSettings.getBoolean(PREF_PLUGIN_ENABLE,
				BluetoothConstants.DEFAULT_PREF_PLUGIN_ENABLE));
	}

	void updateWifiSettings(Settings wifiSettings) {
		wifiEnabled.postValue(wifiSettings.getBoolean(PREF_PLUGIN_ENABLE,
				LanTcpConstants.DEFAULT_PREF_PLUGIN_ENABLE));
	}

	void updateTorSettings(Settings settings) {
		torEnabled.postValue(settings.getBoolean(PREF_PLUGIN_ENABLE,
				TorConstants.DEFAULT_PREF_PLUGIN_ENABLE));

		int torNetworkSetting = settings.getInt(PREF_TOR_NETWORK,
				DEFAULT_PREF_TOR_NETWORK);
		torNetwork.postValue(Integer.toString(torNetworkSetting));

		torMobile.postValue(settings.getBoolean(PREF_TOR_MOBILE,
				DEFAULT_PREF_TOR_MOBILE));

		torCharging.postValue(settings.getBoolean(PREF_TOR_ONLY_WHEN_CHARGING,
				DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING));
	}


	/**
	 * Sample the real Briar transport runtimes and registry, not an external
	 * ping. Caller schedules this off the UI thread. Counts are not identities
	 * and snapshots are only current at observation time.
	 */
	void refreshRoadRuntime() {
		roadRuntime.postValue(new NetworkWitnessState.Snapshot(
				road(BluetoothConstants.ID),
				road(LanTcpConstants.ID),
				road(TorConstants.ID)));
	}

	private NetworkWitnessState.Road road(TransportId transportId) {
		Plugin plugin = pluginManager.getPlugin(transportId);
		String state = plugin == null ? null : plugin.getState().name();
		int connectedContacts =
				connectionRegistry.getConnectedContacts(transportId).size();
		return NetworkWitnessState.fromPlugin(state, connectedContacts);
	}

	LiveData<NetworkWitnessState.Snapshot> roadRuntime() {
		return roadRuntime;
	}

	LiveData<Boolean> btEnabled() {
		return btEnabled;
	}

	LiveData<Boolean> wifiEnabled() {
		return wifiEnabled;
	}

	LiveData<Boolean> torEnabled() {
		return torEnabled;
	}

	LiveData<String> torNetwork() {
		return torNetwork;
	}

	LiveData<Boolean> torMobile() {
		return torMobile;
	}

	LiveData<Boolean> torCharging() {
		return torCharging;
	}

}
