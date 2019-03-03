package br.ufg.emc.termografia.util;

public abstract class Converter {
    public static double fromMilli(int milli) {
        return (double)milli / 100.0;
    }

    public static double fromMilli(double milli) {
        return milli / 100.0;
    }

    public static double kelvintoCelsius(int k) {
        return k - 273.15;
    }

    public static double kelvintoCelsius(double k) {
        return k - 273.15;
    }

    public static double milliKelvinToCelsius(int mK) {
        return (double)(mK - 27315) / 100.0;
    }

    public static double milliKelvinToCelsius(double mK) {
        return (mK - 27315) / 100.0;
    }
}
