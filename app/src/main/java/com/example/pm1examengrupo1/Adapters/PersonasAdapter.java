package com.example.pm1examengrupo1.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pm1examengrupo1.Models.Personas;
import com.example.pm1examengrupo1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PersonasAdapter extends ArrayAdapter<Personas> {
  private LayoutInflater inflater;
  private List<Personas> items;
  private List<Personas> itemsOriginal;
  private int resourceLayout;

  public PersonasAdapter(Context context, int resource, List<Personas> items) {
    super(context, resource, items);
    this.resourceLayout = resource;
    this.items = items;
    itemsOriginal = new ArrayList<>();
    itemsOriginal.addAll(items);
    this.inflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(resourceLayout, null);
    }

    Personas persona = getItem(position);

    if (persona != null) {
      ImageView imageView = convertView.findViewById(com.example.pm1examengrupo1.R.id.imageView);
      TextView descriptionView = convertView.findViewById(R.id.descriptionView);

      byte[] decodedString = android.util.Base64.decode(persona.getFoto(), android.util.Base64.DEFAULT);
      Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      imageView.setImageBitmap(decodedByte);

      descriptionView.setText(persona.getNombre());
    }

    return convertView;
  }

  public void filtrado(String txtSearch){
    int longitud = txtSearch.length();
    if (longitud == 0){
      items.clear();
      items.addAll(itemsOriginal);
    }else{
      List<Personas> collecion = items.stream().filter(i -> i.getNombre().toLowerCase().contains(txtSearch.toLowerCase())).collect(Collectors.toList());
      items.clear();
      items.addAll(collecion);
    }
    notifyDataSetChanged();
  }
}
