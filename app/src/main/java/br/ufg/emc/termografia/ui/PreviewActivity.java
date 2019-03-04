package br.ufg.emc.termografia.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PreviewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = PreviewActivity.class.getSimpleName();

    private ThermalFrameViewModel frameViewModel;
    private FlirProxy flir;
    private FrameProcessor processor;

    private GLSurfaceView glSurfaceView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        processor = new FrameProcessor(this, null, null, true);

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
            Log.d(TAG, "Device state: " + connected.toString());
            findViewById(R.id.textview_preview_connectdevice).setVisibility(connected ? View.GONE : View.VISIBLE);
            glSurfaceView.setVisibility(connected ? View.VISIBLE : View.GONE);

            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.menu_preview_devicesettings).setEnabled(connected);
            menu.findItem(R.id.menu_preview_capture).setEnabled(connected);
        });
        flir.getFrame().observe(this, (Frame frame) -> {
            if (frame == null) return;
            processor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            glSurfaceView.requestRender();
        });
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
