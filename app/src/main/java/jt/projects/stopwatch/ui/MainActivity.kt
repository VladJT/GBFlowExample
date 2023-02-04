package jt.projects.stopwatch.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import jt.projects.flowexample.R
import jt.projects.randomint.MainViewModel
import jt.projects.searchactivity.SearchActivity
import jt.projects.stopwatch.viewmodel.TimerViewModel

class MainActivity : AppCompatActivity() {

    private val timerViewModel: TimerViewModel by lazy {
        TimerViewModel()
    }

    private val timerViewModel2: TimerViewModel by lazy {
        TimerViewModel()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textViewMsg = findViewById<TextView>(R.id.message)

        ViewModelProvider(this)[MainViewModel::class.java].liveData.observe(this) {
            textViewMsg.text = it.data
        }

        timerViewModel.liveData.observe(this) {
            findViewById<TextView>(R.id.text_time).text = it
        }

        timerViewModel2.liveData.observe(this) {
            findViewById<TextView>(R.id.text_time2).text = it
        }

        findViewById<Button>(R.id.button_start).setOnClickListener {
            timerViewModel.startTimer()
        }

        findViewById<Button>(R.id.button_start2).setOnClickListener {
            timerViewModel2.startTimer()
        }

        findViewById<Button>(R.id.button_pause).setOnClickListener {
            timerViewModel.pauseTimer()
            timerViewModel2.pauseTimer()
        }
        findViewById<Button>(R.id.button_stop).setOnClickListener {
            timerViewModel.stopTimer()
            timerViewModel2.stopTimer()
        }


        findViewById<Button>(R.id.button_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

    }
}