package com.example.xplore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Button mregister,mlogin;
    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mregister=findViewById(R.id.btn_signup);
        mlogin=findViewById(R.id.btn_login);
        firebaseAuth=FirebaseAuth.getInstance();
        checkuserstatus();
        mregister.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this,register.class));
            }



        });
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,login.class));

            }
        });
    }
    private void checkuserstatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user !=null){
            startActivity(new Intent(MainActivity.this,dashboard.class));
            finish();



            //mprofileTv.setText(user.getEmail());





        }
        else{


        }
    }



}