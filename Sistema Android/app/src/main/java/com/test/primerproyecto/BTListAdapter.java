package com.test.primerproyecto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class BTListAdapter extends ArrayAdapter<BTDevice>
{

    public BTListAdapter(Context context, ArrayList<BTDevice> btDevices)
    {
        super(context, 0, btDevices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Get the data item for this position
        BTDevice btDevice = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bt_item_list, parent, false);
        }
        // Lookup view for data population
        TextView Nombre = (TextView) convertView.findViewById(R.id.nombre);
        TextView MAC = (TextView) convertView.findViewById(R.id.mac);
        // Populate the data into the template view using the data object
        Nombre.setText(btDevice.Nombre);
        MAC.setText(btDevice.MAC);
        // Return the completed view to render on screen
        return convertView;
    }
}