package com.example.videochatdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class UsersListAdapter extends ArrayAdapter<User> {

    List<User> users;
    Context context;
    int resource;

    public UsersListAdapter(@NonNull Context context, int resource, List<User> users) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        //getting the view
        View view = layoutInflater.inflate(resource, null, false);

        TextView name_text_view = view.findViewById(R.id.name_text_view);
        TextView uid_text_view = view.findViewById(R.id.uid_text_view);

        User user = users.get(position);

        name_text_view.setText(user.getName());
        uid_text_view.setText(user.getUid());

        return view;
    }

    @Override
    public int getCount() {
        return users.size();
    }
}
