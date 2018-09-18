package com.example.webspider;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class XiuAdapter extends ArrayAdapter<Thing> {

    private int id;

    public XiuAdapter(@NonNull Context context, int resource, @NonNull List<Thing> objects) {
        super(context, resource, objects);
        id = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(id, parent, false);
        Thing thing = getItem(position);
        TextView textView = view.findViewById(R.id.thing_name);
        textView.setText(thing.getItem());
        return view;
    }
}
