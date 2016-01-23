package pablotoledo.airsendtfg;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import librerias.utilidades.Persistencia;
import pablotoledo.airsendtfg.extensiones.GridViewAdapter;
import pablotoledo.airsendtfg.extensiones.ImageItem;

public class Instalacion extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private View itemView;
    private boolean seleccionado = false;
    private int posicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instalacion);
        gridView = (GridView) findViewById(R.id.listadoAvatar);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_layout, getData());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(myOnItemClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instalacion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, ""));
        }
        return imageItems;
    }

    AdapterView.OnItemClickListener myOnItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //String prompt = (String)parent.getItemAtPosition(position);
            if (seleccionado) {
                itemView = gridView.getChildAt((int) id);
                int childcount = gridView.getChildCount();
                int firstPos = gridView.getFirstVisiblePosition();
                for (int i = 0; i < childcount; i++) {
                    gridView.getChildAt(i).setBackgroundColor(Color.parseColor("#666666"));
                }
                for (int i = 0; i < childcount; i++) {
                    int posInArray = firstPos + i;

                    if (posInArray == position) {
                        gridView.getChildAt(i).setBackgroundColor(Color.parseColor("#E56500"));
                    }

                }
                posicion = position;
            } else {
                try {
                    seleccionado = true;
                    itemView = gridView.getChildAt((int) id);
                    int childcount = gridView.getChildCount();
                    int firstPos = gridView.getFirstVisiblePosition();

                    for (int i = 0; i < childcount; i++) {
                        int posInArray = firstPos + i;

                        if (posInArray == position) {
                            gridView.getChildAt(i).setBackgroundColor(Color.parseColor("#E56500"));
                        }

                    }
                    posicion = position;
                } catch (NullPointerException e) {
                    Log.e("AirSend+", "" + position + " " + id);
                }
            }
            //Toast.makeText(getApplicationContext(),""+position, Toast.LENGTH_LONG).show();
        }
    };

    public void botonAceptar(View v) {
        EditText mEdit = (EditText) findViewById(R.id.nombreUsuario);
        if ((mEdit.getText().length() > 0) && (this.seleccionado)) {
            if (mEdit.getText().length() > 4) {
                Persistencia.setNombreUsuario(mEdit.getText().toString());
                //obtener gato
                String[] myResArray = getResources().getStringArray(R.array.image_ids);
                for (int i = 0; i < myResArray.length; i++) {
                    myResArray[i] = myResArray[i].substring(myResArray[i].lastIndexOf("/") + 1);
                    myResArray[i] = myResArray[i].substring(0, myResArray[i].lastIndexOf("."));
                }
                ArrayList<String> listaGatos = new ArrayList<>(Arrays.asList(myResArray));

                Persistencia.setGatoUsuario(listaGatos.get(this.posicion));
                Persistencia.guardarPersistencia(this.getApplicationContext());

                Intent intent = new Intent(Instalacion.this, MenuPrincipal.class);
                Instalacion.this.startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "El nombre debe tener al menos 5 letras", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Imagen y/o nombre incompleto", Toast.LENGTH_LONG).show();
        }
    }
}
