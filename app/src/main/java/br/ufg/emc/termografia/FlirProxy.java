package br.ufg.emc.termografia;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.EmbeddedDevice;
import com.flir.flironesdk.FlirUsbDevice;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.SimulatedDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.util.Preferences;

public class FlirProxy implements LifecycleObserver, Device.Delegate, Device.StreamDelegate, Device.PowerUpdateDelegate {
    private static final String LOG_TAG = FlirProxy.class.getSimpleName();

    private AppCompatActivity activityContext;
    private Device device;

    private MutableLiveData<DeviceState> deviceState = new MutableLiveData<>();
    private MutableLiveData<Device.BatteryChargingState> batteryChargingState = new MutableLiveData<>();
    private MutableLiveData<Byte> batteryPercentage = new MutableLiveData<>();
    private MutableLiveData<Float> lowerAccuracyBound = new MutableLiveData<>();
    private MutableLiveData<Float> upperAccuracyBound = new MutableLiveData<>();
    private MutableLiveData<Device.TuningState> tuningState = new MutableLiveData<>();
    private MutableLiveData<Frame> frame = new MutableLiveData<>();

    public FlirProxy(AppCompatActivity activity)  {
        activityContext = activity;
        activityContext.getLifecycle().addObserver(this);

        resetDeviceData();
        frame.postValue(null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        try {
            Device.startDiscovery(activityContext, this);
        } catch(IllegalStateException e) {
            Log.w(LOG_TAG, "Tried to start discovery multiple times in succession");
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
        deviceState.postValue(DeviceState.Disconnected);
        batteryChargingState.postValue(null);
        batteryPercentage.postValue(null);
        lowerAccuracyBound.postValue(null);
        upperAccuracyBound.postValue(null);
        tuningState.postValue(null);
    }

    @Override
    public void onDeviceConnected(Device connectedDevice) {
        Log.i(LOG_TAG, "Device connected");

        device = connectedDevice;

        SharedPreferences preferences = Preferences.getPreferences(activityContext);
        String key = activityContext.getString(R.string.flirsettings_automatictuning_key);
        boolean defaultValue = activityContext.getResources().getBoolean(R.bool.flirsettings_automatictuning_default);
        setAutomaticTuning(preferences.getBoolean(key, defaultValue));

        if (device instanceof FlirUsbDevice) deviceState.postValue(DeviceState.Usb);
        else if (device instanceof EmbeddedDevice) deviceState.postValue(DeviceState.Embedded);
        else deviceState.postValue(DeviceState.Connected);

        float lower = (float)Converter.kelvinToCelsius(device.getLowerAccuracyBound());
        float upper = (float)Converter.kelvinToCelsius(device.getUpperAccuracyBound());
        lowerAccuracyBound.postValue(lower);
        upperAccuracyBound.postValue(upper);

        device.setPowerUpdateDelegate(this);
        device.startFrameStream(this);
    }

    @Override
    public void onDeviceDisconnected(Device disconnectedDevice) {
        Log.i(LOG_TAG, "Device disconnected");
        device = null;
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
    public void onAutomaticTuningChanged(boolean enabled) {
        String key = activityContext.getString(R.string.flirsettings_automatictuning_key);
        Preferences.getPreferences(activityContext).edit()
                .putBoolean(key, enabled)
                .apply();
    }

    @Override
    public void onFrameReceived(Frame frame) {
        this.frame.postValue(frame);
    }

    public void setAutomaticTuning(boolean enabled) {
        if (device != null)
            device.setAutomaticTuning(enabled);
    }

    public boolean performTuning() {
        if (device == null) return false;
        device.performTuning();
        return true;
    }

    public void closeSimulatedDevice() {
        if (device instanceof SimulatedDevice)
            device.close();
    }

    public LiveData<DeviceState> getDeviceState() {
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

    public LiveData<Frame> getFrame() {
        return frame;
    }

    public enum DeviceState {
        Disconnected, Connected, Usb, Embedded
    }
}
