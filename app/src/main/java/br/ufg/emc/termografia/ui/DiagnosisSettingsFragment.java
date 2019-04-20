package br.ufg.emc.termografia.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.NumberTextWatcher;
import br.ufg.emc.termografia.viewmodel.DiagnosisViewModel;

public class DiagnosisSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private DiagnosisViewModel diagnosisViewModel;

    public DiagnosisSettingsFragment() { /* Required empty public constructor */ }

    public static DiagnosisSettingsFragment newInstance() {
        return new DiagnosisSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        diagnosisViewModel = ViewModelProviders.of(requireActivity()).get(DiagnosisViewModel.class);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.diagnosis_settings, rootKey);

        EditTextPreference preference = findPreference(getString(R.string.diagnosissettings_loading_key));
        preference.setOnBindEditTextListener((EditText editText) -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.addTextChangedListener(new NumberTextWatcher(3));
            editText.setSelection(editText.getText().length());
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (getString(R.string.diagnosissettings_material_key).equals(key))
            diagnosisViewModel.setMaterial(preferences.getString(key, getString(R.string.diagnosissettings_material_default)));
        else if (getString(R.string.diagnosissettings_loading_key).equals(key))
            diagnosisViewModel.setLoading(preferences.getString(key, getString(R.string.diagnosissettings_loading_default)));
    }
}
