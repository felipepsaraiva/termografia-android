package br.ufg.emc.termografia.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.Preferences;

public class DiagnosisViewModel extends AndroidViewModel {
    private MutableLiveData<String> material = new MutableLiveData<>();
    private MutableLiveData<String> loading = new MutableLiveData<>();

    public DiagnosisViewModel(@NonNull Application app) {
        super(app);
        SharedPreferences preferences = Preferences.getPreferences(app);

        String key, defaultString;

        key = app.getString(R.string.diagnosissettings_material_key);
        defaultString = app.getString(R.string.diagnosissettings_material_default);
        this.material.setValue(preferences.getString(key, defaultString));

        key = app.getString(R.string.diagnosissettings_loading_key);
        defaultString = app.getString(R.string.diagnosissettings_loading_default);
        this.loading.setValue(preferences.getString(key, defaultString));
    }

    public void setMaterial(String material) {
        this.material.setValue(material);
    }

    public void setLoading(String loading) {
        this.loading.setValue(loading);
    }

    public LiveData<String> getMaterial() {
        return material;
    }

    public LiveData<String> getLoading() {
        return loading;
    }
}
