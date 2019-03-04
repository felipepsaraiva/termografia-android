package br.ufg.emc.termografia.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import br.ufg.emc.termografia.BuildConfig;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.EnumSet;
import java.util.Set;

public class PreviewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = PreviewActivity.class.getSimpleName();

    private ThermalFrameViewModel frameViewModel;
    private FlirProxy flir;
    private FrameProcessor processor;

    private GLSurfaceView glSurfaceView;
    private BottomNavigationView bottomNavigationView;

    // Workaround on the issue of gray scale image on device connection
    private CountDownTimer paletteCountDown = new CountDownTimer(500, 500) {
        @Override public void onTick(long l) { }

        @Override
        public void onFinish() {
            processor.setImagePalette(frameViewModel.getPalette().getValue());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        processor = new FrameProcessor(this, (RenderedImage image) -> {}, null, true);

        glSurfaceView = findViewById(R.id.glsurfaceview_preview_previewscreen);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        glSurfaceView.setRenderer(processor);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        bottomNavigationView = findViewById(R.id.bottomnavigationview_preview_actions);
        bottomNavigationView.setSelectedItemId(R.id.menu_preview_capture);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        frameViewModel = ViewModelProviders.of(this).get(ThermalFrameViewModel.class);
        frameViewModel.getImageType().observe(this, (RenderedImage.ImageType imageType) -> processor.setGLOutputMode(imageType));
        frameViewModel.getMsxDistance().observe(this, (Float distance) -> processor.setMSXDistance(distance));
        frameViewModel.getPalette().observe(this, (RenderedImage.Palette palette) -> processor.setImagePalette(palette));
        frameViewModel.getEmissivity().observe(this, (Float emissivity) -> processor.setEmissivity(emissivity));
        frameViewModel.getFramePath().observe(this, (String path) -> bottomNavigationView.getMenu().findItem(R.id.menu_preview_analyze).setEnabled(path != null));

        flir = new FlirProxy(this);
        flir.getDeviceState().observe(this, (Boolean connected) -> {
            findViewById(R.id.textview_preview_connectdevice).setVisibility(connected ? View.GONE : View.VISIBLE);
            glSurfaceView.setVisibility(connected ? View.VISIBLE : View.GONE);

            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.menu_preview_devicesettings).setEnabled(connected);
            menu.findItem(R.id.menu_preview_capture).setEnabled(connected);

            if (connected) {
                processor.setImagePalette(RenderedImage.Palette.Gray);
                paletteCountDown.start();
            }
        });
        flir.getFrame().observe(this, (Frame frame) -> {
            if (frame == null) return;
            processor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            glSurfaceView.requestRender();
        });


        if (BuildConfig.DEBUG) debugSetup();
    }

    // Enables SimulatedDevice on debug builds
    private void debugSetup() {
        glSurfaceView.setOnClickListener((View v) -> flir.closeSimulatedDevice());

        TextView textView = findViewById(R.id.textview_preview_connectdevice);
        textView.setOnClickListener((View v) -> new Thread() {
            @Override
            public void run() {
                super.run();
                if (flir.getDeviceState().getValue() != null && flir.getDeviceState().getValue()) return;
                try {
                    new SimulatedDevice(flir, getBaseContext(), getResources().openRawResource(R.raw.sampleframes), 100);
                } catch (Exception e) {
                    Log.d(TAG, "Could not open simulated device!");
                }
            }
        }.start());
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            default:
                Log.w(TAG, "Selection of menu item \"" + menuItem.getTitle() + "\" was not handled by the listener");
        }

        return false;
    }
}
