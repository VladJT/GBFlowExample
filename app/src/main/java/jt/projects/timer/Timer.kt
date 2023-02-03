package jt.projects.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Определим возможные состояния для нашего секундомера. Их будет всего два: секундомер работает
и секундомер на паузе. Этого более чем достаточно для нашего простого приложения.
Состояние Running будет содержать информацию о том, когда секундомер был запущен (в
миллисекундах) и сколько времени прошло с момента запуска. В момент запуска прошедшее время
будет == 0. Но каждый раз после паузы это значение будет меняться, чтобы потом мы могли верно
рассчитать время.
Состояние Paused будет содержать только информацию о том, сколько времени прошло (в
миллисекундах).

 */
sealed class StopwatchState {
    data class Paused(val elapsedTime: Long) : StopwatchState()
    data class Running(val startTime: Long, val elapsedTime: Long) : StopwatchState()
}

//Текущее время, нужное для запуска секундомера, мы будем брать через интерфейс.
interface TimestampProvider {
    fun getMs(): Long
}

class StopwatchStateCalculator(
    private val timestampProvider: TimestampProvider,
    private val elapsedTimeCalculator: ElapsedTimeCalculator
) {
    fun calculateRunningState(oldstate: StopwatchState): StopwatchState.Running =
        when (oldstate) {
            is StopwatchState.Running -> oldstate
            is StopwatchState.Paused -> {
                StopwatchState.Running(
                    startTime = timestampProvider.getMs(),
                    elapsedTime = oldstate.elapsedTime
                )
            }
        }


    fun calculatePausedState(oldState: StopwatchState): StopwatchState.Paused =
        when (oldState) {
            is StopwatchState.Running -> StopwatchState.Paused(
                elapsedTime = elapsedTimeCalculator.calculate(
                    oldState
                )
            )
            is StopwatchState.Paused -> oldState
        }
}

class ElapsedTimeCalculator(private val timestampProvider: TimestampProvider) {

    fun calculate(state: StopwatchState.Running): Long {
        val currentTimestamp = timestampProvider.getMs()
        val timePassedSinceStart = if (currentTimestamp > state.startTime) {
            currentTimestamp - state.startTime
        } else {
            0
        }
        return timePassedSinceStart + state.elapsedTime
    }
}


class TimestampMillisecondsFormatter() {
    fun format(timestamp: Long): String {
        val millisecondsFormatted = (timestamp % 1000).pad(3)
        val seconds = timestamp / 1000
        val secondsFormatted = (seconds % 60).pad(2)
        val minutes = seconds / 60
        val minutesFormatted = (minutes % 60).pad(2)
        val hours = minutes / 60
        return if (hours > 0) {
            val hoursFormatted = (minutes / 60).pad(2)
            "$hoursFormatted:$minutesFormatted:$secondsFormatted"
        } else {
            "$minutesFormatted:$secondsFormatted:$millisecondsFormatted"
        }
    }

    private fun Long.pad(desiredLength: Int) =
        this.toString().padStart(desiredLength, '0')

    companion object {
        const val DEFAULT_TIME = "00:00:000"
    }
}


class StopwatchStateHolder(
    private val stopwatchStateCalculator: StopwatchStateCalculator,
    private val elapsedTimeCalculator: ElapsedTimeCalculator,
    private val timestampMillisecondsFormatter: TimestampMillisecondsFormatter,
) {
    var currentState: StopwatchState = StopwatchState.Paused(0)
        private set

    fun start() {
        currentState = stopwatchStateCalculator.calculateRunningState(currentState)
    }

    fun pause() {
        currentState = stopwatchStateCalculator.calculatePausedState(currentState)
    }

    fun stop() {
        currentState = StopwatchState.Paused(0)
    }

    fun getStringTimeRepresentation(): String {
        val elapsedTime = when (val currentState = currentState) {
            is StopwatchState.Paused -> currentState.elapsedTime
            is StopwatchState.Running ->
                elapsedTimeCalculator.calculate(currentState)
        }
        return timestampMillisecondsFormatter.format(elapsedTime)
    }
}


class StopwatchListOrchestrator(
    private val stopwatchStateHolder: StopwatchStateHolder,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null
    private val mutableTicker = MutableStateFlow("")

    val ticker: StateFlow<String> = mutableTicker

    fun start() {
        if (job == null) startJob()
        stopwatchStateHolder.start()
    }

    private fun startJob() {
        scope.launch {
            while (isActive) {
                mutableTicker.value =
                    stopwatchStateHolder.getStringTimeRepresentation()
                delay(20)
            }
        }
    }

    fun pause() {
        stopwatchStateHolder.pause()
        stopJob()
    }

    fun stop() {
        stopwatchStateHolder.stop()
        stopJob()
        clearValue()
    }

    private fun stopJob() {
        scope.coroutineContext.cancelChildren()
        job = null
    }

    private fun clearValue() {
        mutableTicker.value = ""
    }
}
