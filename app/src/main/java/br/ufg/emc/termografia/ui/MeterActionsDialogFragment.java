package br.ufg.emc.termografia.ui;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

public class MeterActionsDialogFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_TAG = MeterActionsDialogFragment.class.getCanonicalName();

    private Meter selected;
    private ThermalImageViewModel imageViewModel;

    public MeterActionsDialogFragment() { /* Required empty public constructor */ }

    public static MeterActionsDialogFragment newInstance() {
        return new MeterActionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewModel = ViewModelProviders.of(requireActivity()).get(ThermalImageViewModel.class);
        selected = imageViewModel.getSelectedMeter().getValue();
        if (selected == null) dismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meter_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selected == null) return;

        TextView name = view.findViewById(R.id.textview_meteractions_name);
        TextView temperature = view.findViewById(R.id.textview_meteractions_temperature);
        TextView concept = view.findViewById(R.id.textview_meteractions_concept);

        temperature.setText(requireContext().getString(
                R.string.meter_actions_temperature,
                selected.getTemperature(),
                selected.getPercentageDiscrepancy()
        ));

        if (selected.isAmbient()) {
            ViewGroup actions = view.findViewById(R.id.viewgroup_meteractions_actions);

            name.setText(requireContext().getString(R.string.meter_actions_name_ambient));
            concept.setVisibility(View.GONE);
            actions.setVisibility(View.GONE);
        } else {
            TextView remove = view.findViewById(R.id.textview_meteractions_remove);

            name.setText(requireContext().getString(R.string.meter_actions_name, "1"));
            remove.setOnClickListener(v -> {
                imageViewModel.removeMeter(selected);
                dismiss();
            });
        }

        // TODO: Atualizar o valor do Conceito
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        imageViewModel.setSelectedMeter(null);
    }
}
