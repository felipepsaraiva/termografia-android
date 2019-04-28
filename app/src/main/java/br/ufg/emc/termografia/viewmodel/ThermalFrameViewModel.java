package br.ufg.emc.termografia.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.Preferences;

public class ThermalFrameViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Application app;
    private SharedPreferences preferences;

    private MutableLiveData<String> imageType = new MutableLiveData<>();
    private MutableLiveData<Integer> msxDistance = new MutableLiveData<>();
    private MutableLiveData<String> palette = new MutableLiveData<>();
    private MutableLiveData<String> emissivity = new MutableLiveData<>();
    private MutableLiveData<String> framePath = new MutableLiveData<>();

    public ThermalFrameViewModel(@NonNull Application application) {
        super(application);
        app = application;
        preferences = Preferences.getPreferences(app);

        String key, defaultString;
        int defaultInt;

        key = app.getString(R.string.flirsettings_imagetype_key);
        defaultString = app.getString(R.string.flirsettings_imagetype_default);
        this.imageType.setValue(preferences.getString(key, defaultString));

        key = app.getString(R.string.flirsettings_msxdistance_key);
        defaultInt = app.getResources().getInteger(R.integer.flirsettings_msxdistance_default);
        this.msxDistance.setValue(preferences.getInt(key, defaultInt));

        key = app.getString(R.string.flirsettings_palette_key);
        defaultString = app.getString(R.string.flirsettings_palette_default);
        this.palette.setValue(preferences.getString(key, defaultString));

        key = app.getString(R.string.flirsettings_emissivity_key);
        defaultString = app.getString(R.string.flirsettings_emissivity_default);
        this.emissivity.setValue(preferences.getString(key, defaultString));

        this.framePath.setValue(null);

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (app.getString(R.string.flirsettings_msxdistance_key).equals(key))
            setMsxDistance(preferences.getInt(key, app.getResources().getInteger(R.integer.flirsettings_msxdistance_default)));
        else if (app.getString(R.string.flirsettings_palette_key).equals(key))
            setPalette(preferences.getString(key, app.getString(R.string.flirsettings_palette_default)));
        else if (app.getString(R.string.flirsettings_emissivity_key).equals(key))
            setEmissivity(preferences.getString(key, app.getString(R.string.flirsettings_emissivity_default)));
        else if (app.getString(R.string.flirsettings_imagetype_key).equals(key))
            setImageType(preferences.getString(key, app.getString(R.string.flirsettings_imagetype_default)));
    }

    private void setImageType(String imageType) {
        this.imageType.setValue(imageType);
    }

    private void setMsxDistance(int msxDistance) {
        this.msxDistance.setValue(msxDistance);
    }

    private void setPalette(String palette) {
        this.palette.setValue(palette);
    }

    private void setEmissivity(String emissivity) {
        this.emissivity.setValue(emissivity);
    }

    public void setFramePath(String path) {
        this.framePath.setValue(path);
    }

    public LiveData<String> getImageType() {
        return imageType;
    }

    public LiveData<Integer> getMsxDistance() {
        return msxDistance;
    }

    public LiveData<String> getPalette() {
        return palette;
    }

    public LiveData<String> getEmissivity() {
        return emissivity;
    }

    public LiveData<String> getFramePath() {
        return framePath;
    }
}
