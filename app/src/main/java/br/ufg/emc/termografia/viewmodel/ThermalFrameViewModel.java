package br.ufg.emc.termografia.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import com.flir.flironesdk.RenderedImage;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import br.ufg.emc.termografia.Preferences;

public class ThermalFrameViewModel extends AndroidViewModel {
    private SharedPreferences preferences;

    private MutableLiveData<RenderedImage.ImageType> imageType = new MutableLiveData<>();
    private MutableLiveData<Float> msxDistance = new MutableLiveData<>();
    private MutableLiveData<RenderedImage.Palette> palette = new MutableLiveData<>();
    private MutableLiveData<Float> emissivity = new MutableLiveData<>();
    private MutableLiveData<String> framePath = new MutableLiveData<>();

    public ThermalFrameViewModel(@NonNull Application application) {
        super(application);
        this.preferences = Preferences.getSharedPreferences(application);

        RenderedImage.ImageType imageType = RenderedImage.ImageType.valueOf(
                preferences.getString(Preferences.imageType.key, Preferences.imageType.defaultValue));
        RenderedImage.Palette palette = RenderedImage.Palette.valueOf(
                preferences.getString(Preferences.colorPalette.key, Preferences.colorPalette.defaultValue));

        float msxDistance = preferences.getFloat(Preferences.msxDistance.key, Preferences.msxDistance.defaultValue);
        float emissivity = preferences.getFloat(Preferences.emissivity.key, Preferences.emissivity.defaultValue);

        this.imageType.setValue(imageType);
        this.msxDistance.setValue(msxDistance);
        this.palette.setValue(palette);
        this.emissivity.setValue(emissivity);
        this.framePath.setValue(null);
    }

    public void setImageType(RenderedImage.ImageType imageType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.imageType.key, imageType.toString());
        editor.apply();
        this.imageType.setValue(imageType);
    }

    public void setMsxDistance(float msxDistance) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Preferences.msxDistance.key, msxDistance);
        editor.apply();
        this.msxDistance.setValue(msxDistance);
    }

    public void setPalette(RenderedImage.Palette palette) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.colorPalette.key, palette.toString());
        editor.apply();
        this.palette.setValue(palette);
    }

    public void setEmissivity(float emissivity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Preferences.emissivity.key, emissivity);
        editor.apply();
        this.emissivity.setValue(emissivity);
    }

    public void setFramePath(String path) {
        this.framePath.setValue(path);
    }

    public LiveData<RenderedImage.ImageType> getImageType() {
        return imageType;
    }

    public LiveData<Float> getMsxDistance() {
        return msxDistance;
    }

    public LiveData<RenderedImage.Palette> getPalette() {
        return palette;
    }

    public LiveData<Float> getEmissivity() {
        return emissivity;
    }

    public LiveData<String> getFramePath() {
        return framePath;
    }
}
