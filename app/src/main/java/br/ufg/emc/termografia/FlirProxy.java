package br.ufg.emc.termografia;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.SimulatedDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import br.ufg.emc.termografia.util.Converter;

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
        frame.setValue(null);

        SharedPreferences preferences = Preferences.getSharedPreferences(activityContext);
        Boolean auto = preferences.getBoolean(Preferences.automaticTuning.key, Preferences.automaticTuning.defaultValue);
        automaticTuning.setValue(auto);
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
        if (device instanceof SimulatedDevice) device.close();
        Device.stopDiscovery();
        device = null;
    }

    private void resetDeviceData() {
        deviceState.setValue(false);
        batteryChargingState.setValue(null);
        batteryPercentage.setValue(null);
        lowerAccuracyBound.setValue(null);
        upperAccuracyBound.setValue(null);
        tuningState.setValue(Device.TuningState.Unknown);
    }

    @Override
    public void onDeviceConnected(Device connectedDevice) {
        Log.i(TAG, "Device connected");

        device = connectedDevice;
        deviceState.setValue(true);

        float lower = (float)Converter.kelvintoCelsius(device.getLowerAccuracyBound());
        float upper = (float)Converter.kelvintoCelsius(device.getUpperAccuracyBound());
        lowerAccuracyBound.setValue(lower);
        upperAccuracyBound.setValue(upper);

        device.setPowerUpdateDelegate(this);
        device.startFrameStream(this);
    }

    @Override
    public void onDeviceDisconnected(Device disconnectedDevice) {
        device = null;
        resetDeviceData();
    }

    @Override
    public void onBatteryChargingStateReceived(Device.BatteryChargingState state) {
        batteryChargingState.setValue(state);
    }

    @Override
    public void onBatteryPercentageReceived(byte percentage) {
        batteryPercentage.setValue(percentage);
    }

    @Override
    public void onTuningStateChanged(Device.TuningState state) {
        tuningState.setValue(state);
    }

    @Override
    public void onAutomaticTuningChanged(boolean isEnabled) {
        SharedPreferences.Editor editor = Preferences.getSharedPreferences(activityContext).edit();
        editor.putBoolean(Preferences.automaticTuning.key, isEnabled);
        editor.apply();
        changeAutomaticTuningRequested = false;
        automaticTuning.setValue(isEnabled);
    }

    @Override
    public void onFrameReceived(Frame frame) {
        this.frame.setValue(frame);
    }

    public void setAutomaticTuning(boolean enabled) {
        if (device != null && !changeAutomaticTuningRequested) {
            changeAutomaticTuningRequested = true;
            device.setAutomaticTuning(enabled);
        }
    }

    public void toggleAutomaticTuning() {
        boolean val = (automaticTuning.getValue() != null && automaticTuning.getValue());
        setAutomaticTuning(!val);
    }

    public void performTuning() {
        if (device != null)
            device.performTuning();
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
