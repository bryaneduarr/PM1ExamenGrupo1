package com.example.pm1examengrupo1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityActualizarPersona extends AppCompatActivity {
  private EditText nombreEditText, telefonoEditText, latitudEditText, longitudEditText;
  private static final int REQUEST_CAMERA_PERMISSION = 100;
  private static final int REQUEST_IMAGE_CAPTURE = 101;
  private Button actualizarButton, regresarButton;
  private String currentPhotoPath;
  private ImageView imageView;
  private int personId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_actualizar_persona);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    personId = getIntent().getIntExtra("id", -1);

    nombreEditText = findViewById(R.id.nombreTextInputEditText);
    telefonoEditText = findViewById(R.id.telefonoTextInputEditText);
    latitudEditText = findViewById(R.id.latitudTextInputEditText);
    longitudEditText = findViewById(R.id.longitudTextInputEditText);
    imageView = findViewById(R.id.imageView);

    actualizarButton = findViewById(R.id.actualizarButton);
    regresarButton = findViewById(R.id.regresarButton);

    traerDatosPersona();

    actualizarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        actualizarPersona();
      }
    });

    regresarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ActivityActualizarPersona.this, ActivityListView.class);

        startActivity(intent);
      }
    });

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        permisosCamara();
      }
    });

  }

  private void permisosCamara() {
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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

    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        abrirCamara();
      } else {
        Toast.makeText(this, "Permiso de camara denegado.", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void traerDatosPersona() {
    String url = "http://10.0.2.2/examen-rest-api/peticiones-http/GetPersons.php?id=" + personId;

    RequestQueue requestQueue = Volley.newRequestQueue(this);

    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
      @Override
      public void onResponse(JSONArray response) {
        try {
          if (response.length() > 0) {
            JSONObject person = response.getJSONObject(0);


            String foto = person.getString("foto");
            String nombre = person.getString("nombre");
            int telefono = person.getInt("telefono");
            int latitud = person.getInt("latitud");
            int longitud = person.getInt("longitud");

            byte[] decodedString = Base64.decode(foto, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);

            nombreEditText.setText(nombre);
            telefonoEditText.setText(String.valueOf(telefono));
            latitudEditText.setText(String.valueOf(latitud));
            longitudEditText.setText(String.valueOf(longitud));
          } else {
            Log.e("Error", "Persona sin id");
          }
        } catch (Exception error) {
          Log.e("Error", error.toString());
        }
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("Error", error.toString());
      }
    });
    requestQueue.add(jsonArrayRequest);
  }

  private void actualizarPersona() {
    String url = "http://10.0.2.2/examen-rest-api/peticiones-http/UpdatePerson.php";
    RequestQueue requestQueue = Volley.newRequestQueue(this);

    try {
      JSONObject jsonObject = new JSONObject();

      jsonObject.put("id", personId);

      imageView.setDrawingCacheEnabled(true);
      imageView.buildDrawingCache();
      Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
      String fotoBase64 = android.util.Base64.encodeToString(getBitmapAsByteArray(bitmap), Base64.DEFAULT);

      jsonObject.put("foto", fotoBase64);
      jsonObject.put("nombre", nombreEditText.getText().toString());
      jsonObject.put("telefono", Long.parseLong(telefonoEditText.getText().toString()));
      jsonObject.put("latitud", Long.parseLong(latitudEditText.getText().toString()));
      jsonObject.put("longitud", Long.parseLong(longitudEditText.getText().toString()));

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          Log.d("Response", response.toString());
          finish();
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e("Error", error.toString());
        }
      }) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String, String> headers = new HashMap<>();

          headers.put("Content-Type", "application/json; charset=utf-8");

          return headers;
        }
      };

      Log.d("Res", jsonObject.toString());
      requestQueue.add(jsonObjectRequest);
    } catch (Exception error) {
      Log.e("Error", error.toString());
    }
  }

  private byte[] getBitmapAsByteArray(Bitmap bitmap) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

    return outputStream.toByteArray();
  }
}