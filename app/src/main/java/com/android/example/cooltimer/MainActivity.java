package com.android.example.cooltimer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SeekBar seekBar;
    TextView textView;
    Button startButton;
    private Boolean isTimerOn;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        startButton = findViewById(R.id.startButton);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isTimerOn = false;
        seekBar.setMax(600);
        setIntervalFromSharedPreferences(sharedPreferences);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long millisUntilFinished = progress * 1000;
                updateTimer(millisUntilFinished);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void startTimer(View view) {
        if (!isTimerOn) {
            startButton.setText("STOP");
            seekBar.setEnabled(false);
            isTimerOn = true;

            countDownTimer = new CountDownTimer(seekBar.getProgress()*1000 , 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer(millisUntilFinished);
                    seekBar.setProgress((int)millisUntilFinished/1000);
                }
                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable sound" , true)){
                        String melodyName = sharedPreferences.getString("timer_melody" , "bell");
                        if (melodyName.equals("bell")){
                            seekBar.setProgress(0);
                            setAlertDialog();
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                            mediaPlayer.start();
                        } else if (melodyName.equals("alarm_siren")) {
                            seekBar.setProgress(0);
                            setAlertDialog();
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                            mediaPlayer.start();
                        } else if (melodyName.equals("bip")) {
                            seekBar.setProgress(0);
                            setAlertDialog();
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                            mediaPlayer.start();
                        }
                    }

                    resetTimer();

                }
            };
            countDownTimer.start();
        }else {
            resetTimer();
        }

    }


    private void setAlertDialog (){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Time is running out")
                .setCancelable(false)
                .setNegativeButton("It's time to do something!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void updateTimer(long millisUntilFinished){
        int minutes = (int)millisUntilFinished/1000/60;
        int seconds = (int)millisUntilFinished/1000 - (minutes * 60);

        String minutesString = "";
        String secondsString = "";

        if (minutes < 10){
            minutesString = "0" + minutes;
        }else{
            String.valueOf(minutes);
        }

        if (seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = String.valueOf(seconds);
        }

        textView.setText(minutesString + ":" + secondsString);
    }

    private void resetTimer(){
        startButton.setText("START");
        countDownTimer.cancel();
        seekBar.setEnabled(true);
        setIntervalFromSharedPreferences(sharedPreferences);
        isTimerOn = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent openSettings = new Intent(this , SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this , AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences){

        defaultInterval = Integer.valueOf(sharedPreferences.getString("default_interval" , "30"));

        long defaultIntervalInMillis = defaultInterval*1000;

        updateTimer(defaultIntervalInMillis);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")){
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}

