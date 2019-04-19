package br.ufg.emc.termografia.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import br.ufg.emc.termografia.BuildConfig;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.util.ExternalStorageUtils;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;
import com.github.florent37.runtimepermission.PermissionResult;
import com.github.florent37.runtimepermission.RuntimePermission;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PreviewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = PreviewActivity.class.getSimpleName();

    private ThermalFrameViewModel frameViewModel;
    private FlirProxy flir;
    private FrameProcessor processor;
    private boolean imageCaptureRequested = false;

    private GLSurfaceView glSurfaceView;
    private BottomNavigationView bottomNavigationView;

    // Workaround on the issue of gray scale image the first time a device is connected
    private CountDownTimer paletteCountDown = new CountDownTimer(500, 500) {
        @Override public void onTick(long l) {}

        @Override
        public void onFinish() {
            RenderedImage.Palette palette = RenderedImage.Palette.valueOf(frameViewModel.getPalette().getValue());
            processor.setImagePalette(palette);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        processor = new FrameProcessor(this, (RenderedImage image) -> {}, null, true);

        glSurfaceView = findViewById(R.id.glsurfaceview_preview);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        glSurfaceView.setRenderer(processor);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        bottomNavigationView = findViewById(R.id.bottomnavigationview_preview_actions);
        // TODO: Item selecionado não fica com aparencia de desabilitado
        bottomNavigationView.setSelectedItemId(R.id.menu_preview_capture);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        flir = new FlirProxy(this);
        frameViewModel = ViewModelProviders.of(this).get(ThermalFrameViewModel.class);

        setupObservers();
        if (BuildConfig.DEBUG) debugSetup();
    }

    private void setupObservers() {
        frameViewModel.getImageType().observe(this, (String imageTypeName) -> {
            RenderedImage.ImageType imageType = RenderedImage.ImageType.valueOf(imageTypeName);
            processor.setGLOutputMode(imageType);
        });
        frameViewModel.getMsxDistance().observe(this, (Integer percentage) -> {
            float distance = Converter.msxDistance(this, Objects.requireNonNull(percentage));
            processor.setMSXDistance(distance);
        });
        frameViewModel.getPalette().observe(this, (String paletteName) -> {
            RenderedImage.Palette palette = RenderedImage.Palette.valueOf(paletteName);
            processor.setImagePalette(palette);
        });
        frameViewModel.getEmissivity().observe(this, (String emissivityType) -> {
            float emissivity;
            switch (emissivityType) {
                case "semi-glossy":
                    emissivity = FrameProcessor.EMISSIVITY_SEMI_GLOSSY;
                    break;
                case "semi-matte":
                    emissivity = FrameProcessor.EMISSIVITY_SEMI_MATTE;
                    break;
                case "matte":
                    emissivity = FrameProcessor.EMISSIVITY_MATTE;
                    break;

                default:
                    emissivity = FrameProcessor.EMISSIVITY_GLOSSY;
            }
            processor.setEmissivity(emissivity);
        });

        flir.getDeviceState().observe(this, (FlirProxy.DeviceState state) -> {
            boolean connected = (state != FlirProxy.DeviceState.Disconnected);
            findViewById(R.id.textview_preview_connectdevice).setVisibility(connected ? View.GONE : View.VISIBLE);
            glSurfaceView.setVisibility(connected ? View.VISIBLE : View.GONE);

            bottomNavigationView.getMenu()
                    .findItem(R.id.menu_preview_capture).setEnabled(connected);

            if (connected) {
                processor.setImagePalette(RenderedImage.Palette.Gray);
                paletteCountDown.start();
            }
        });
        flir.getFrame().observe(this, (Frame frame) -> {
            if (frame == null) return;
            processor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            glSurfaceView.requestRender();

            if (imageCaptureRequested) {
                imageCaptureRequested = false;
                requestPermissionsToCaptureFrame(frame);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_preview_devicesettings:
                FlirDeviceDialogFragment.newInstance(flir)
                        .show(getSupportFragmentManager(), FlirDeviceDialogFragment.FRAGMENT_TAG);
                break;

            case R.id.menu_preview_capture:
                FlirProxy.DeviceState deviceState = flir.getDeviceState().getValue();
                if (deviceState != null && deviceState != FlirProxy.DeviceState.Disconnected)
                    imageCaptureRequested = true;
                break;

            case  R.id.menu_preview_analyze:
                openAnalysisActvity();
                break;

            default:
                Log.w(LOG_TAG, "Selection of menu item \"" + menuItem.getTitle() + "\" was not handled by the listener");
        }

        return false;
    }

    private void requestPermissionsToCaptureFrame(@NonNull Frame frame) {
        RuntimePermission.askPermission(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onAccepted((PermissionResult result) -> saveFrame(frame))
                .ask();
    }

    private void saveFrame(Frame frame) {
        new Thread(() -> {
            try {
                File frameFile = ExternalStorageUtils.createNewFrameFile(this);
                frame.save(frameFile, processor);

                runOnUiThread(() -> {
                    frameViewModel.setFramePath(frameFile.getAbsolutePath());
                    Snackbar.make(findViewById(R.id.root_preview), getString(R.string.preview_capture_success), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.all_open), (View view) -> openAnalysisActvity())
                            .show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(R.id.root_preview), getString(R.string.preview_capture_error), Snackbar.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void openAnalysisActvity() {
        Intent intent = new Intent(this, AnalysisActivity.class);
        String path = frameViewModel.getFramePath().getValue();
        if (path != null) intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

    // Enables SimulatedDevice on debug builds
    private void debugSetup() {
        glSurfaceView.setOnClickListener((View v) -> flir.closeSimulatedDevice());
        findViewById(R.id.textview_preview_connectdevice).setOnClickListener((View v) -> new Thread() {
            @Override
            public void run() {
                super.run();
                if (flir.getDeviceState().getValue() != FlirProxy.DeviceState.Disconnected) return;
                try {
                    new SimulatedDevice(flir, getBaseContext(), getResources().openRawResource(R.raw.sampleframes), 100);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Could not open simulated device!");
                    e.printStackTrace();
                }
            }
        }.start());
    }
}
