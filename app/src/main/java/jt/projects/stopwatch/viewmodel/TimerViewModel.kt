package jt.projects.stopwatch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jt.projects.App
import jt.projects.stopwatch.model.datasource.StopwatchStateCalculator
import jt.projects.stopwatch.model.datasource.StopwatchStateHolder
import jt.projects.stopwatch.model.repo.ElapsedTimeCalculator
import jt.projects.stopwatch.utils.TimestampMillisecondsFormatter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class TimerViewModel(
    private val stopwatchStateHolder: StopwatchStateHolder =
        StopwatchStateHolder(
            StopwatchStateCalculator(
                App.instance.timestampProvider,
                ElapsedTimeCalculator(App.instance.timestampProvider)
            ),
            ElapsedTimeCalculator(App.instance.timestampProvider),
            TimestampMillisecondsFormatter()
        ),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : ViewModel() {
    private val mutableTicker: MutableStateFlow<String> =
        MutableStateFlow(TimestampMillisecondsFormatter.DEFAULT_TIME)

    val liveData: LiveData<String> = mutableTicker.asLiveData()

    private var job: Job? = null

    fun startTimer() {
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

    fun pauseTimer() {
        stopwatchStateHolder.pause()
        stopJob()
    }

    fun stopTimer() {
        stopwatchStateHolder.stop()
        stopJob()
        clearValue()
    }

    private fun stopJob() {
        scope.coroutineContext.cancelChildren()
        job = null
    }

    private fun clearValue() {
        mutableTicker.value = TimestampMillisecondsFormatter.DEFAULT_TIME
    }
}

