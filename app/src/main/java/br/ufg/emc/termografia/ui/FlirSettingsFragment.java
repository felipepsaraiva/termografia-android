package br.ufg.emc.termografia.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.flir.flironesdk.Device;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

// TODO: Bloquear a preferência msx_distance quando o image_type for diferente de BlendedMSX

public class FlirSettingsFragment extends PreferenceFragmentCompat {
    private static final String LOG_TAG = FlirSettingsFragment.class.getSimpleName();

    private FlirProxy flir;

    public FlirSettingsFragment() { /* Required empty public constructor */ }

    public static FlirSettingsFragment newInstance(@Nullable FlirProxy proxy) {
        FlirSettingsFragment instance = new FlirSettingsFragment();
        instance.setFlirProxy(proxy);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupObservers();
    }

    private void setupObservers() {
        if (flir == null) return;
        flir.getDeviceState().observe(this, (FlirProxy.DeviceState state) -> {
            String summary = getResources().getStringArray(R.array.flirsettings_devicestate_values)[state.ordinal()];
            findPreference(getString(R.string.flirsettings_devicestate_key)).setSummary(summary);
            findPreference(getString(R.string.flisettings_performtuning_key)).setVisible(state != FlirProxy.DeviceState.Disconnected);
        });
        flir.getBatteryChargingState().observe(this, (Device.BatteryChargingState state) -> {
            Preference preference = findPreference(getString(R.string.flirsettings_batterystate_key));
            if (state != null) {
                String summary = getResources().getStringArray(R.array.flirsettings_batterystate_values)[state.ordinal()];
                preference.setSummary(summary);
                preference.setVisible(true);
            } else {
                preference.setVisible(false);
            }
        });
        flir.getBatteryPercentage().observe(this, (Byte percentage) -> {
            Preference preference = findPreference(getString(R.string.flirsettings_batterypercentage_key));
            if (percentage != null) {
                String summary = getResources().getString(R.string.flirsettings_batterypercentage_summary, percentage);
                preference.setSummary(summary);
                preference.setVisible(true);
            } else {
                preference.setVisible(false);
            }
        });
        flir.getLowerAccuracyBound().observe(this, (Float lowerAccuracy) -> {
            Preference preference = findPreference(getString(R.string.flirsettings_loweraccuracy_key));
            if (lowerAccuracy != null) {
                String summary = getResources().getString(R.string.flirsettings_loweraccuracy_summary, lowerAccuracy);
                preference.setSummary(summary);
                preference.setVisible(true);
            } else {
                preference.setVisible(false);
            }
        });
        flir.getUpperAccuracyBound().observe(this, (Float upperAccuracy) -> {
            Preference preference = findPreference(getString(R.string.flirsettings_upperaccuracy_key));
            if (upperAccuracy != null) {
                String summary = getResources().getString(R.string.flirsettings_upperaccuracy_summary, upperAccuracy);
                preference.setSummary(summary);
                preference.setVisible(true);
            } else {
                preference.setVisible(false);
            }
        });
        flir.getTuningState().observe(this, (Device.TuningState state) -> {
            Preference preference = findPreference(getString(R.string.flirsettings_tuningstate_key));
            if (state != null) {
                String summary = getResources().getStringArray(R.array.flirsettings_tuningstate_values)[state.ordinal()];
                preference.setSummary(summary);
                preference.setVisible(true);
            } else {
                preference.setVisible(false);
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.flir_device_settings, rootKey);

        if (flir != null) {
            findPreference(getString(R.string.flirsettings_category_device_key)).setVisible(true);

            SeekBarPreference msxDistance = findPreference(getString(R.string.flirsettings_msxdistance_key));
            float initial = Converter.msxDistance(requireContext(), msxDistance.getValue());
            msxDistance.setSummary(getString(R.string.flirsettings_msxdistance_summary, initial));
            msxDistance.setOnPreferenceChangeListener((Preference preference, Object value) -> {
                float distance = Converter.msxDistance(requireContext(), (Integer) value);
                preference.setSummary(getString(R.string.flirsettings_msxdistance_summary, distance));
                return true;
            });

            findPreference(getString(R.string.flirsettings_automatictuning_key))
                    .setOnPreferenceChangeListener((Preference preference, Object enabled) -> {
                        if (flir != null) flir.setAutomaticTuning((Boolean) enabled);
                        return true;
                    });

            findPreference(getString(R.string.flisettings_performtuning_key))
                    .setOnPreferenceClickListener((Preference p) -> onTuningRequested());
        }
    }

    private boolean onTuningRequested() {
        if (flir != null && flir.performTuning()) {
            Toast.makeText(requireContext(), R.string.flirdevice_tuning_requested, Toast.LENGTH_SHORT).show();
            return true;
        }

        Toast.makeText(requireContext(), R.string.flirdevice_tuning_request_error, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setFlirProxy(FlirProxy proxy) {
        flir = proxy;
    }
}
