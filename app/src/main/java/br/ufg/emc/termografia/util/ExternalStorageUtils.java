package br.ufg.emc.termografia.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.ufg.emc.termografia.R;

public abstract class ExternalStorageUtils {
    private static final String LOG_TAG = ExternalStorageUtils.class.getSimpleName();

    public static final String FILENAME_SEPARATOR = "_";
    public static final String TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss";

    public static final String FRAME_PREFIX = "FLIR";
    public static final String FRAME_EXTENSION = ".jpg";

    public static File getAppDirectory(Context context) throws IOException {
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(pictures, context.getString(R.string.app_name));

        if (!appDir.exists()) {
            boolean created = appDir.mkdirs();
            if (!created) throw new IOException("App directory could not be created");
        }

        Log.i(LOG_TAG, "App directory created: " + appDir.getAbsolutePath());
        return appDir;
    }

    public static String getNewFrameName() {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault()).format(new Date());
        return FRAME_PREFIX + FILENAME_SEPARATOR + timeStamp + FRAME_EXTENSION;
    }

    public static File createNewFrameFile(Context context) throws IOException {
        File frameFile = new File(getAppDirectory(context), getNewFrameName());
        if (!frameFile.createNewFile())
            throw new IOException("File \"" + frameFile.getAbsolutePath() + "\" could not be created");

        Log.i(LOG_TAG, "New file created: " + frameFile.getAbsolutePath());
        return frameFile;
    }
}
