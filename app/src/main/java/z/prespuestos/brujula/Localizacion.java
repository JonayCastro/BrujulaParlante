package z.prespuestos.brujula;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.text.DecimalFormat;

public class Localizacion{

    private LocationManager locationManager;
    private Location location;
    private DecimalFormat formato;

    private int  MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
    private String[] posicion;
    private String latitud, longitud, altitud;
    private Activity activity;

    public Localizacion(Activity activity){
        formato = new DecimalFormat("#.00");
        this.activity = activity;
        posicion = new String[3];

        getLocation();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }

        }
    }
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},  MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else {

            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                location = new Location(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

                latitud = formato.format(location.getLatitude());
                longitud = formato.format(location.getLongitude());
                altitud = formato.format(location.getAltitude());

                posicion[0] = latitud;
                posicion[1] = longitud;
                posicion[2] = altitud;
            }
        }

    }

    public String[] getPosicion(){
        return posicion;
    }
}
