package com.example.videochatdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    Button signOutButton;
    ExtendedFloatingActionButton createRoomFab, joinRoomFab;
    ListView usersListView;

    ArrayAdapter<String> adapter;
    List<String> users;

    FirebaseAuth mAuth;
    FirebaseFirestore database;

    private static final int PERMISSION_REQ_ID = 22;

    private String ACTION = "";

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultPhone = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);
        return (resultCamera == PackageManager.PERMISSION_GRANTED) && (resultMic == PackageManager.PERMISSION_GRANTED) && (resultStorage == PackageManager.PERMISSION_GRANTED) && (resultPhone == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForCameraAndMicrophone() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQ_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        signOutButton = findViewById(R.id.signOutButton);
        createRoomFab = findViewById(R.id.createRoomFAB);
        joinRoomFab = findViewById(R.id.joinRoomFAB);
        usersListView = findViewById(R.id.usersListView);

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

        users = new ArrayList<>();

        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,users);
        usersListView.setAdapter(adapter);

        database.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (!documentSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            users.add(documentSnapshot.get("name").toString());
                            Log.i("name",documentSnapshot.get("name").toString());
                            Log.i("document",documentSnapshot.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(UsersActivity.this, "Error fetching users", Toast.LENGTH_SHORT).show();
                    Log.i("Error getting documents",task.getException().getMessage());
                }
            }
        });

        createRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACTION = "CREATE";
                Intent intent = new Intent(UsersActivity.this, RoomActivity.class);
                intent.putExtra("ACTION", ACTION);
                startActivity(intent);
            }
        });

        joinRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACTION = "JOIN";
                Intent intent = new Intent(UsersActivity.this, RoomActivity.class);
                intent.putExtra("ACTION", ACTION);
                startActivity(intent);
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(UsersActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}