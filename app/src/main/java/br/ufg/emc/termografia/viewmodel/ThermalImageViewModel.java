package br.ufg.emc.termografia.viewmodel;

import android.graphics.Bitmap;
import android.preference.ListPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import br.ufg.emc.termografia.Meter;
import br.ufg.emc.termografia.ThermalData;
import br.ufg.emc.termografia.util.RelativePoint;

public class ThermalImageViewModel extends ViewModel {
    private MutableLiveData<Bitmap> image = new MutableLiveData<>();
    private MutableLiveData<ThermalData> thermalData = new MutableLiveData<>();
    private MutableLiveData<List<Meter>> meterList = new MutableLiveData<>();

    private List<Meter> fixedMeterList;
    private Meter ambient;

    public ThermalImageViewModel() {
        fixedMeterList = Collections.synchronizedList(new ArrayList<>());

        ambient = new Meter(0.5, 0.5);
        ambient.setSelected(true);
        ambient.setAmbient(true);
        addMeter(ambient, false);

        image.setValue(null);
        thermalData.setValue(null);
        meterList.setValue(fixedMeterList);
    }

    public void setImage(Bitmap bitmap) {
        image.setValue(bitmap);
    }

    public void setThermalData(ThermalData data) {
        thermalData.setValue(data);

        ambient.updateTemperature(data);
        ambient.setDifference(0);
        double ambientTemperature = ambient.getTemperature();

        for (Meter m: fixedMeterList) {
            if (m.isAmbient()) continue;
            m.updateTemperature(data);
            m.updateDifference(ambientTemperature);
        }

        notifyMeterUpdate();
    }

    private void notifyMeterUpdate() {
        meterList.setValue(fixedMeterList);
    }

    public LiveData<Bitmap> getImage() {
        return image;
    }

    public LiveData<ThermalData> getThermalData() {
        return thermalData;
    }

    public LiveData<List<Meter>> getMeterList() {
        return meterList;
    }

    // Handling Meters
    public void addMeter(Meter meter, boolean notify) {
        fixedMeterList.add(meter);
        updateMeterTemperature(meter);
        if (notify) notifyMeterUpdate();
    }

    public void addNewMeter(boolean select) {
        addMeter(new Meter(0.5, 0.5), !select);
        if (select) setSelectedMeter(fixedMeterList.size() - 1);
    }

    @Nullable
    public Meter getMeter(int index) {
        if (index < 0 || index >= fixedMeterList.size()) return null;
        return fixedMeterList.get(index);
    }

    @NonNull
    public Meter getAmbientMeter() {
        return ambient;
    }

    @Nullable
    public Meter getSelectedMeter() {
        for (Meter m: fixedMeterList)
            if (m.isSelected()) return m;
        return null;
    }

    public void setSelectedMeter(int index) {
        Meter m = getSelectedMeter();
        if (m != null) m.setSelected(false);

        m = getMeter(index);
        if (m != null)
            m.setSelected(true);

        notifyMeterUpdate();
    }

    private void updateMeterTemperature(Meter meter) {
        ThermalData data = thermalData.getValue();
        if (data == null) return;

        meter.updateTemperature(data);
        if (meter.isAmbient()) {
            double ambientTemperature = meter.getTemperature();
            for (Meter m: fixedMeterList)
                m.updateDifference(ambientTemperature);
        } else {
            meter.updateDifference(ambient.getTemperature());
        }
    }

    public void changeMeterPosition(RelativePoint point) {
        Meter selected = getSelectedMeter();
        if (selected == null) return;

        selected.setRelativeCoordinates(point.getRelativeX(), point.getRelativeY());
        updateMeterTemperature(selected);
        notifyMeterUpdate();
    }

    public void removeMeter(Meter meter) {
        if (meter.isAmbient()) return;
        fixedMeterList.remove(meter);

        if (meter.isSelected()) setSelectedMeter(fixedMeterList.size() - 1);
        else notifyMeterUpdate();
    }
}
