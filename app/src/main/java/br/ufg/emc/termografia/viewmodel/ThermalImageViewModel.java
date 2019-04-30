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
    private MutableLiveData<Meter> selectedMeter = new MutableLiveData<>();

    private List<Meter> fixedMeterList;
    private Meter ambient;
    private Meter target;

    public ThermalImageViewModel() {
        fixedMeterList = Collections.synchronizedList(new ArrayList<>());
        target = null;

        ambient = new Meter(0.5, 0.5);
        ambient.setAmbient(true);
        addMeter(ambient, false);

        image.setValue(null);
        thermalData.setValue(null);
        meterList.setValue(fixedMeterList);
        selectedMeter.setValue(null);
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

    public void setSelectedMeter(Meter meter) {
        selectedMeter.setValue(meter);
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

    public LiveData<Meter> getSelectedMeter() {
        return selectedMeter;
    }

    private void notifyMeterUpdate() {
        meterList.setValue(fixedMeterList);
    }

    /**
     * Handling Meters
     */

    public void addMeter(Meter meter, boolean notify) {
        fixedMeterList.add(meter);
        updateMeterTemperature(meter);
        if (notify) notifyMeterUpdate();
    }

    public void addNewMeter() {
        addMeter(new Meter(0.5, 0.5), true);
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

    public void setTarget(Meter meter) {
        target = meter;
    }

    public Meter getTarget() {
        return target;
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

    public void changeTargetPosition(RelativePoint point) {
        if (target == null) return;

        target.setRelativeCoordinates(point.getRelativeX(), point.getRelativeY());
        updateMeterTemperature(target);
        notifyMeterUpdate();
    }

    public void removeMeter(Meter meter) {
        if (meter.isAmbient()) return;
        fixedMeterList.remove(meter);

        if (meter == target) target = null;
        if (meter == selectedMeter.getValue()) setSelectedMeter(null);

        notifyMeterUpdate();
    }
}
