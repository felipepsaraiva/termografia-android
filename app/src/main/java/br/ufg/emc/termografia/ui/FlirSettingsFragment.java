package br.ufg.emc.termografia.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Objects;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

public class FlirSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ThermalFrameViewModel frameViewModel;

    public FlirSettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Objects.requireNonNull(getActivity());
        frameViewModel = ViewModelProviders.of(getActivity()).get(ThermalFrameViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.flir_device_settings, rootKey);
        findPreference(getString(R.string.flirsettings_imagetype_key))
                .setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        findPreference(getString(R.string.flirsettings_palette_key))
                .setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        findPreference(getString(R.string.flirsettings_emissivity_key))
                .setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        // TODO: Adicionar defaultValues
        if (getString(R.string.flirsettings_msxdistance_key).equals(key))
            frameViewModel.setMsxDistance(preferences.getInt(key, 0));
        else if (getString(R.string.flirsettings_palette_key).equals(key))
            frameViewModel.setPalette(preferences.getString(key, null));
        else if (getString(R.string.flirsettings_emissivity_key).equals(key))
            frameViewModel.setEmissivity(preferences.getString(key, null));
        else if (getString(R.string.flirsettings_imagetype_key).equals(key))
            frameViewModel.setImageType(preferences.getString(key, null));
    }
}
