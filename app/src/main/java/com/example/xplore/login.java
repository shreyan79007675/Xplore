package com.example.xplore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class login extends AppCompatActivity {

    private static final int Rc_SIGN_IN=100;
    GoogleSignInClient mGoogleSignInClient;


    EditText mEmailEt,mPasswordEt;
    TextView nothaveaccountTV,mrecoverpass;
    Button mlogin;
    SignInButton mgoogle;

    private FirebaseAuth mAuth;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("LOGIN");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);




        mAuth = FirebaseAuth.getInstance();
        mEmailEt = findViewById(R.id.emailET);
        mPasswordEt = findViewById(R.id.passwordET);
        nothaveaccountTV = findViewById(R.id.nothave_accountTv);
        mrecoverpass = findViewById(R.id.recoverPassTv);
        mgoogle=findViewById(R.id.googleLoginBtn);

        mlogin = findViewById(R.id.loginbtn);

        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailt = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(emailt).matches()) {
                    mEmailEt.setError("Invalid Email..");
                    mEmailEt.setFocusable(true);
                } else {
                    loginuser(emailt, passw);
                }

            }
        });
        nothaveaccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, register.class));
                finish();
            }
        });
        mrecoverpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showrecoverpassworddialog();
            }
        });

        mgoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signInIntent=mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,Rc_SIGN_IN);

            }
        });
        pd=new ProgressDialog(this);
        pd.setMessage("Logging in....");
    }
    private void showrecoverpassworddialog() {
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout=new LinearLayout(this);
        EditText emailEt=new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,100,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int Which) {
                String email=emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int Which) {
                pd.dismiss();

            }
        });

        builder.create().show();

    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending email......");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(login.this,"Email Sent",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(login.this,"Failed....",Toast.LENGTH_SHORT).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(login.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

        private void loginuser (String emailt, String passw){
            pd.setMessage("Logging in......");
            pd.show();


            mAuth.signInWithEmailAndPassword(emailt, passw)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                FirebaseUser user = mAuth.getCurrentUser();

                                startActivity(new Intent(login.this, dashboard.class));
                                finish();
                            } else {
                                pd.dismiss();
                                Toast.makeText(login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(login.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Rc_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch(ApiException e){
                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){

        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user=mAuth.getCurrentUser();
                    if(task.getResult().getAdditionalUserInfo().isNewUser()){
                        String email =user.getEmail();
                        String uid = user.getUid();


                        HashMap<Object,String> hashMap =new HashMap<>();
                        hashMap.put("email",email);
                        hashMap.put("uid",uid);
                        hashMap.put("phone","");
                        hashMap.put("name","");
                        hashMap.put("onlinestatus","Online");
                        hashMap.put("typingto","None");
                        hashMap.put("image","");
                        hashMap.put("cover","");
                        FirebaseDatabase database=FirebaseDatabase.getInstance();
                        DatabaseReference reference=database.getReference("Users");
                        reference.child(uid).setValue(hashMap);

                    }




                    Toast.makeText(login.this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(login.this, dashboard.class));
                    finish();


                }else{
                    Toast.makeText(login.this,"Login failed.....",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(login.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }
}