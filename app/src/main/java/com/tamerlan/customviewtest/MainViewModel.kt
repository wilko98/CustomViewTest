package com.tamerlan.customviewtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {
    private var sides = 5
    private var numberOfShapes = 3
    private val draws = mutableListOf<DrawModel>()

    //  frame rate
    private val drawDelay = 12L
    private var maxScreenSize = 0f
    fun setMaxScreenSize(size: Float) {
        maxScreenSize = size
        numberOfShapes = (maxScreenSize / 150).toInt()
        generateDraws()
    }

    fun generateDraws() {
        if (draws.isEmpty())
            draws.apply {
                for (i in 1..numberOfShapes) {
                    add(DrawModel(i * 150f))
                }
            }
    }

    val uiState: MutableStateFlow<DrawUiState> =
        MutableStateFlow(
            DrawUiState(
                draws,
                gyroscopeValues = GyroscopeValues(0f, 0f, 0f),
                sides = sides
            )
        )

    init {
        startToDrawShapes()
    }

    private fun startToDrawShapes() {
        viewModelScope.launch {
            while (true) {
                delay(drawDelay)
                draws.forEachIndexed { index, it ->
                    it.size += 5
                    if (it.size > maxScreenSize) {
                        it.size = 50f
                    }
                }
                uiState.update {
                    DrawUiState(
                        draws,
                        uiState.replayCache.last().gyroscopeValues,
                        !uiState.value.change,
                        sides
                    )
                }
            }
        }
    }

    fun setGyroscopeValues(gyroscopeValues: GyroscopeValues) {
        uiState.update {
            DrawUiState(
                draws,
//                calculateNewRelativeValues(
//                    uiState.replayCache.last().gyroscopeValues,
//                    gyroscopeValues
//                ),
                gyroscopeValues,
                !uiState.value.change,
                sides
            )
        }
    }

    fun increaseSides() {
        sides++
    }
}

data class DrawUiState(
    val models: List<DrawModel>,
    val gyroscopeValues: GyroscopeValues,
    val change: Boolean = true,
    var sides: Int
)

data class DrawModel(
    var size: Float
)

data class GyroscopeValues(
    val x: Float,
    val y: Float,
    val z: Float
)