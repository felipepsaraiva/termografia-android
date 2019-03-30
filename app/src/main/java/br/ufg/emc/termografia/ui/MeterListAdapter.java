package br.ufg.emc.termografia.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.R;
import br.ufg.emc.termografia.viewmodel.ThermalImageViewModel;

public class MeterListAdapter extends RecyclerView.Adapter<MeterListAdapter.MeterViewHolder> {
    private static final String LOG_TAG = MeterListAdapter.class.getSimpleName();

    private Context context;
    private LayoutInflater inflater;
    private ThermalImageViewModel imageViewModel;

    public MeterListAdapter(FragmentActivity parent) {
        context = parent;
        inflater = LayoutInflater.from(context);
        imageViewModel = ViewModelProviders.of(parent).get(ThermalImageViewModel.class);
        imageViewModel.getMeterList().observe(parent, (List<Meter> l) -> notifyDataSetChanged());
    }

    public MeterListAdapter(Fragment parent) {
        context = parent.requireContext();
        inflater = LayoutInflater.from(context);
        imageViewModel = ViewModelProviders.of(parent.requireActivity()).get(ThermalImageViewModel.class);
        imageViewModel.getMeterList().observe(parent, (List<Meter> l) -> notifyDataSetChanged());
    }

    @NonNull
    @Override
    public MeterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_meter, parent, false);
        return new MeterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeterViewHolder holder, int position) {
        final Meter m = imageViewModel.getMeter(position);
        if (m != null) {
            holder.root.setOnClickListener(view -> imageViewModel.setSelectedMeter(position));

            if (m.isSelected())
                holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.meter_list_background_selected));
            else
                holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.meter_list_background_default));

            if (m.isAmbient()) {
                holder.name.setText(context.getString(R.string.meters_name_ambient));
                holder.remove.setVisibility(View.GONE);
                holder.remove.setOnClickListener(null);
            } else {
                holder.name.setText(context.getString(R.string.meters_name_default, position));
                holder.remove.setVisibility(View.VISIBLE);
                holder.remove.setOnClickListener(view -> imageViewModel.removeMeter(m));
            }

            double temperature =  m.getTemperature();
            double percentage = (m.getAverageDiscrepancy() / temperature) * 100;
            String text = context.getString(R.string.meters_temperature, temperature, percentage);
            holder.temperature.setText(text);
        } else {
            holder.root.setVisibility(View.GONE);
            holder.root.setOnClickListener(null);
            holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.meter_list_background_default));
            holder.name.setText(null);
            holder.temperature.setText(null);
            holder.remove.setVisibility(View.GONE);
            holder.remove.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        List<Meter> list = imageViewModel.getMeterList().getValue();
        return (list != null ? list.size() : 0);
    }

    public class MeterViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView name;
        TextView temperature;
        ImageButton remove;

        public MeterViewHolder(@NonNull View view) {
            super(view);
            root = view;
            name = view.findViewById(R.id.textview_meters_item_name);
            temperature = view.findViewById(R.id.textview_meters_item_temperature);
            remove = view.findViewById(R.id.imagebutton_meters_item_remove);
        }
    }
}
