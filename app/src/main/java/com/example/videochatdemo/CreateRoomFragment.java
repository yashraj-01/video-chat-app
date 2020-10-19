package com.example.videochatdemo;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
 * Use the {@link CreateRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateRoomFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateRoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateRoomFragment newInstance(String param1, String param2) {
        CreateRoomFragment fragment = new CreateRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static String appId = "15b44db1bf054b1f8fc3d4450967cdf3";
    private static String appCertificate = "2849332911cc47128c9067ca9cbf4105";
    private static String channelName = "";
    private static int uid = 0;
    private static int expirationTimeInSeconds = 3600;

    private int lastSelectedPosition = -1;
    View lastSelectedView = null;

    ListView usersListView;
    Button createRoom;

    UsersListAdapter adapter;
    List<User> users;

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

        channelName = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);

        usersListView = view.findViewById(R.id.users);
        createRoom = view.findViewById(R.id.createRoom);

        users = new ArrayList<>();

        adapter = new UsersListAdapter(getActivity(),R.layout.user_layout,users);
        usersListView.setAdapter(adapter);

        database.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (!documentSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            User user = new User(documentSnapshot.get("name").toString(),documentSnapshot.getId());
                            users.add(user);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    Toast.makeText(getActivity(), "Error fetching users", Toast.LENGTH_SHORT).show();
                    Log.i("Error getting documents",task.getException().getMessage());
                }
            }
        });

        usersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                User user = users.get(position);

                if(lastSelectedPosition >= 0 && lastSelectedView != view) {
                    users.get(lastSelectedPosition).setSelected(false);
                    lastSelectedView.setBackgroundColor(Color.WHITE);
                }

                user.setSelected(!user.isSelected());

                view.setBackgroundColor(user.isSelected() ? Color.LTGRAY : Color.WHITE);

                lastSelectedPosition = position;
                lastSelectedView = view;

                return true;
            }
        });

        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (User user : users){
                    if (user.isSelected()){
                        Log.i("Users Selected",user.getName() + " : " + user.getUid());
                    } else{
                        Log.i("Users Selected",Boolean.toString(user.isSelected()));
                    }
                }
            }
        });

        return view;
    }
}