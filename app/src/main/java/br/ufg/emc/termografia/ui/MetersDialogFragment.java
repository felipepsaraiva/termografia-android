package br.ufg.emc.termografia.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

public class MetersDialogFragment extends BottomSheetDialogFragment implements Toolbar.OnMenuItemClickListener {
    private static final String LOG_TAG = MetersDialogFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = MetersDialogFragment.class.getCanonicalName();

    private ThermalImageViewModel imageViewModel;

    public MetersDialogFragment() { /* Required empty public constructor */ }

    public static MetersDialogFragment newInstance() {
        return new MetersDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewModel = ViewModelProviders.of(requireActivity()).get(ThermalImageViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_meters);
        toolbar.setNavigationIcon(R.drawable.ic_all_dismiss);
        toolbar.setNavigationOnClickListener((View v) -> dismiss());
        toolbar.inflateMenu(R.menu.fragment_meters_toolbar_actions);
        toolbar.setOnMenuItemClickListener(this);

        RecyclerView meterList = view.findViewById(R.id.recyclerview_meters);
        meterList.setAdapter(new MeterListAdapter(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        meterList.setLayoutManager(layoutManager);
        meterList.addItemDecoration(new CustomDividerItemDecorator(requireContext(), null));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_meters_add:
                imageViewModel.addNewMeter(true);
                return true;

            default:
                Log.w(LOG_TAG, "Toolbar menu item click not handled!");
        }

        return false;
    }
}
