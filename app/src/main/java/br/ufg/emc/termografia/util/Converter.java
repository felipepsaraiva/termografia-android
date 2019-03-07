package br.ufg.emc.termografia.util;

import android.content.Context;

import br.ufg.emc.termografia.R;

public abstract class Converter {
    public static double fromMilli(int milli) {
        return (double)milli / 100.0;
    }

    public static double fromMilli(double milli) {
        return milli / 100.0;
    }

    public static double kelvinToCelsius(int k) {
        return k - 273.15;
    }

    public static double kelvinToCelsius(double k) {
        return k - 273.15;
    }

    public static double milliKelvinToCelsius(int mK) {
        return (double)(mK - 27315) / 100.0;
    }

    public static double milliKelvinToCelsius(double mK) {
        return (mK - 27315) / 100.0;
    }

    public static float msxDistance(Context context, int percentage) {
        int min = context.getResources().getInteger(R.integer.flirsettings_msxdistance_min);
        int max = context.getResources().getInteger(R.integer.flirsettings_msxdistance_max);
        float factor = (float)percentage / 100f;
        return (float)(max - min) * factor / 100f;
    }
}
