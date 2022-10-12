package com.example.xplore.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.xplore.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static  final int MSG_TYPE_LEFT=0;
    private static  final int MSG_TYPE_RIGHT=1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;
    private FirebaseAuth firebaseAuth;



    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
        firebaseAuth=FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);

        }



    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, @SuppressLint("RecyclerView") int position) {

        ModelGroupChat model=modelGroupChatList.get(position);
        String message=model.getMessage();
        String timestamp=model.getTimestamp();
        String senderUid=model.getSender();
        String messageType=model.getType();


        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        if(messageType.equals("text")){

            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);

        }else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);




            try {


                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);

            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }

        }

        /*holder.messagelayout.setOnClickListener(new View.OnClickListener() {
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
        });*/




        holder.timeTv.setText(datetime);

        setUsername(model,holder);

    }

    private void setUsername(ModelGroupChat model, HolderGroupChat holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            String name=""+ds.child("name").getValue();
                            holder.nameTv.setText(name);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
    private void deletemessages(int position) {
        String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgtimestamp=modelGroupChatList.get(position).getTimestamp();
        DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("Messages");
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
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {

       if(modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())){
           return MSG_TYPE_RIGHT;
       }else{
           return MSG_TYPE_LEFT;
       }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{


        private TextView nameTv,messageTv,timeTv;
        private ImageView messageIv;
        LinearLayout messagelayout;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            messageIv=itemView.findViewById(R.id.messageIv);
            messagelayout=itemView.findViewById(R.id.messagelayout);


        }

    }


}
