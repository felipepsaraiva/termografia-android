package br.ufg.emc.termografia.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import br.ufg.emc.termografia.FlirProxy;
import br.ufg.emc.termografia.R;

public class FlirDeviceDialogFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_TAG = FlirDeviceDialogFragment.class.getCanonicalName();

    private FlirProxy flir;

    public FlirDeviceDialogFragment() { /* Required empty public constructor */ }

    public static FlirDeviceDialogFragment newInstance(@Nullable FlirProxy proxy) {
        FlirDeviceDialogFragment instance = new FlirDeviceDialogFragment();
        instance.setFlirProxy(proxy);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flirdevice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_flirdevice);
        toolbar.setNavigationIcon(R.drawable.ic_all_dismiss);
        toolbar.setNavigationOnClickListener((View v) -> dismiss());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.viewgroup_flirdevice_fragmentcontainer, FlirSettingsFragment.newInstance(flir))
                .commit();
    }

    private void setFlirProxy(FlirProxy flirProxy) {
        flir = flirProxy;
    }
}
