package com.daftneeks.julius.flashstudy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.daftneeks.julius.flashstudy.slides.FirstSlide;
import com.daftneeks.julius.flashstudy.slides.FourthSlide;
import com.daftneeks.julius.flashstudy.slides.SecondSlide;
import com.daftneeks.julius.flashstudy.slides.ThirdSlide;
import com.github.paolorotolo.appintro.AppIntro2;


public class IntroActivity extends AppIntro2 {

    public SharedPreferences sharedPreferences;
    public boolean aBoolean;

    @Override
    public void init(Bundle bundle) {

        sharedPreferences = getSharedPreferences("preferences", 0);
        aBoolean = sharedPreferences.getBoolean("firstCommit", true);

        if (aBoolean) {

        addSlide(new FirstSlide(), getApplicationContext());
        addSlide(new SecondSlide(), getApplicationContext());
        addSlide(new ThirdSlide(), getApplicationContext());
        addSlide(new FourthSlide(), getApplicationContext());

        setCustomTransformer(new ZoomOutPageTransformer());
        setVibrate(true);
        setVibrateIntensity(40);
        }

        else {
            loadMainActivity();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void loadMainActivity(){

        sharedPreferences = getSharedPreferences("preferences", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstCommit", false);
        editor.apply();

        Intent intent = new Intent(this, LaunchActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    public void getStarted(View v){
        loadMainActivity();
    }
}
