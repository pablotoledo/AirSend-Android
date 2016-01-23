package pablotoledo.airsendtfg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import librerias.utilidades.Persistencia;
import librerias.utilidades.Utilidades;

public class Inicial extends AppCompatActivity {

    private boolean conexionWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);
        metodoPrincipal();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inicial, menu);
        return true;
    }

    private void comprobacionesLicenciaFlujo(){
        Persistencia.cargarPersistencia(this.getApplicationContext());
        if(Persistencia.isLicencia()){
            //Comprobamos si tiene un nombre inicializado
            if(Persistencia.getNombreUsuario().isEmpty()){
                Intent intent = new Intent(Inicial.this, Instalacion.class);
                Inicial.this.startActivity(intent);
            }
            else{
                Intent intent = new Intent(Inicial.this, MenuPrincipal.class);
                Inicial.this.startActivity(intent);
            }

        }else{
            //Si no hay licencia, llamamos a la vista de licencia
            Intent intent = new Intent(Inicial.this, LicenciaActivity.class);
            Inicial.this.startActivity(intent);
        }
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

    public void metodoPrincipal(){

        Thread hiloEspera = new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        Thread.sleep(1500);
                        comprobacionesLicenciaFlujo();
                    } catch (InterruptedException ex) {
                        //Log.error(ex.getLocalizedMessage());
                    }

            }
        });
        hiloEspera.start();


    }
}
