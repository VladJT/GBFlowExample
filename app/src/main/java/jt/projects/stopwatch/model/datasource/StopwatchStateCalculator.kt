package jt.projects.stopwatch.model.datasource

import jt.projects.stopwatch.model.domain.StopwatchState
import jt.projects.stopwatch.model.repo.ElapsedTimeCalculator
import jt.projects.stopwatch.utils.TimestampProvider

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