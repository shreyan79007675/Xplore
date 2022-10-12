package com.example.xplore.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.GroupCreateActivity;
import com.example.xplore.MainActivity;
import com.example.xplore.R;
import com.example.xplore.adapters.AdapterChatlist;
import com.example.xplore.models.ModelChatlist;
import com.example.xplore.models.ModelUsers;
import com.example.xplore.models.Modelchat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class chatlist extends Fragment {

    RecyclerView recyclerView;
    FirebaseAuth firebaseAuth;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;





    public chatlist() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view= inflater.inflate(R.layout.fragment_chatlist, container, false);

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        recyclerView=view.findViewById(R.id.recyclerView);
        chatlistList=new ArrayList<>();


        reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatlistList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelChatlist chatlist=ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return view;
    }

    private void loadChats() {

        userList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers user=ds.getValue(ModelUsers.class);
                    for(ModelChatlist chatlist:chatlistList){
                        if(user.getUid()!=null&&user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    adapterChatlist=new AdapterChatlist(getContext(),userList);
                    recyclerView.setAdapter(adapterChatlist);
                    for(int i=0;i<userList.size();i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void lastMessage(String userId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Modelchat chat = ds.getValue(Modelchat.class);
                    if (chat==null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReciever();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReciever().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReciever().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        if(chat.getType().equals("image")){
                            theLastMessage="Sent a Photo";
                        }else{
                            theLastMessage = chat.getMessage();
                        }

                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
    //By Harsha upto end
    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){

        }
        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_groupinfo).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionItemSelected(MenuItem item){
        int id =item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id ==R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


}