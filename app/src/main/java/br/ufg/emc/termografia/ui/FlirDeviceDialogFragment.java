package br.ufg.emc.termografia.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalFrameViewModel;

// TODO: Configurar FlirProxy e propagar para o FlirSettingsFragment

public class FlirDeviceDialogFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_TAG = FlirDeviceDialogFragment.class.getCanonicalName();

    private ThermalFrameViewModel frameViewModel;
    private FlirProxy flir;

    public static FlirDeviceDialogFragment newInstance(FlirProxy proxy) {
        FlirDeviceDialogFragment instance = new FlirDeviceDialogFragment();
        instance.setFlirProxy(proxy);
        return instance;
    }

    public FlirDeviceDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModels(Objects.requireNonNull(getActivity()));
    }

    private void setupViewModels(FragmentActivity activity) {
        frameViewModel = ViewModelProviders.of(activity).get(ThermalFrameViewModel.class);
        // Subscribe to events
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flirdevice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.viewgroup_flirdevice_fragmentcontainer, new FlirSettingsFragment())
                .commit();
        // Setup views here
    }

    private void setFlirProxy(FlirProxy flirProxy) {
        flir = flirProxy;
    }
}
