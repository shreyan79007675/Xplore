package com.example.xplore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.xplore.fragments.HomeFragment;
import com.example.xplore.fragments.ProfileFragment;
import com.example.xplore.fragments.UsersFragment;
import com.example.xplore.fragments.chatlist;
import com.example.xplore.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class dashboard extends AppCompatActivity {
    FirebaseAuth firebaseauth;

    ActionBar actionBar;

    String mUID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar=getSupportActionBar();
        firebaseauth = FirebaseAuth.getInstance();
        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);




        actionBar.setTitle("Home");
        HomeFragment fragment1 =new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();

        checkuserstatus();

        updateToken(FirebaseInstanceId.getInstance().getToken());


    }
    @Override
    protected void onResume() {
        checkuserstatus();
        super.onResume();
    }

    //Notification
    public  void updateToken(String token){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);

    }


    private  BottomNavigationView.OnNavigationItemSelectedListener selectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment fragment1=new HomeFragment();
                    FragmentTransaction ft1= getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content,fragment1,"");
                    ft1.commit();
                    return  true;
                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment2=new ProfileFragment();
                    FragmentTransaction ft2= getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content,fragment2,"");
                    ft2.commit();
                    return  true;
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment fragment3=new UsersFragment();
                    FragmentTransaction ft3= getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content,fragment3,"");
                    ft3.commit();
                    return  true;
                case R.id.nav_chat:
                    actionBar.setTitle("Chats");
                    chatlist fragment4=new chatlist();
                    FragmentTransaction ft4= getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content,fragment4,"");
                    ft4.commit();
                    return  true;

            }
            return false;
        }
    };
    private void checkuserstatus(){
        FirebaseUser user =firebaseauth.getCurrentUser();
        if(user !=null){


            //mprofileTv.setText(user.getEmail());
            mUID=user.getUid();
            SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();





        }
        else{
            startActivity(new Intent(dashboard.this,MainActivity.class));
            finish();

        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }



    @Override
    protected void onStart() {
        checkuserstatus();
        super.onStart();

    }


}