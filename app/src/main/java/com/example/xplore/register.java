package com.example.xplore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class register extends AppCompatActivity {
    EditText memail,inpassword;
    Button mregbtn;
    TextView mhaveacctv;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
// ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar=getSupportActionBar();

        actionBar.setTitle("CREATE ACCOUNT");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        memail=findViewById(R.id.emailET);
        inpassword=findViewById(R.id.passwordET);
        mregbtn=findViewById(R.id.registerbtn);
        mAuth = FirebaseAuth.getInstance();
        mhaveacctv=findViewById(R.id.have_accountTv);


        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Registering User........");
        mregbtn.setOnClickListener(V -> {
            String email=memail.getText().toString().trim();
            String password=inpassword.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                memail.setError("Invalid Email");
                memail.setFocusable(true);
            }
            else if(password.length()<6){
                inpassword.setError("Password Length Atleast 6 Charecters");
                inpassword.setFocusable(true);
            }
            else{
                registeruser(email,password);
            }


        });
        mhaveacctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(register.this,login.class));
                finish();
            }
        });


    }

    private void registeruser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email =user.getEmail();
                            String uid = user.getUid();


                            HashMap<Object,String> hashMap =new HashMap<>();
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("phone","");
                            hashMap.put("name","");
                            hashMap.put("onlinestatus","Online");
                            hashMap.put("typingto","Noone");
                            hashMap.put("image","");
                            hashMap.put("cover","");
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            DatabaseReference reference=database.getReference("Users");
                            reference.child(uid).setValue(hashMap);







                            Toast.makeText(register.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(register.this, dashboard.class));

                            finish();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(register.this,"Authentication Failed.",Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(register.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}