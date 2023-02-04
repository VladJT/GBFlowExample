package jt.projects.stopwatch.utils

//Текущее время, нужное для запуска секундомера
interface TimestampProvider {
    fun getMs(): Long
}