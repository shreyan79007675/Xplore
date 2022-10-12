package com.example.xplore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.adapters.Adapterchat;
import com.example.xplore.models.ModelUsers;
import com.example.xplore.models.Modelchat;
import com.example.xplore.notifications.APIService;
import com.example.xplore.notifications.Client;
import com.example.xplore.notifications.Data;
import com.example.xplore.notifications.Response;
import com.example.xplore.notifications.Sender;
import com.example.xplore.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class chat extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTv,blockIv;
    TextView nameTv,userstatusTv;
    EditText messageEt;
    ImageButton sendBtn,attachBtn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersdbref;
    ValueEventListener seenListener;
    DatabaseReference userrefforseen;
    List<Modelchat> Chatlist;
    Adapterchat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;


    boolean isBlocked=false;
    private boolean notify=false;
    private static  final int CAMERA_REQUEST_CODE=100;
    private static  final int STORAGE_REQUEST_CODE=200;

    private static  final int IMAGE_PICK_GALLERY_CODE=300;
    private static  final int IMAGE_PICK_CAMERA_CODE=400;

    String cameraPermissions[];
    String storagePermissions[];

    Uri image_rui=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_recyclerView);
        profileTv=findViewById(R.id.profileIv);
        blockIv=findViewById(R.id.blockIv);
        nameTv=findViewById(R.id.nameIv);
        userstatusTv=findViewById(R.id.userstatusTv);
        messageEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);
        attachBtn=findViewById(R.id.attachBtn);
        userstatusTv=findViewById(R.id.userstatusTv);


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);






        Intent intent=getIntent();

        hisUid=intent.getStringExtra("hisUid");
        firebaseAuth=FirebaseAuth.getInstance();

        firebaseDatabase=FirebaseDatabase.getInstance();

        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        usersdbref=firebaseDatabase.getReference("Users");
        checkuserstatus();

        Query userquery=usersdbref.orderByChild("uid").equalTo(hisUid);
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();
                    String typingstatus=""+ds.child("typingto").getValue();


                    if(typingstatus.equals(hisUid)){
                        userstatusTv.setText("typing.....");
                    }else{
                        String onlinestatus=""+ds.child("onlinestatus").getValue();
                        if(onlinestatus.equals("Online")){
                            userstatusTv.setText(onlinestatus);
                        }
                        else{
                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlinestatus));
                            String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            userstatusTv.setText("Last seen" +datetime);
                        }
                    }




                    nameTv.setText(name);

                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileTv);

                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_default).into(profileTv);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                notify=true;

                String message=messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(chat.this,"Cannot send message...",Toast.LENGTH_SHORT).show();

                }else{
                    sendmessage(message);

                }
                messageEt.setText("");
            }
        });
        blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBlocked){
                    unblockuser();
                }
                else{
                    blockuser();
                }

            }
        });


        readmessages();
        checkisblocked();
        seenMessage();

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showimagepicdialog();

            }
        });

    }

    private void checkisblocked() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.exists()){
                        blockIv.setImageResource(R.drawable.ic_blocked_red);
                        isBlocked=true;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void blockuser() {
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",hisUid);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(chat.this, "Blocked Successfully", Toast.LENGTH_SHORT).show();
                        blockIv.setImageResource(R.drawable.ic_blocked_red);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(chat.this, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void unblockuser() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(chat.this, "Unblocked Successfully..", Toast.LENGTH_SHORT).show();
                                                blockIv.setImageResource(R.drawable.ic_unblocked_green);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(chat.this, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }


    private void showimagepicdialog() {
        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(chat.this);
        builder.setTitle("Pick image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkcamerapermission()){
                        requestcamerapermission();

                    }else{
                        pickfromcamera();
                    }



                }else if(i == 1){
                    if(!checkstoragepermission()){
                        requeststoragepermission();
                    }else{
                        pickfromgallery();
                    }


                }

            }
        });
        builder.create().show();
    }
    private boolean checkstoragepermission(){
        boolean result= ContextCompat.checkSelfPermission(chat.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requeststoragepermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkcamerapermission(){
        boolean result= ContextCompat.checkSelfPermission(chat.this,Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(chat.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestcamerapermission(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }
    private void pickfromcamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void pickfromgallery() {
        Intent galleryIntent= new Intent( Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);

    }

    private void seenMessage() {
        userrefforseen=FirebaseDatabase.getInstance().getReference("chats");
        seenListener=userrefforseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Modelchat chat=ds.getValue(Modelchat.class);
                    if(chat.getReciever().equals(myUid)&& chat.getSender().equals(hisUid)){
                        HashMap<String ,Object> hasseenhashmap=new HashMap<>();
                        hasseenhashmap.put("isseen",true);
                        ds.getRef().updateChildren(hasseenhashmap);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readmessages() {

        Chatlist=new ArrayList<>();
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Chatlist.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Modelchat chat=ds.getValue(Modelchat.class);

                    if(chat.getReciever().equals(myUid) && chat.getSender().equals(hisUid) || chat.getReciever().equals(hisUid) && chat.getSender().equals(myUid)){

                        Chatlist.add(chat);



                    }
                    adapterChat=new Adapterchat(chat.this,Chatlist,hisImage);


                    recyclerView.setAdapter(adapterChat);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendmessage(String message) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        String timestamp= String.valueOf(System.currentTimeMillis());

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("reciever",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isseen",false);
        hashMap.put("type","text");
        databaseReference.child("chats").push().setValue(hashMap);


        final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user=snapshot.getValue(ModelUsers.class);
                if(notify){
                    sennotifications(hisUid,user.getName(),message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        DatabaseReference chatref1=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);

        chatref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatref1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatref2=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);

        chatref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatref2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sennotifications(String hisUid, String name, String message) {

        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid,name+":"+message,"New Message",hisUid,R.drawable.ic_default_img);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendnotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(chat.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendImageMessage(Uri image_rui) throws IOException {
        notify=true;

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending image....");
        progressDialog.show();
        final String timestamp=""+System.currentTimeMillis();

        String filenameandpath="ChatImages/"+"post_"+timestamp;
        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        final byte[] data=baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filenameandpath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.dismiss();
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri=uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender",myUid);
                            hashMap.put("reciever",hisUid);
                            hashMap.put("message",downloadUri);
                            hashMap.put("timestamp",timestamp);
                            hashMap.put("type","image");
                            hashMap.put("isseen",false);

                            databaseReference.child("chats").push().setValue(hashMap);


                            final DatabaseReference chatref1=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);

                            chatref1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        chatref1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            final DatabaseReference chatref2=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);

                            chatref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        chatref2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });








                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }




    private void checkuserstatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user !=null){
            //mprofileTv.setText(user.getEmail());
            myUid= user.getUid();



        }
        else{
            startActivity(new Intent(this,MainActivity.class));
            finish();

        }
    }
    private void  checkonlinestatus(String status){
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlinestatus",status);
        dbref.updateChildren(hashMap);

    }
    private void  checktypingstatus(String typing){
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingto",typing);
        dbref.updateChildren(hashMap);

    }

    @Override
    protected void onStart() {

        checkuserstatus();
        checkonlinestatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {

        super.onPause();
        String timestamp= String.valueOf(System.currentTimeMillis());
        checkonlinestatus(timestamp);
        checktypingstatus("Noone");


        userrefforseen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {

        checkonlinestatus("Online");
        super.onResume();
    }



    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickfromcamera();

                    } else {
                        Toast.makeText(chat.this, "Please Enable Camera and Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickfromgallery();

                    } else {
                        Toast.makeText(chat.this, "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_rui=data.getData();
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false); //By Harsha
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkuserstatus();

        }
        return super.onOptionsItemSelected(item);
    }
}
