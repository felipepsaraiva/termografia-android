package br.ufg.emc.termografia.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;

import br.ufg.emc.termografia.R;

public abstract class Preferences {
    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getInitializedPreferences(Context context) {
        setDefaultValues(context, false);
        return getPreferences(context);
    }

    private static void setDefaultValues(Context context, boolean reset) {
        if (reset) getPreferences(context).edit().clear().apply();
        PreferenceManager.setDefaultValues(context, R.xml.flir_device_settings, reset);
    }
}
