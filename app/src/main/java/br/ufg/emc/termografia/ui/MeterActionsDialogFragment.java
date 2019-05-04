package br.ufg.emc.termografia.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.diagnosis.BushingDiagnoser;
import br.ufg.emc.termografia.diagnosis.Concept;
import br.ufg.emc.termografia.viewmodel.DiagnosisViewModel;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

public class MeterActionsDialogFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_TAG = MeterActionsDialogFragment.class.getCanonicalName();

    private Meter selected;
    private ThermalImageViewModel imageViewModel;
    private DiagnosisViewModel diagnosisViewModel;

    public MeterActionsDialogFragment() { /* Required empty public constructor */ }

    public static MeterActionsDialogFragment newInstance() {
        return new MeterActionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewModel = ViewModelProviders.of(requireActivity()).get(ThermalImageViewModel.class);
        diagnosisViewModel = ViewModelProviders.of(requireActivity()).get(DiagnosisViewModel.class);

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
        TextView relativeTemperature = view.findViewById(R.id.textview_meteractions_temperature_relative);
        TextView conceptView = view.findViewById(R.id.textview_meteractions_concept);

        temperature.setText(requireContext().getString(
                R.string.meter_actions_temperature,
                selected.getTemperature(),
                selected.getPercentageDiscrepancy()
        ));

        if (selected.isAmbient()) {
            ViewGroup actions = view.findViewById(R.id.viewgroup_meteractions_actions);

            name.setText(requireContext().getString(R.string.meter_actions_name_ambient));
            relativeTemperature.setVisibility(View.GONE);
            conceptView.setVisibility(View.GONE);
            actions.setVisibility(View.GONE);
        } else {
            int index = imageViewModel.getMeterList().getValue().indexOf(selected);
            name.setText(requireContext().getString(R.string.meter_actions_name, index));
            relativeTemperature.setText(
                    requireContext().getString(R.string.meter_actions_temperature_relative, selected.getDifference()));

            diagnosisViewModel.getDiagnoser().observe(this, (BushingDiagnoser diagnoser) -> {
                Concept concept = diagnoser.getIndividualConcept(selected);
                conceptView.setText(concept.toString());
                conceptView.setTextColor(concept.getColor(requireContext()));
            });
            conceptView.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.meter_actions_concept_description, Toast.LENGTH_SHORT).show());

            TextView remove = view.findViewById(R.id.textview_meteractions_remove);
            remove.setOnClickListener(v -> {
                imageViewModel.removeMeter(selected);
                dismiss();
            });
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        imageViewModel.setSelectedMeter(null);
    }
}
