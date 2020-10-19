package com.example.videochatdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinRoomFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JoinRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JoinRoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JoinRoomFragment newInstance(String param1, String param2) {
        JoinRoomFragment fragment = new JoinRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    ListView roomsListView;

    ArrayAdapter<String> adapter;
    List<String> rooms;
    List<String> tokens;

    FirebaseAuth mAuth;
    FirebaseFirestore database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();

        // [START get_firestore_instance]
        database = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);
        // [END set_firestore_settings]

        rooms = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_room, container, false);

        roomsListView = view.findViewById(R.id.rooms);

        adapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,rooms);
        roomsListView.setAdapter(adapter);

        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CallActivity.class);
                intent.putExtra("channelName", rooms.get(position));
                intent.putExtra("token", tokens.get(position));
                startActivity(intent);
            }
        });

        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();

        database.collection("rooms").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    rooms.clear();
                    tokens.clear();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.get("participant").toString().equals(mAuth.getCurrentUser().getUid()) && Long.parseLong(documentSnapshot.get("expireTime").toString()) > System.currentTimeMillis()){
                            rooms.add(documentSnapshot.getId());
                            tokens.add(documentSnapshot.get("token").toString());
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getActivity(), "Error fetching rooms", Toast.LENGTH_SHORT).show();
                    Log.i("Error getting documents",task.getException().getMessage());
                }
            }
        });
    }
}