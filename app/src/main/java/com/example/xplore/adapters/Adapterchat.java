package com.example.xplore.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.R;
import com.example.xplore.models.Modelchat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Adapterchat extends RecyclerView.Adapter<Adapterchat.MyHolder> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<Modelchat> chatList;
    String imageUrl;
    FirebaseUser fuser;


    public Adapterchat(Context context, List<Modelchat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);

        }else{
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);

        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        String message=chatList.get(position).getMessage();
        String timestamp=chatList.get(position).getTimestamp();
        String type =chatList.get(position).getType();
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();


        holder.messageTv.setText(message);
        holder.timeTv.setText(datetime);



        try{
            Picasso.get().load(imageUrl).into(holder.profileIv);


        }catch(Exception e){

        }
        if(type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);

        }else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);


        }


        holder.messagelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message..?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletemessages(position);


                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();


                    }

                });
                builder.create().show();
            }
        });


        if(position==chatList.size()-1){
            if(chatList.get(position).isIsseen()){
                holder.isSeenTv.setText("Seen..");

            }else{
                holder.isSeenTv.setText("Delivered..");
            }
        }else{
            holder.isSeenTv.setVisibility(View.GONE);

        }




    }

    private void deletemessages(int position) {
        String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgtimestamp=chatList.get(position).getTimestamp();
        DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("chats");
        Query query=dbref.orderByChild("timestamp").equalTo(msgtimestamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){
                        //ds.getRef().removeValue(); //if we uncomment then total message will disappear
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("message","This message was deleted");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message Deleted..", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "you can delete only your messages....", Toast.LENGTH_SHORT).show();
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
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv,messageIv;
        TextView messageTv,timeTv,isSeenTv;
        LinearLayout messagelayout;


        public MyHolder(@NonNull View itemView){
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.TimeTv);
            isSeenTv=itemView.findViewById(R.id.IsSeenTv);
            messagelayout=itemView.findViewById(R.id.messagelayout);
            messageIv=itemView.findViewById(R.id.messageIv);



        }
    }
}
