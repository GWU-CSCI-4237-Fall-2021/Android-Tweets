package edu.gwu.androidtweetsfall2021

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.lang.Math.sqrt
import kotlin.math.abs
import kotlin.math.pow

class ShakeManager(context: Context) : SensorEventListener {

    private val SHAKE_THRESHOLD = 5

    private val MIN_TIME_BETWEEN_SHAKES = 2000

    private var lastShakeTime: Long = 0L

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var shakeListener: (() -> Unit)? = null

    fun startDetectingShakes(shakeListener: () -> Unit) {
        this.shakeListener = shakeListener

        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).isNotEmpty()) {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            Log.e("ShakeManager", "Device has no accelerometer!")
            stopDetectingShakes()
        }
    }

    fun stopDetectingShakes() {
        shakeListener = null
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = sqrt(
            x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2)
        ) - SensorManager.GRAVITY_EARTH

        val formatted = String.format("[X, Y, Z] = [%.4f, %.4f, %.4f]; Acceleration = %.4f", x, y, z, acceleration)
        Log.d("ShakeManager", formatted)

        if (abs(acceleration) > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - lastShakeTime
            if (timeDiff > MIN_TIME_BETWEEN_SHAKES) {
                Log.d("ShakeManager", "Shake detected!")
                lastShakeTime = currentTime
                shakeListener?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}