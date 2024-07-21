package com.example.pm1examengrupo1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm1examengrupo1.Adapters.PersonasAdapter;
import com.example.pm1examengrupo1.Models.Personas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityListView extends AppCompatActivity implements SearchView.OnQueryTextListener{
  private Button eliminarButton, actualizarButton, regresarButton, verMapaButton;
  private double selectedLatitud, selectedLongitud;
  private int selectedItemPosition = -1;
  private List<Personas> personasList;
  private int selectedItemId = -1;
  private PersonasAdapter adapter;
  private ListView listView;
  private SearchView txtsearch;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_list_view);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
    listView = findViewById(R.id.listView);
    personasList = new ArrayList<>();
    adapter = new PersonasAdapter(this, R.layout.list_item, personasList);
    listView.setAdapter(adapter);

    actualizarButton = findViewById(R.id.actualizarButton);
    eliminarButton = findViewById(R.id.eliminarButton);
    regresarButton = findViewById(R.id.regresarButton);
    verMapaButton = findViewById(R.id.verMapaButton);
    txtsearch = findViewById(R.id.txtsearch);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedItemPosition = position;

        selectedItemId = personasList.get(position).getId();

        selectedLatitud = personasList.get(position).getLatitud();
        selectedLongitud = personasList.get(position).getLongitud();
      }
    });

    eliminarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        eliminarPersona();
      }
    });

    actualizarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        actualizarPersona();
      }
    });

    verMapaButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mostrarActivityMapa();
      }
    });

    regresarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ActivityListView.this, MainActivity.class);

        startActivity(intent);
      }
    });

    txtsearch.setOnQueryTextListener(this);

    traerDatos();
  }

  private void traerDatos() {
    String url = "http://192.168.225.212/examen-rest-api/peticiones-http/GetPersons.php";
    RequestQueue queue = Volley.newRequestQueue(this);

    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
      @Override
      public void onResponse(JSONArray response) {
        for (int i = 0; i < response.length(); i++) {
          try {
            JSONObject object = response.getJSONObject(i);

            int id = object.getInt("id");
            String foto = object.getString("foto");
            String nombre = object.getString("nombre");
            int telefono = object.getInt("telefono");
            int latitud = object.getInt("latitud");
            int longitud = object.getInt("longitud");

            personasList.add(new Personas(id, foto, nombre, telefono, latitud, longitud));
          } catch (JSONException e) {
            Log.e("Error", e.toString());
          }
        }
        adapter.notifyDataSetChanged();
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("ERROR", error.toString());
      }
    });
    queue.add(jsonArrayRequest);
  }

  private void eliminarPersona() {
    if (selectedItemPosition != -1) {
      String url = "http://192.168.225.212/examen-rest-api/peticiones-http/DeletePerson.php";
      RequestQueue queue = Volley.newRequestQueue(this);

      StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          personasList.remove(selectedItemPosition);

          adapter.notifyDataSetChanged();

          selectedItemPosition = -1;
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e("ERROR", error.toString());
        }
      }) {
        @Override
        public byte[] getBody() throws AuthFailureError {
          Map<String, Integer> params = new HashMap<>();
          params.put("id", selectedItemId);
          return new JSONObject(params).toString().getBytes();
        }

        @Override
        public String getBodyContentType() {
          return "application/json; charset=utf-8";
        }
      };

      queue.add(stringRequest);
    }
  }

  private void actualizarPersona() {
    if (selectedItemPosition != -1) {
      Intent intent = new Intent(ActivityListView.this, ActivityActualizarPersona.class);

      intent.putExtra("id", selectedItemId);

      startActivity(intent);
    }
  }

  private void mostrarActivityMapa() {
    if (selectedItemPosition != -1) {
      Intent intent = new Intent(ActivityListView.this, ActivityMapa.class);

      intent.putExtra("latitud", selectedLatitud);
      intent.putExtra("longitud", selectedLongitud);

      startActivity(intent);
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    adapter.filtrado(newText);
    return false;
  }
}