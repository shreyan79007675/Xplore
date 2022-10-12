package com.example.xplore.adapters;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.xplore.R;
import com.example.xplore.ThereProfileActivity;
import com.example.xplore.chat;
import com.example.xplore.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    String uid;
    String myuid;

    public AdapterUser(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    List<ModelUsers> list;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        final String hisuid = list.get(position).getUid();
        String userImage = list.get(position).getImage();
        String username = list.get(position).getName();
        String usermail = list.get(position).getEmail();
        holder.name.setText(username);
        holder.email.setText(usermail);
        try {

            Picasso.get().load(userImage).into(holder.profiletv);

        } catch (Exception e) {
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent=new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("hisUid",hisuid);
                            context.startActivity(intent);


                        }
                        if(i==1){
                            imblockedornot(hisuid);



                        }
                    }
                });
                builder.create().show();


            }
        });




        holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        checkisblocked(hisuid,holder,position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, chat.class);
                intent.putExtra("hisUid",hisuid);
                context.startActivity(intent);
            }
        });
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(list.get(position).isBlocked()){
                    unblockuser(hisuid);
                }
                else{
                    blockuser(hisuid);
                }

            }
        });

    }
    private void imblockedornot(String hisuid){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisuid).child("BlockedUsers").orderByChild("uid").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "You are Blocked By user cant Send message", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent=new Intent(context,chat.class);
                        intent.putExtra("hisUid",hisuid);
                        context.startActivity(intent);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void checkisblocked(String hisuid, MyHolder holder, int position) {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("BlockedUsers").orderByChild("uid").equalTo(hisuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.exists()){
                        holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                        list.get(position).setBlocked(true);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void blockuser(String hisuid) {
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",hisuid);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("BlockedUsers").child(hisuid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Blocked Successfully", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void unblockuser(String hisuid) {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("BlockedUsers").orderByChild("uid").equalTo(hisuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(context, "Unblocked Successfully..", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();

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

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name, email;
        ImageView blockIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagep);
            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
            blockIv=itemView.findViewById(R.id.blockIv);
        }
    }
}

