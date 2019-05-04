package br.ufg.emc.termografia.ui;

import android.os.Bundle;

import android.os.CountDownTimer;
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

    private TextView conceptView;
    private ViewGroup loadingView;
    private TextView statusView;

    private LoadingCountDown countDown = null;

    public DiagnosisDialogFragment() { /* Required empty public constructor */ }

    public static DiagnosisDialogFragment newInstance() {
        return new DiagnosisDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewModel = ViewModelProviders.of(requireActivity()).get(ThermalImageViewModel.class);
        diagnosisViewModel = ViewModelProviders.of(requireActivity()).get(DiagnosisViewModel.class);
        diagnosisViewModel.getDiagnoser().observe(this, d -> diagnose());
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

        conceptView = view.findViewById(R.id.textview_diagnosis_concept);
        loadingView = view.findViewById(R.id.viewgroup_diagnosis_loading);
        statusView = view.findViewById(R.id.textview_diagnosis_status);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.viewgroup_diagnosis_fragmentcontainer, DiagnosisSettingsFragment.newInstance())
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        conceptView = null;
        loadingView = null;
        statusView = null;

        if (countDown != null) {
            countDown.cancel();
            countDown = null;
        }
    }

    private void diagnose() {
        if (countDown != null) return;

        conceptView.setVisibility(View.GONE);

        List<Meter> meterList = imageViewModel.getMeterList().getValue();
        if (meterList == null || meterList.size() < 2) {
            statusView.setText(meterList == null ? R.string.diagnosis_error : R.string.diagnosis_insufficient_meters);
            loadingView.setVisibility(View.GONE);
            statusView.setVisibility(View.VISIBLE);
            return;
        }

        statusView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        countDown = new LoadingCountDown(2000);
        countDown.start();
    }

    private class LoadingCountDown extends CountDownTimer {
        public LoadingCountDown(long ms) {
            super(ms, ms);
        }

        @Override
        public void onTick(long millisUntilFinished) {}

        @Override
        public void onFinish() {
            if (conceptView == null || loadingView == null || statusView == null) return;

            List<Meter> meterList = imageViewModel.getMeterList().getValue();
            BushingDiagnoser diagnoser = diagnosisViewModel.getDiagnoser().getValue();

            loadingView.setVisibility(View.GONE);

            if (meterList == null || diagnoser == null) {
                statusView.setText(R.string.diagnosis_error);
                conceptView.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                return;
            }

            Concept concept = diagnoser.getConcept(imageViewModel.getMeterList().getValue());
            conceptView.setText(concept.toString());
            conceptView.setTextColor(concept.getColor(requireContext()));

            statusView.setVisibility(View.GONE);
            conceptView.setVisibility(View.VISIBLE);

            countDown = null;
        }
    }
}
