package com.example.pm1examengrupo1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_LOCATION_PERMISSION = 1;
  private static final int REQUEST_CAMERA_PERMISSION = 100;
  private static final int REQUEST_IMAGE_CAPTURE = 101;
  private TextInputEditText longitudTextInputEditText;
  private TextInputEditText telefonoTextInputEditText;
  private TextInputEditText latitudTextInputEditText;
  private MyLocationNewOverlay myLocationNewOverlay;
  private TextInputEditText nombreTextInputEditText;
  private Button buttonGuardar, buttonVerPersonas;
  private RequestQueue requestQueue;
  private String currentPhotoPath;
  private ImageView imageView;
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    longitudTextInputEditText = findViewById(R.id.longitudTextInputEditText);
    telefonoTextInputEditText = findViewById(R.id.telefonoTextInputEditText);
    latitudTextInputEditText = findViewById(R.id.latitudTextInputEditText);
    nombreTextInputEditText = findViewById(R.id.nombreTextInputEditText);
    buttonVerPersonas = findViewById(R.id.buttonVerPersonas);
    buttonGuardar = findViewById(R.id.buttonGuardar);
    imageView = findViewById(R.id.imageView);
    mapView = findViewById(R.id.mapView);

    requestQueue = Volley.newRequestQueue(this);

    Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
    mapView.setMultiTouchControls(true);

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    } else {
      permitirMiUbicacion();
    }

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        permisosCamara();
      }
    });

    buttonGuardar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        guardarDatos();
      }
    });

    buttonVerPersonas.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, ActivityListView.class);
        startActivity(intent);
      }
    });
  }

  private void guardarDatos() {
    try {
      String nombre = nombreTextInputEditText.getText().toString().trim();
      String telefono = telefonoTextInputEditText.getText().toString().trim();
      String latitudStr = latitudTextInputEditText.getText().toString().trim();
      String longitudStr = longitudTextInputEditText.getText().toString().trim();
      String fotoBase64 = convertImageBase64(currentPhotoPath);

      JSONObject jsonObject = new JSONObject();

      jsonObject.put("nombre", nombre);
      jsonObject.put("telefono", telefono);
      jsonObject.put("latitud", latitudStr);
      jsonObject.put("longitud", longitudStr);
      jsonObject.put("foto", fotoBase64);

      Log.d("Datos", jsonObject.toString());

      mandarApi(jsonObject);

    } catch (Exception error) {
      Log.e("Error", "Error al crear el JSON: " + error.toString());
    }
  }

  private void mandarApi(JSONObject jsonObject) {
    String url = "http://192.168.225.212/examen-rest-api/peticiones-http/CreatePerson.php";

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        Toast.makeText(MainActivity.this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show();
        Log.d("Response", response.toString());
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Toast.makeText(MainActivity.this, "Error al enviar datos", Toast.LENGTH_SHORT).show();
        Log.e("Error", error.toString());
      }
    });

    requestQueue.add(jsonObjectRequest);
  }

  public static class EdgeToEdge {
    public static void enable(@NonNull Activity activity) {
      WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
    }
  }

  private void permisosCamara() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
    } else {
      abrirCamara();
    }
  }

  private void abrirCamara() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      File photoFile = null;

      try {
        photoFile = createImageFile();
      } catch (IOException ex) {
        Log.e("MainActivity", "Error al crear el archivo de la imagen.", ex);
      }

      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.pm1examengrupo1.fileprovider", photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      }
    }
  }

  private File createImageFile() throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    String imageFileName = "JPEG_" + timeStamp + "_";

    File storageDir = getExternalFilesDir(null);

    File image = File.createTempFile(imageFileName, ".jpg", storageDir);

    currentPhotoPath = image.getAbsolutePath();

    return image;
  }

  private void permitirMiUbicacion() {
    GpsMyLocationProvider provider = new GpsMyLocationProvider(this);

    myLocationNewOverlay = new MyLocationNewOverlay(provider, mapView);

    myLocationNewOverlay.enableMyLocation();

    myLocationNewOverlay.runOnFirstFix(() -> {
      GeoPoint myLocation = myLocationNewOverlay.getMyLocation();

      if (myLocation != null) {
        runOnUiThread(() -> {
          latitudTextInputEditText.setText(String.valueOf(myLocation.getLatitude()));

          longitudTextInputEditText.setText(String.valueOf(myLocation.getLongitude()));
        });
      }
    });

    mapView.getOverlays().add(myLocationNewOverlay);
  }

  private String convertImageBase64(String path) {
    Bitmap bitmap = BitmapFactory.decodeFile(path);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

    byte[] imageArray = byteArrayOutputStream.toByteArray();

    return Base64.encodeToString(imageArray, Base64.NO_WRAP);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

      imageView.setImageBitmap(bitmap);

      String base64Image = convertImageBase64(currentPhotoPath);

      Log.d("Imagen Base64", base64Image);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        permitirMiUbicacion();
      } else {
        Toast.makeText(this, "Permisos denegados de ubicacion.", Toast.LENGTH_SHORT).show();
      }
    }

    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        abrirCamara();
      } else {
        Toast.makeText(this, "Permiso de camara denegado.", Toast.LENGTH_SHORT).show();
      }
    }
  }
}