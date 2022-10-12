package com.example.xplore;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
    }

    private void checkuserstatus(){
        FirebaseUser user =firebaseauth.getCurrentUser();
        if(user !=null){
            //mprofileTv.setText(user.getEmail());



        }
        else{
            startActivity(new Intent(ThereProfileActivity.this,MainActivity.class));
            finish();

        }
    }

    @Override           //By Harsha
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id==R.id.action_logout){
            firebaseauth.signOut();
            checkuserstatus();

        }
        return super.onOptionsItemSelected(item);
    }

}