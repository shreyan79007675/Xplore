package com.example.xplore.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.GroupChat;
import com.example.xplore.R;
import com.example.xplore.models.ModelGroupChats;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatlist extends RecyclerView.Adapter<AdapterGroupChatlist.HolderGroupChatlist> {



    private Context context;
    private ArrayList<ModelGroupChats> groupChatLists;

    public AdapterGroupChatlist(Context context, ArrayList<ModelGroupChats> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatlist onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);
        return new HolderGroupChatlist(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatlist holder, int position) {

        ModelGroupChats model=groupChatLists.get(position);
        String groupId=model.getGroupId();
        String groupIcon=model.getGroupIcon();
        String groupTitle=model.getGroupTitle();
        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");
        loadlastmessage(model,holder);

        holder.groupTitleTv.setText(groupTitle);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv);

        }catch(Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, GroupChat.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);

            }
        });

    }

    private void loadlastmessage(ModelGroupChats model, HolderGroupChatlist holder) {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String message=""+ds.child("message").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String sender=""+ds.child("sender").getValue();
                            String messageType=""+ds.child("type").getValue();


                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            if(messageType.equals("image")){
                                holder.messageTv.setText("Sent Photo");
                            }else{
                                holder.messageTv.setText(message);

                            }


                            holder.timeTv.setText(datetime);


                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds:snapshot.getChildren()){
                                                String name=""+ds.child("name").getValue();
                                                holder.nameTv.setText(name);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }


    class HolderGroupChatlist extends RecyclerView.ViewHolder{


        private ImageView groupIconIv;
        private TextView groupTitleTv,nameTv,messageTv,timeTv;

        public HolderGroupChatlist(@NonNull View itemView) {
            super(itemView);

            groupIconIv=itemView.findViewById(R.id.groupIconIv);
            groupTitleTv=itemView.findViewById(R.id.groupTitletv);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
