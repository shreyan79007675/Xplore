package com.example.xplore.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplore.GroupCreateActivity;
import com.example.xplore.MainActivity;
import com.example.xplore.R;
import com.example.xplore.adapters.AdapterGroupChatlist;
import com.example.xplore.models.ModelGroupChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {
    FirebaseAuth firebaseauth;
    private RecyclerView groupsRv;
    private ArrayList<ModelGroupChats> groupChatLists;
    private AdapterGroupChatlist adapterGroupChatlist;



    public HomeFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);

        firebaseauth = FirebaseAuth.getInstance();

        groupsRv=view.findViewById(R.id.groupsRv);


        loadgroupchatslist();

        checkuserstatus();







        return  view;
    }

    private void loadgroupchatslist() {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.size();
                for(DataSnapshot ds:snapshot.getChildren()){

                        ModelGroupChats model=ds.getValue(ModelGroupChats.class);
                        groupChatLists.add(model);


                }
                adapterGroupChatlist=new AdapterGroupChatlist(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }




    private void searchgroupchatslist(String query) {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds:snapshot.getChildren()){




                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){

                            ModelGroupChats model=ds.getValue(ModelGroupChats.class);
                            groupChatLists.add(model);

                        }



                }
                adapterGroupChatlist=new AdapterGroupChatlist(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    /*private void loadgroupchatslist() {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.size();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.child("Participants").child(firebaseauth.getUid()).exists()){
                        ModelGroupChats model=ds.getValue(ModelGroupChats.class);
                        groupChatLists.add(model);

                    }
                }
                adapterGroupChatlist=new AdapterGroupChatlist(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }




    private void searchgroupchatslist(String query) {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds:snapshot.getChildren()){


                    if(ds.child("Participants").child(firebaseauth.getUid()).exists()){

                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){

                            ModelGroupChats model=ds.getValue(ModelGroupChats.class);
                            groupChatLists.add(model);

                        }


                    }
                }
                adapterGroupChatlist=new AdapterGroupChatlist(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatlist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }*/





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
/*
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false);  //By Harsha
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
    }*/



    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(false);


        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchgroupchatslist(query);
                } else {
                    loadgroupchatslist();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchgroupchatslist(newText);
                } else {
                    loadgroupchatslist();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id==R.id.action_logout){
            firebaseauth.signOut();
            checkuserstatus();

        }
        else if (id ==R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}