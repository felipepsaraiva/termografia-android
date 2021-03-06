package br.ufg.emc.termografia.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.diagnosis.BushingDiagnoser;
import br.ufg.emc.termografia.util.Preferences;

public class DiagnosisViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Application app;
    private SharedPreferences preferences;

    private MutableLiveData<String> material = new MutableLiveData<>();
    private MutableLiveData<String> loading = new MutableLiveData<>();
    private MutableLiveData<BushingDiagnoser> diagnoser = new MutableLiveData<>();

    public DiagnosisViewModel(@NonNull Application application) {
        super(application);
        app = application;
        preferences = Preferences.getPreferences(app);

        String key, defaultString;

        key = app.getString(R.string.diagnosissettings_material_key);
        defaultString = app.getString(R.string.diagnosissettings_material_default);
        this.material.setValue(preferences.getString(key, defaultString));

        key = app.getString(R.string.diagnosissettings_loading_key);
        defaultString = app.getString(R.string.diagnosissettings_loading_default);
        this.loading.setValue(preferences.getString(key, defaultString));

        updateDiagnoser();

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (app.getString(R.string.diagnosissettings_material_key).equals(key))
            setMaterial(preferences.getString(key, app.getString(R.string.diagnosissettings_material_default)));
        else if (app.getString(R.string.diagnosissettings_loading_key).equals(key))
            setLoading(preferences.getString(key, app.getString(R.string.diagnosissettings_loading_default)));
    }

    private void setMaterial(String material) {
        this.material.setValue(material);
        updateDiagnoser();
    }

    private void setLoading(String loading) {
        this.loading.setValue(loading);
        updateDiagnoser();
    }

    private void updateDiagnoser() {
        double loadingVal = Double.parseDouble(loading.getValue());
        this.diagnoser.setValue(new BushingDiagnoser(loadingVal, material.getValue()));
    }

    public LiveData<String> getMaterial() {
        return material;
    }

    public LiveData<String> getLoading() {
        return loading;
    }

    public LiveData<BushingDiagnoser> getDiagnoser() {
        return diagnoser;
    }
}
