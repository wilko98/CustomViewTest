package com.tamerlan.customviewtest

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    lateinit var gyroscope: Sensor
    lateinit var mSensorManager: SensorManager
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainViewModel()
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        if (!isGyroscopeAvailable()) {
            AlertDialog
                .Builder(this)
                .setMessage("Gyroscope not available on your device")
                .setPositiveButton("Ok") { _, _ ->
                }
                .show()
        }
        val sdf = SimpleDateFormat("dd:MM:yyyy\nHH:mm:ss")
        setContent {
            val screenSize = maxOf(
                LocalConfiguration.current.screenHeightDp,
                LocalConfiguration.current.screenWidthDp
            )
            viewModel.setMaxScreenSize(screenSize.toFloat())
            val viewState by viewModel.uiState.collectAsState()
            Box {
                DrawScreenView(viewState)
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = sdf.format(Calendar.getInstance().time),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.white)
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                viewModel.setGyroscopeValues(
                    GyroscopeValues(
                        sensorEvent?.values?.get(1) ?: 0f,
                        sensorEvent?.values?.get(0) ?: 0f,
                        sensorEvent?.values?.get(2) ?: 0f
                    )
                )
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
        }, gyroscope, SensorManager.SENSOR_DELAY_UI)
    }

    fun isGyroscopeAvailable() =
        packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)


    @Composable
    fun DrawScreenView(viewState: DrawUiState) {
        val visibleCircleColor = colorResource(id = R.color.blackTransparent)
        val drawCirclesColor = colorResource(id = R.color.orangeToxic)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.radialGradient(
                colors = listOf(visibleCircleColor, Color.Black),
                center = Offset(
                    size.width / 2 + viewState.gyroscopeValues.x * 1500,
                    size.height / 2 + viewState.gyroscopeValues.y * 1500
                ),
            )
            drawRect(color = Color.Black, size = size)
            drawCircle(
                brush = gradient,
                alpha = 0.5f,
                center = Offset(
                    size.width / 2 + viewState.gyroscopeValues.x * 1500,
                    size.height / 2 + viewState.gyroscopeValues.y * 1500
                ),
                radius = 550f,
            )
            viewState.models.forEach {
                drawPath(
                    createCustomShapePath(
                        viewState.sides,
                        it.size,
                        Offset(size.width / 2, size.height / 2)
                    ),
                    color = drawCirclesColor,
                    style = Stroke(width = 7.dp.toPx()),
                    blendMode = BlendMode.Overlay
                )
            }
        }
    }
}

fun createCustomShapePath(sides: Int, radius: Float, center: Offset): Path {
    val path = Path()
    val angle = 2.0 * Math.PI / sides
    path.moveTo(
        center.x + (radius * Math.cos(0.0)).toFloat(),
        center.y + (radius * Math.sin(0.0)).toFloat()
    )
    for (i in sides downTo 0) {
        val xPc = center.x + (radius * Math.cos(angle * i)).toFloat()
        val yPc = center.y + (radius * Math.sin(angle * i)).toFloat()
        path.lineTo(xPc, yPc)
    }
    path.close()

    return path
}