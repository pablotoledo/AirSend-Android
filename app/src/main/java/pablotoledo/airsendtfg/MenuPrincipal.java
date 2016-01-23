package pablotoledo.airsendtfg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import librerias.nucleo.NucleoAirSend;
import librerias.nucleo.sondeo.MensajeSondeoJSON;
import librerias.utilidades.Utilidades;
import pablotoledo.airsendtfg.extensiones.GridViewAdapter;
import pablotoledo.airsendtfg.extensiones.ImageItem;

public class MenuPrincipal extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static NucleoAirSend nucleo;
    private Map<Integer, MensajeSondeoJSON> asociacion = new HashMap<Integer, MensajeSondeoJSON>();
    private Context contexto = this;
    private MenuPrincipal soy = this;
    private boolean nucleoParado;
    private Thread hiloInterfaz;
    private MensajeSondeoJSON mensajeSeleccionado;
    private boolean intentRecibido= false;
    private Uri imageUri;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        this.cargarNucleos();
        this.hiloInterfaz = hiloInterfaz();
        this.hiloInterfaz.start();
        intent = getIntent();
        imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            this.intentRecibido=true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu_principal, menu);
        return true;
    }

    public void cargarNucleos() {
        if (!(nucleo == null)) {
            nucleo.pararNucleos();
        }
        nucleo = new NucleoAirSend();
        nucleo.cargarNucleos(this.getApplicationContext(),soy);
    }

    public void cargarLista() {
        gridView = (GridView) findViewById(R.id.listado);
        gridAdapter = new GridViewAdapter(contexto, R.layout.grid_layout, elementosLista());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(myOnItemClickListener);
    }

    public void cargarListaVacia() {
        gridView = (GridView) findViewById(R.id.listado);
        gridAdapter = new GridViewAdapter(contexto, R.layout.grid_layout, listaVacia());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(myOnItemClickListener);
    }

    AdapterView.OnItemClickListener myOnItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String[] items = {"Obtener información", "Enviar un archivo"};

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(contexto);
            final MensajeSondeoJSON elemento = asociacion.get(position);
            mensajeSeleccionado = elemento;
            builder.setTitle("Opciones")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(contexto);
                                builder.setMessage("Nombre: "+elemento.getNombreEquipo() + "\nIP: " + elemento.getDireccionIP() + "\nSistema Operativo: " + elemento.getSistemaOperativo() + "\nID: " + elemento.getIdEmisor())
                                        .setTitle("Información")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.create().show();
                            }
                            if (item ==1){
                                if(!intentRecibido){
                                    Toast.makeText(contexto,"Enviar ARCHIVO",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("*/*");
                                    startActivityForResult(intent, 0);
                                } else {
                                    String FilePath = imageUri.getPath();
                                    /////
                                    Intent intent = new Intent(MenuPrincipal.this, EnviarDatos.class);
                                    intent.putExtra("fichero",FilePath);
                                    intent.putExtra("mensaje",mensajeSeleccionado);
                                    MenuPrincipal.this.startActivity(intent);
                                    intentRecibido=false;
                                }
                            }
                        }
                    });
            builder.create().show();

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch(requestCode){
            case 0:
                if(resultCode==RESULT_OK){
                    String FilePath = Utilidades.obtenerDireccionDesdeUri(this,data.getData());
                    /////
                    Intent intent = new Intent(MenuPrincipal.this, EnviarDatos.class);
                    intent.putExtra("fichero",FilePath);
                    intent.putExtra("mensaje",mensajeSeleccionado);
                    MenuPrincipal.this.startActivity(intent);

                }
                break;

        }
    }

    public Thread hiloInterfaz() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        synchronized (this) {
                            wait(5000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Utilidades.existeConexionWIFI(contexto)) {
                                        if (nucleoParado) {
                                            nucleo.cargarNucleos(contexto,soy);
                                            nucleoParado = false;
                                            Toast.makeText(contexto, "WiFi Detectado", Toast.LENGTH_LONG).show();
                                        }
                                        cargarLista();
                                    } else {
                                        nucleo.pararNucleos();
                                        cargarListaVacia();
                                        nucleoParado = true;
                                        Toast.makeText(contexto, "No existe conexión WiFi", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        contexto = this;
        this.hiloInterfaz = hiloInterfaz();
        this.hiloInterfaz.start();
        this.cargarNucleos();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.hiloInterfaz.interrupt();
        //this.nucleo.pararNucleos();
    }

    private ArrayList<ImageItem> elementosLista() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        ArrayList<MensajeSondeoJSON> listaTemporal = nucleo.getListaDispositivos();
        asociacion.clear();
        int contador = 0;
        for (MensajeSondeoJSON elemento : listaTemporal) {
            int indice = getResources().getIdentifier(elemento.getIconoUsuario(), "drawable", getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), indice);
            imageItems.add(new ImageItem(bitmap, elemento.getNombreEquipo()));
            asociacion.put(contador, elemento);
            contador++;
        }
        return imageItems;
    }

    private ArrayList<ImageItem> listaVacia() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        ArrayList<MensajeSondeoJSON> listaTemporal = new ArrayList<>();
        asociacion.clear();
        int contador = 0;
        for (MensajeSondeoJSON elemento : listaTemporal) {
            int indice = getResources().getIdentifier(elemento.getIconoUsuario(), "drawable", getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), indice);
            imageItems.add(new ImageItem(bitmap, elemento.getNombreEquipo()));
            asociacion.put(contador, elemento);
        }
        return imageItems;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reiniciar:
                Intent intent = new Intent(MenuPrincipal.this, Instalacion.class);
                MenuPrincipal.this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
