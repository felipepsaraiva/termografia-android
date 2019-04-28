package br.ufg.emc.termografia.ui;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.NumberTextWatcher;

public class DiagnosisSettingsFragment extends PreferenceFragmentCompat {

    public DiagnosisSettingsFragment() { /* Required empty public constructor */ }

    public static DiagnosisSettingsFragment newInstance() {
        return new DiagnosisSettingsFragment();
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
}
