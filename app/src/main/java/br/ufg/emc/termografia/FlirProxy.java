package br.ufg.emc.termografia;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.SimulatedDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.util.Preferences;

public class FlirProxy implements LifecycleObserver, Device.Delegate, Device.StreamDelegate, Device.PowerUpdateDelegate {
    private static final String TAG = FlirProxy.class.getSimpleName();

    private AppCompatActivity activityContext;
    private Device device;
    private boolean changeAutomaticTuningRequested = false;

    private MutableLiveData<Boolean> deviceState = new MutableLiveData<>();
    private MutableLiveData<Device.BatteryChargingState> batteryChargingState = new MutableLiveData<>();
    private MutableLiveData<Byte> batteryPercentage = new MutableLiveData<>();
    private MutableLiveData<Float> lowerAccuracyBound = new MutableLiveData<>();
    private MutableLiveData<Float> upperAccuracyBound = new MutableLiveData<>();
    private MutableLiveData<Device.TuningState> tuningState = new MutableLiveData<>();
    private MutableLiveData<Boolean> automaticTuning = new MutableLiveData<>();
    private MutableLiveData<Frame> frame = new MutableLiveData<>();

    public FlirProxy(AppCompatActivity activity)  {
        activityContext = activity;
        activityContext.getLifecycle().addObserver(this);

        resetDeviceData();
        frame.postValue(null);

        SharedPreferences preferences = Preferences.getPreferences(activity);
        String key = activity.getString(R.string.flirsettings_automatictuning_key);
        boolean defaultValue = activity.getResources().getBoolean(R.bool.flirsettings_automatictuning_default);
        automaticTuning.setValue(preferences.getBoolean(key, defaultValue));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        try {
            Device.startDiscovery(activityContext, this);
        } catch(IllegalStateException e) {
            Log.w(TAG, "Tried to start discovery multiple times in succession");
        } catch (SecurityException e) {
            // On some platforms, we need the user to select the app to give us permission to the USB device.
            Toast.makeText(activityContext, "Please insert FLIR One and select " + activityContext.getString(R.string.app_name), Toast.LENGTH_LONG).show();
            activityContext.finish();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (device != null)
            device.startFrameStream(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        if (device != null)
            device.stopFrameStream();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Device.stopDiscovery();
        device = null;
    }

    private void resetDeviceData() {
        deviceState.postValue(false);
        batteryChargingState.postValue(null);
        batteryPercentage.postValue(null);
        lowerAccuracyBound.postValue(null);
        upperAccuracyBound.postValue(null);
        tuningState.postValue(Device.TuningState.Unknown);
    }

    @Override
    public void onDeviceConnected(Device connectedDevice) {
        Log.i(TAG, "Device connected");

        device = connectedDevice;
        setAutomaticTuning(automaticTuning.getValue() == null || automaticTuning.getValue());
        deviceState.postValue(true);

        float lower = (float)Converter.kelvintoCelsius(device.getLowerAccuracyBound());
        float upper = (float)Converter.kelvintoCelsius(device.getUpperAccuracyBound());
        lowerAccuracyBound.postValue(lower);
        upperAccuracyBound.postValue(upper);

        device.setPowerUpdateDelegate(this);
        device.startFrameStream(this);
    }

    @Override
    public void onDeviceDisconnected(Device disconnectedDevice) {
        Log.i(TAG, "Device disconnected");
        device = null;
        changeAutomaticTuningRequested = false;
        resetDeviceData();
    }

    @Override
    public void onBatteryChargingStateReceived(Device.BatteryChargingState state) {
        batteryChargingState.postValue(state);
    }

    @Override
    public void onBatteryPercentageReceived(byte percentage) {
        batteryPercentage.postValue(percentage);
    }

    @Override
    public void onTuningStateChanged(Device.TuningState state) {
        tuningState.postValue(state);
    }

    @Override
    public void onAutomaticTuningChanged(boolean isEnabled) {
        String key = activityContext.getString(R.string.flirsettings_automatictuning_key);
        SharedPreferences.Editor editor = Preferences.getPreferences(activityContext).edit();
        editor.putBoolean(key, isEnabled);
        editor.apply();
        changeAutomaticTuningRequested = false;
        automaticTuning.postValue(isEnabled);
    }

    @Override
    public void onFrameReceived(Frame frame) {
        this.frame.postValue(frame);
    }

    public void setAutomaticTuning(boolean enabled) {
        if (device == null) return;
        if (changeAutomaticTuningRequested) return;
        if (automaticTuning.getValue() != null && automaticTuning.getValue() == enabled) return;

        changeAutomaticTuningRequested = true;
        device.setAutomaticTuning(enabled);
    }

    public void performTuning() {
        if (device != null)
            device.performTuning();
    }

    public void closeSimulatedDevice() {
        if (device instanceof SimulatedDevice)
            device.close();
    }

    public LiveData<Boolean> getDeviceState() {
        return deviceState;
    }

    public LiveData<Device.BatteryChargingState> getBatteryChargingState() {
        return batteryChargingState;
    }

    public LiveData<Byte> getBatteryPercentage() {
        return batteryPercentage;
    }

    public LiveData<Float> getLowerAccuracyBound() {
        return lowerAccuracyBound;
    }

    public LiveData<Float> getUpperAccuracyBound() {
        return upperAccuracyBound;
    }

    public LiveData<Device.TuningState> getTuningState() {
        return tuningState;
    }

    public LiveData<Boolean> getAutomaticTuning() {
        return automaticTuning;
    }

    public LiveData<Frame> getFrame() {
        return frame;
    }
}
