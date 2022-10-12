package com.example.xplore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splashactivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashactivity);
        firebaseAuth=FirebaseAuth.getInstance();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser user =firebaseAuth.getCurrentUser();
                if(user ==null){
                    startActivity(new Intent(Splashactivity.this,MainActivity.class));
                    finish();



                    //mprofileTv.setText(user.getEmail());





                }
                else{
                    startActivity(new Intent(Splashactivity.this,dashboard.class));
                    finish();

                }
            }
        },2000);
    }

}