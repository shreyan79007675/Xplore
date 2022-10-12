package com.example.xplore.fragments;

import static android.app.Activity.RESULT_OK;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.xplore.MainActivity;
import com.example.xplore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

import java.util.HashMap;


public class ProfileFragment extends Fragment  {

    FirebaseAuth firebaseauth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    ImageView avatariv,coveriv;
    TextView nametv,emailtv,phonetv ;
    FloatingActionButton fab;
    ProgressDialog pd;
    private static  final int CAMERA_REQUEST_CODE=100;
    private static  final int STORAGE_REQUEST_CODE=200;

    private static  final int IMAGE_PICK_GALLERY_CODE=300;
    private static  final int IMAGE_PICK_CAMERA_CODE=400;
    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri;
    String profileorcoverphoto;
    String storagepath="Users_Profile_Cover_Imgs/";



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile,container,false);
        firebaseauth=FirebaseAuth.getInstance();
        user=firebaseauth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference= FirebaseStorage.getInstance().getReference();

        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        avatariv=view.findViewById(R.id.avatarIv);
        coveriv=view.findViewById(R.id.coverIv);
        fab=view.findViewById(R.id.fab);
        nametv=view.findViewById(R.id.nameTv);
        emailtv=view.findViewById(R.id.emailTv);
        phonetv=view.findViewById(R.id.phoneTv);
        pd=new ProgressDialog(getActivity());
        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    nametv.setText(name);
                    emailtv.setText(email);
                    phonetv.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatariv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatariv);

                    }
                    try {
                        Picasso.get().load(cover).into(coveriv);
                    }catch (Exception e){


                    }




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showeditprofiledialog();
            }
        });

        return view;
    }
    private boolean checkstoragepermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requeststoragepermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkcamerapermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestcamerapermission(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showeditprofiledialog() {
        String options[]={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone","Change Password"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    pd.setMessage("Updating Profile Picture...");
                    profileorcoverphoto="image";
                    showimagepicdialog();


                }else if(i == 1){
                    pd.setMessage("Updating cover Photo...");
                    profileorcoverphoto="cover";
                    showimagepicdialog();


                }else if(i == 2){
                    pd.setMessage("Updating Name...");
                    shownamephoneupdatedialog("name");

                }else if(i == 3){
                    pd.setMessage("Updating Phone...");
                    shownamephoneupdatedialog("phone");

                }
                else if(i == 4){
                    pd.setMessage("Changing Password");
                    showchangepassworddialog();

                }

            }
        });
        builder.create().show();
    }

    private void showchangepassworddialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);
        EditText passwordET = view.findViewById(R.id.passwordET);
        EditText newPasswordET = view.findViewById(R.id.newPasswordET);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();


        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = passwordET.getText().toString().trim();
                String newPassword = newPasswordET.getText().toString().trim();
                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must be atleast 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword,newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        pd.show();
        FirebaseUser user=firebaseauth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(),oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shownamephoneupdatedialog(String key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        LinearLayout linaerla=new LinearLayout(getActivity());
        linaerla.setOrientation(LinearLayout.VERTICAL);
        linaerla.setPadding(10,10,10,10);
        EditText edittext=new EditText(getActivity());
        edittext.setHint("Enter"+key);
        linaerla.addView(edittext);
        builder.setView(linaerla);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value=edittext.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result=new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Updated..",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

                }else{
                    Toast.makeText(getActivity(),"Please Enter"+key,Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.create().show();
    }

    private void showimagepicdialog() {
        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickfromcamera();

                    }else{
                        Toast.makeText(getActivity(), "Please Enable Camera and Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){

                    boolean writeStorageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickfromgallery();

                    }else{
                        Toast.makeText(getActivity(), "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
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
                Uploadprofilecoverphoto(image_uri);

            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                Uploadprofilecoverphoto(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void Uploadprofilecoverphoto(Uri uri) {
        pd.show();
        String filepathandname=storagepath+""+profileorcoverphoto+" "+user.getUid();
        StorageReference storageReference2=storageReference.child(filepathandname);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                Uri downloadUri=uriTask.getResult();
                if(uriTask.isSuccessful()){
                    HashMap<String,Object> results=new HashMap<>();
                    results.put(profileorcoverphoto,downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Image Updated...",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Failed..",Toast.LENGTH_SHORT).show();

                        }
                    });

                }else{
                    pd.dismiss();
                    Toast.makeText(getActivity(),"Some Error Occured",Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void pickfromcamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void pickfromgallery() {
        Intent galleryIntent= new Intent( Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);

    }
    private void checkuserstatus(){
        FirebaseUser user =firebaseauth.getCurrentUser();
        if(user !=null){
            //mprofileTv.setText(user.getEmail());



        }
        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();

        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false); //By Harsha
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        super.onCreateOptionsMenu(menu,inflater);
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