package pablotoledo.airsendtfg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class menuDebug extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_debug);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu_debug, menu);
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

    public void activityUno(View v) {
        Intent intent = new Intent(menuDebug.this, LicenciaActivity.class);
        menuDebug.this.startActivity(intent);

    }

    public void activityDos(View v) {
        Intent intent = new Intent(menuDebug.this, MenuPrincipal.class);
        menuDebug.this.startActivity(intent);

    }

    public void activtyTres(View v) {
        Intent intent = new Intent(menuDebug.this, Instalacion.class);
        menuDebug.this.startActivity(intent);
    }

    public void activtyCuatro(View v) {
        Intent intent = new Intent(menuDebug.this, EnviarDatos.class);
        menuDebug.this.startActivity(intent);
    }

    public void activtyCinco(View v) {
        Intent intent = new Intent(menuDebug.this, RecibirDatos.class);
        menuDebug.this.startActivity(intent);
    }
}
