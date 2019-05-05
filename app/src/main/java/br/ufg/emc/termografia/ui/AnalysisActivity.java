package br.ufg.emc.termografia.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.ThermalData;
import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.util.ExternalStorageUtils;
import br.ufg.emc.termografia.util.FileUtils;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.LoadedFrame;
import com.flir.flironesdk.RenderedImage;
import com.github.florent37.runtimepermission.PermissionResult;
import com.github.florent37.runtimepermission.RuntimePermission;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public class AnalysisActivity extends AppCompatActivity implements FrameProcessor.Delegate, BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = AnalysisActivity.class.getSimpleName();
    private static final int CHOOSE_FRAME_REQUEST_CODE = 1;

    private ThermalFrameViewModel frameViewModel;
    private ThermalImageViewModel imageViewModel;
    private FrameProcessor processor;
    private Uri frameUri;
    private LoadedFrame frame;
    private boolean visible = false;

    private ThermometerSurfaceView surfaceView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        processor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage));
        frameUri = getIntent().getData();

        surfaceView = findViewById(R.id.thermometersurfaceview_analysis);

        bottomNavigationView = findViewById(R.id.bottomnavigationview_analysis_actions);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        Menu bottomMenu = bottomNavigationView.getMenu();
        for (int i = 0; i < bottomMenu.size(); i++)
            bottomMenu.getItem(i).setCheckable(false);

        frameViewModel = ViewModelProviders.of(this).get(ThermalFrameViewModel.class);
        imageViewModel = ViewModelProviders.of(this).get(ThermalImageViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        imageViewModel.getImage().observe(this, image -> {
            boolean hasImage = image != null;
            surfaceView.setVisibility(hasImage ? View.VISIBLE : View.GONE);
            Menu actions = bottomNavigationView.getMenu();
            actions.findItem(R.id.menu_analysis_new_meter).setEnabled(hasImage);
            actions.findItem(R.id.menu_analysis_diagnosis).setEnabled(hasImage);
        });
        imageViewModel.getSelectedMeter().observe(this, (Meter selected) -> {
            if (selected == null) return;
            MeterActionsDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), MeterActionsDialogFragment.FRAGMENT_TAG);
        });

        frameViewModel.getImageType().observe(this, (String name) -> {
            processor.setImageTypes(EnumSet.of(
                    RenderedImage.ImageType.ThermalRadiometricKelvinImage,
                    RenderedImage.ImageType.valueOf(name)
            ));
            processFrame();
        });
        frameViewModel.getMsxDistance().observe(this, (Integer percentage) -> {
            // TODO: Conversão apropriada para o LoadedFrame
            float distance = Converter.msxDistance(this, Objects.requireNonNull(percentage));
            processor.setMSXDistance(distance);
            processFrame();
        });
        frameViewModel.getPalette().observe(this, (String name) -> {
            RenderedImage.Palette palette = RenderedImage.Palette.valueOf(name);
            processor.setImagePalette(palette);
            processFrame();
        });
        frameViewModel.getEmissivity().observe(this, (String emissivityType) -> {
            // TODO: Verificar se a temperatura muda quando a emissividade muda
            float emissivity;
            switch (emissivityType) {
                case "glossy":
                    emissivity = FrameProcessor.EMISSIVITY_GLOSSY;
                    break;

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
                    Log.w(LOG_TAG, "Selected value for emissivity does not exist. Setting default...");
                    emissivity = FrameProcessor.EMISSIVITY_GLOSSY;
            }

            processor.setEmissivity(emissivity);
            processFrame();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RuntimePermission.askPermission(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .onAccepted((PermissionResult result) -> loadFrame())
                .onDenied((PermissionResult result) -> finish())
                .ask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
        processFrame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }

    private void loadFrame() {
        File frameFile = null;

        try {
            if (frameUri != null) {
                // TODO: Encontrar alternativa para abrir o frame a partir do Uri
                if ("file".equalsIgnoreCase(frameUri.getScheme()))
                    frameFile = (frameUri.getPath() != null ? new File(frameUri.getPath()) : null);
                else
                    frameFile = FileUtils.getFile(this, frameUri);
            } else {
                File dir = ExternalStorageUtils.getAppDirectory(this);
                File[] files = dir.listFiles();
                if (files.length > 0)
                    frameFile = Collections.max(Arrays.asList(files),
                            (File a, File b) -> a.getName().compareTo(b.getName()));
            }


            if (frameFile != null)
                frame = new LoadedFrame(frameFile);
        } catch (RuntimeException | IOException e) {
            frame = null;
            e.printStackTrace();
            finishWithToast(R.string.analysis_load_error);
        } finally {
            processFrame();
        }
    }

    private void processFrame() {
        if (frame == null || !visible) return;
        processor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
    }

    @Override
    public void onFrameProcessed(RenderedImage renderedImage) {
        Log.d(LOG_TAG, "[Processed frame]");
        Log.d(LOG_TAG, "Image type: " + processor.getImageTypes().toString());
        Log.d(LOG_TAG, "Palette: " + processor.getImagePalette().toString());
        Log.d(LOG_TAG, "Msx Distance: " + processor.getMSXDistance());
        Log.d(LOG_TAG, "Emissivity: " + processor.getEmissivity());

        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
            int[] temperatures = renderedImage.thermalPixelValues();
            int width = renderedImage.width();
            int height = renderedImage.height();
            imageViewModel.setThermalData(new ThermalData(temperatures, width, height));
        } else if (renderedImage.imageType() == RenderedImage.ImageType.valueOf(frameViewModel.getImageType().getValue())) {
            imageViewModel.setImage(renderedImage.getBitmap());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_analysis_replace_frame:
                startFrameChooser();
                break;

            case R.id.menu_analysis_devicesettings:
                FlirDeviceDialogFragment.newInstance(null)
                        .show(getSupportFragmentManager(), FlirDeviceDialogFragment.FRAGMENT_TAG);
                break;

            case R.id.menu_analysis_diagnosis:
                DiagnosisDialogFragment.newInstance()
                        .show(getSupportFragmentManager(), DiagnosisDialogFragment.FRAGMENT_TAG);
                break;

            case R.id.menu_analysis_new_meter:
                imageViewModel.addNewMeter();
                break;

            default:
                Log.w(LOG_TAG, "Selection of menu item \"" + menuItem.getTitle() + "\" was not handled by the listener");
        }

        return false;
    }

    private void startFrameChooser() {
        RuntimePermission.askPermission(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .onAccepted((PermissionResult result) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/jpeg");
                    startActivityForResult(intent, CHOOSE_FRAME_REQUEST_CODE);
                }).ask();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CHOOSE_FRAME_REQUEST_CODE) return;
        if (resultCode != RESULT_OK) return;
        if (data == null || data.getData() == null) return;

        frameUri = data.getData();
        loadFrame();
    }

    private void finishWithToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
        finish();
    }

    private void finishWithToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }
}
