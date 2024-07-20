package com.example.pm1examengrupo1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ActivityMapa extends AppCompatActivity {
  private MapView mapView;
  private Button regresarButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_mapa);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    mapView = findViewById(R.id.mapView);
    regresarButton = findViewById(R.id.regresarButton);

    double latitud = getIntent().getDoubleExtra("latitud", 0.0);
    double longitud = getIntent().getDoubleExtra("longitud", 0.0);

    mapView.setBuiltInZoomControls(true);
    mapView.setMultiTouchControls(true);

    GeoPoint geoPoint = new GeoPoint(latitud, longitud);

    mapView.getController().setZoom(15.0);
    mapView.getController().setCenter(geoPoint);

    Marker marker = new Marker(mapView);

    marker.setPosition(geoPoint);

    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

    mapView.getOverlays().add(marker);

    regresarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ActivityMapa.this, ActivityListView.class);

        startActivity(intent);
      }
    });
  }
}