package com.example.maria.notificationscheduler

import android.app.job.JobInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.job.JobScheduler
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast

import android.content.ComponentName
import android.view.View

class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 0
    }

    private var mScheduler: JobScheduler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScheduler  = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler?

        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i > 0) {
                    seekBarProgress.text = getString(R.string.seconds, i)
                } else {
                    seekBarProgress.text=getString(R.string.not_set)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        scheduleJob_button.setOnClickListener {
            scheduleJob()
        }

        cancelJobs_button.setOnClickListener {
            cancelJobs()
        }
    }

    fun scheduleJob() {
        val selectedNetworkID = networkOptions.checkedRadioButtonId

        var selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE

        val seekBarInteger = mSeekBar.progress
        val seekBarSet = seekBarInteger > 0


        when (selectedNetworkID) {
            R.id.noNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE
            R.id.anyNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY
            R.id.wifiNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED
        }

        val serviceName = ComponentName(packageName,
                NotificationJobService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(idleSwitch.isChecked)
                .setRequiresCharging(chargingSwitch.isChecked)

        if (seekBarSet) {
            builder.setOverrideDeadline((seekBarInteger * 1000).toLong())
        }
        val constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
                || chargingSwitch.isChecked
                || idleSwitch.isChecked
                || seekBarSet)

        if (constraintSet) {
            val myJobInfo = builder.build()
            mScheduler?.schedule(myJobInfo)
            Toast.makeText(this, R.string.job_scheduled, Toast.LENGTH_SHORT)
                    .show()
        } else {
            Toast.makeText(this, R.string.no_constraint_toast,
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs() {

        if (mScheduler != null) {
            mScheduler?.cancelAll()
            mScheduler = null
            Toast.makeText(this, R.string.jobs_canceled, Toast.LENGTH_SHORT)
                    .show()
        }
    }
}
