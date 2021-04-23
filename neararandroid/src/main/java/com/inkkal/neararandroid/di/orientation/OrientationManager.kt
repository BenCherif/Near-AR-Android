package com.inkkal.neararandroid.di.orientation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import android.view.WindowManager
import androidx.annotation.Nullable
import com.inkkal.neararandroid.models.OrientationModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


@Suppress("MagicNumber")
internal class OrientationManager @Inject constructor(
    private val sensorManager: SensorManager,
    private val windowManager: WindowManager
) {

    private var alpha = 0f
    private var lastCos = 0f
    private var lastSin = 0f
    private var sensorEventListener: SensorEventListener? = null

    // Not all devices have a compassSensor
    @Nullable
    private var compassSensor: Sensor? = null

    @Nullable
    private var gravitySensor: Sensor? = null

    @Nullable
    private var magneticFieldSensor: Sensor? = null

    private val orientationPublishObservable: Observable<SensorEvent> =
        Observable.create { emitter ->
            sensorEventListener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) =
                    when (sensor?.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> when (accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Timber.d("ACCURACY low")
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Timber.d("ACCURACY medium")
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Timber.d("ACCURACY high")
                            else -> Unit
                        }
                        else -> Unit
                    }

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) emitter.onNext(event)
                }
            }
            compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (compassSensor == null) {
                if (isGyroscopeAvailable()) {
                    Timber.d("Rotation vector sensor not supported on device, falling back to orientation.");
                    compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                } else {
                    Timber.d("Rotation vector sensor not supported on device, falling back to accelerometer and magnetic field.");
                    gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    magneticFieldSensor =
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                }
            }


            if (isCompassSensorAvailable()) {
                // Does nothing if the sensors already registered.
                //  sensorManager.registerListener(this, compassSensor, SENSOR_DELAY_MICROS);
                sensorManager.registerListener(
                    sensorEventListener,
                    compassSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            } else {
                sensorManager.registerListener(
                    sensorEventListener,
                    gravitySensor,
                    SensorManager.SENSOR_DELAY_GAME
                );
                sensorManager.registerListener(
                    sensorEventListener,
                    magneticFieldSensor,
                    SensorManager.SENSOR_DELAY_GAME
                );
            }
            emitter.setCancellable {
                //sensorManager.unregisterListener(sensorEventListener)
                if (isCompassSensorAvailable()) {
                    sensorManager.unregisterListener(sensorEventListener, compassSensor);
                } else {
                    sensorManager.unregisterListener(sensorEventListener, gravitySensor);
                    sensorManager.unregisterListener(sensorEventListener, magneticFieldSensor);
                }
            }
        }

    private fun isGyroscopeAvailable(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    }


    private fun isCompassSensorAvailable(): Boolean {
        return compassSensor != null
    }

    fun getSensorUpdates(): Flowable<OrientationModel> {
        return orientationPublishObservable.subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .toFlowable(BackpressureStrategy.LATEST)
            .map { sensorEvent: SensorEvent -> handleSensorEvent(sensorEvent) }
    }

    private fun handleSensorEvent(sensorEvent: SensorEvent): OrientationModel {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values)

        val adjustedRotationMatrix = getAdjustedRotationMatrix(rotationMatrix)

        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()

        val azimuth = lowPassDegreesFilter(orientation[0])

        return OrientationModel(azimuth, pitch)
    }

    private fun lowPassDegreesFilter(azimuthRadians: Float): Float {
        lastSin = alpha * lastSin + (1 - alpha) * sin(azimuthRadians)
        lastCos = alpha * lastCos + (1 - alpha) * cos(azimuthRadians)

        return ((Math.toDegrees(atan2(lastSin, lastCos).toDouble()) + 360) % 360).toFloat()
    }

    private fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val axisXY = getProperAxis()

        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, axisXY.first,
            axisXY.second, adjustedRotationMatrix
        )
        return adjustedRotationMatrix
    }

    private fun getProperAxis(): Pair<Int, Int> {
        val worldAxisX: Int
        val worldAxisY: Int
        when (windowManager.defaultDisplay?.rotation) {
            ROTATION_90 -> {
                worldAxisX = SensorManager.AXIS_Z
                worldAxisY = SensorManager.AXIS_MINUS_X
            }
            ROTATION_180 -> {
                worldAxisX = SensorManager.AXIS_MINUS_X
                worldAxisY = SensorManager.AXIS_MINUS_Z
            }
            ROTATION_270 -> {
                worldAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisY = SensorManager.AXIS_X
            }
            ROTATION_0 -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
            else -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
        }
        return Pair(worldAxisX, worldAxisY)
    }

    //TODO export alpha from OrientationProvider
    fun setLowPassFilterAlpha(lowPassFilterAlpha: Float) {
        alpha = lowPassFilterAlpha
    }
}
