package br.ufg.emc.termografia.ui;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.diagnosis.BushingDiagnoser;
import br.ufg.emc.termografia.diagnosis.Concept;
import br.ufg.emc.termografia.viewmodel.DiagnosisViewModel;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

public class DiagnosisDialogFragment extends BottomSheetDialogFragment {
    public static final String FRAGMENT_TAG = DiagnosisDialogFragment.class.getCanonicalName();

    private ThermalImageViewModel imageViewModel;
    private DiagnosisViewModel diagnosisViewModel;

    public DiagnosisDialogFragment() { /* Required empty public constructor */ }

    public static DiagnosisDialogFragment newInstance() {
        return new DiagnosisDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewModel = ViewModelProviders.of(requireActivity()).get(ThermalImageViewModel.class);
        diagnosisViewModel = ViewModelProviders.of(requireActivity()).get(DiagnosisViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diagnosis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_diagnosis);
        toolbar.setNavigationIcon(R.drawable.ic_all_dismiss);
        toolbar.setNavigationOnClickListener((View v) -> dismiss());

        TextView conceptView = view.findViewById(R.id.textview_diagnosis_concept);
        TextView insufficientView = view.findViewById(R.id.textview_diagnosis_insufficient);

        diagnosisViewModel.getDiagnoser().observe(this, (BushingDiagnoser diagnoser) -> {
            List<Meter> meterList = imageViewModel.getMeterList().getValue();
            if (meterList.size() < 2) {
                conceptView.setVisibility(View.GONE);
                insufficientView.setVisibility(View.VISIBLE);
            } else {
                Concept concept = diagnoser.getConcept(imageViewModel.getMeterList().getValue());
                conceptView.setVisibility(View.VISIBLE);
                conceptView.setText(concept.toString());
                conceptView.setTextColor(concept.getColor(requireContext()));
                insufficientView.setVisibility(View.GONE);
            }
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.viewgroup_diagnosis_fragmentcontainer, DiagnosisSettingsFragment.newInstance())
                .commit();
    }
}
