package br.ufg.emc.termografia;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;

public abstract class Preferences {
    private static final String initialized = "preferences_initialized";

    // FrameProcessor preferences
    public static final Entry<String> colorPalette = new Entry<>("color_palette", RenderedImage.Palette.Iron.toString());
    public static final Entry<Float> emissivity = new Entry<>("emissivity", FrameProcessor.EMISSIVITY_GLOSSY);
    public static final Entry<String> imageType = new Entry<>("image_type", RenderedImage.ImageType.BlendedMSXRGBA8888Image.toString());
    public static final Entry<Float> msxDistance = new Entry<>("msx_distance", 1f);

    // Device preferences
    public static final Entry<Boolean> automaticTuning = new Entry<>("auto_tuning", true);

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        // TODO: Avaliar se é necessário inicializar as preferencias
//        if (preferences.getBoolean(initialized, false)) setDefaultValues(preferences);
        return preferences;
    }

    private static void setDefaultValues(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putBoolean(initialized, true);
        editor.putString(colorPalette.key, colorPalette.defaultValue);
        editor.putFloat(emissivity.key, emissivity.defaultValue);
        editor.putString(imageType.key, imageType.defaultValue);
        editor.putFloat(msxDistance.key, msxDistance.defaultValue);
        editor.putBoolean(automaticTuning.key, automaticTuning.defaultValue);
        editor.apply();
    }

    public static class Entry<T> {
        public final T defaultValue;
        public final String key;

        public Entry(String key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }
}
