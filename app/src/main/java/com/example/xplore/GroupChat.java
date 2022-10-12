package com.example.xplore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.adapters.AdapterGroupChat;
import com.example.xplore.models.ModelGroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChat extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    String groupId,myGroupRole="";
    private Toolbar toolbar;
    private ImageView groupIconIv;
    private TextView groupTitleTv;
    private ImageButton attachBtn,sendBtn,videobtn;
    private EditText messageEt;
    private RecyclerView chatRv;
    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;


    private static  final int CAMERA_REQUEST_CODE=200;
    private static  final int STORAGE_REQUEST_CODE=400;

    private static  final int IMAGE_PICK_GALLERY_CODE=1000;
    private static  final int IMAGE_PICK_CAMERA_CODE=2000;

    String cameraPermissions[];
    String storagePermissions[];

    private Uri image_uri=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar=findViewById(R.id.toolbar);
        groupIconIv=findViewById(R.id.groupIconIv);
        groupTitleTv=findViewById(R.id.groupTitleTv);
        attachBtn=findViewById(R.id.attachBtn);
        messageEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);
        chatRv=findViewById(R.id.chatRv);
        videobtn=findViewById(R.id.videocallbtn);

        /*LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        chatRv.setHasFixedSize(true);
        chatRv.setLayoutManager(linearLayoutManager);*/

        //setSupportActionBar(toolbar);





        Intent intent=getIntent();
        groupId=intent.getStringExtra("groupId");

        cameraPermissions=new String[]{
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
        };


        firebaseAuth=FirebaseAuth.getInstance();
        loadgroupinfo();
        loadgroupmessages();
        loadmygrouprole();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message=messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChat.this, "Cant send empty Message", Toast.LENGTH_SHORT).show();
                }else{
                    sendmessage(message);
                }
            }
        });
        videobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupChat.this, videocall.class));
                finish();
            }
        });


        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showimageimportdialogue();

            }
        });


    }

    private void loadmygrouprole() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();
                            invalidateOptionsMenu();


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showimageimportdialogue() {

        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick image ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkcamerapermission()){
                        requestcamerapermission();

                    }else{
                        pickcamera();
                    }



                }else {
                    if(!checkstoragepermission()){
                        requeststoragepermission();
                    }else{
                        pickgallery();
                    }


                }

            }
        })

                .show();


    }
    private boolean checkstoragepermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requeststoragepermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkcamerapermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestcamerapermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }


    private void pickcamera() {
        ContentValues contentvalues=new ContentValues();
        contentvalues.put(MediaStore.Images.Media.TITLE,"GroupImageTitle");
        contentvalues.put(MediaStore.Images.Media.DESCRIPTION,"GroupImageDescription");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentvalues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }

    private void pickgallery() {
        Intent intent= new Intent( Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }


    private void loadgroupmessages() {
        groupChatList=new ArrayList<>();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelGroupChat model=ds.getValue(ModelGroupChat.class);
                            groupChatList.add(model);

                        }
                        adapterGroupChat=new AdapterGroupChat(GroupChat.this,groupChatList);
                        //Harsha
                        LinearLayoutManager manager= new LinearLayoutManager(GroupChat.this);
                        chatRv.setLayoutManager(manager);
                        manager.setStackFromEnd(true);
                        chatRv.setHasFixedSize(true);//


                        chatRv.setAdapter(adapterGroupChat);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void sendmessage(String message) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        String timestamp=""+System.currentTimeMillis();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());

        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);

        hashMap.put("type",""+"text");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        messageEt.setText("");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChat.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }

   private void loadgroupinfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds:snapshot.getChildren()){
                            String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();
                            groupTitleTv.setText(groupTitle);
                            try{
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);

                            }catch(Exception e) {
                                groupIconIv.setImageResource(R.drawable.ic_group_white);
                            }



                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickcamera();

                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickgallery();

                    } else {
                        Toast.makeText(this, "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
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
                image_uri=data.getData();
                sendImageMessage();




            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                sendImageMessage();


            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage() {

        ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setMessage("Sending Image");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        String filenamePath="ChatImages/"+""+System.currentTimeMillis();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filenamePath);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> p_uriTask =taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();

                        if(p_uriTask.isSuccessful()){

                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
                            String timestamp=""+System.currentTimeMillis();
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender",""+firebaseAuth.getUid());

                            hashMap.put("message",""+p_downloadUri);
                            hashMap.put("timestamp",""+timestamp);

                            hashMap.put("type",""+"image");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            messageEt.setText("");
                                            pd.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(GroupChat.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    pd.dismiss();

                                }
                            });
                        }
                        
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChat.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
        


    }
    //By Harsha
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        if(myGroupRole.equals("creator")||myGroupRole.equals("admin")){

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.action_groupinfo){
            Intent intent=new Intent(this,GroupInfo.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }
}
