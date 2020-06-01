package z.prespuestos.brujula;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

@TargetApi(23)

public class MainActivity extends FragmentActivity implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, SensorEventListener{

    private ImageView brujulaImg;
    private TextView latitud, longitud, altitud;
    private Button boton;

    private TextToSpeech speech;
    private GestureDetectorCompat GDC;

    private SensorManager manager;
    private Sensor acelerometro, brujula;

    private final float[] magnetometerReading = new float[3];
    private final float[] accelerometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private int mTargetDirection;

    private String cardinal;

    private Runnable hiloValores;

    private Handler manejadorValores;

    private Localizacion l;

    private CameraManager managerCamara;
    private String idCamara;
    private boolean encendida;



    /*
    -------------------------------------------------------
    Metodos vida la activity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        brujulaImg = findViewById(R.id.imagen);
        altitud = findViewById(R.id.alttitud);
        longitud = findViewById(R.id.longitud);
        latitud = findViewById(R.id.latitud);
        boton = findViewById(R.id.controlLuz);



        GDC = new GestureDetectorCompat(getApplicationContext(), this);
        GDC.setOnDoubleTapListener(this);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        manejadorValores = new Handler();

        hiloValores = new Runnable() {
            @Override
            public void run() {
                upOrientation();
            }
        };

        managerCamara = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            idCamara = managerCamara.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        acelerometro = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(acelerometro != null){
            manager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        brujula = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(brujula != null){
            manager.registerListener(this, brujula, SensorManager.SENSOR_DELAY_NORMAL);
        }

        boton.setText(getString(R.string.linternaOn));
        if(encendida) boton.setText(getString(R.string.linternaOff));
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        manejadorValores.removeCallbacks(hiloValores);

    }
    /*
    -------------------------------------------------------
     */

    /*
    -------------------------------------------------------
    Mis metodos
     */
    public void cargaLocation(){
        l = new Localizacion(this);

        String[] p = l.getPosicion();

        latitud.setText(p[0]+"º");
        longitud.setText(p[1]+"º");
        altitud.setText(p[2]+"m");

    }

    public void upOrientation(){

        FragmentManager miManejador = getSupportFragmentManager();
        FragmentTransaction miTransaccion = miManejador.beginTransaction();

        Bundle datos = new Bundle();
        datos.putInt("GRADOS", mTargetDirection);
        datos.putString("LABEL", getCardinals(mTargetDirection)[1]);

        Fragment valores = new Valores();
        valores.setArguments(datos);
        miTransaccion.replace(R.id.valores, valores);
        miTransaccion.commit();
    }

    public void moveCompass(){

        float fromDegrees = brujulaImg.getRotation();
        float toDegrees = mTargetDirection;

        if(fromDegrees != toDegrees){
            brujulaImg.animate().rotation(0.015f*360-mTargetDirection).setDuration(0);
        }

    }

    public String[] getCardinals(int d){

        String[] c = new String[2];

        if (d >= 0 && d <= 30) {
            c[0] = getString(R.string.norte);
            c[1] = "N";
        }
        else if (d >= 31 && d <= 75) {
            c[0] = getString(R.string.nordeste);
            c[1] = "NE";
        }
        else if ( d >= 76 && d <= 120) {
            c[0] = getString(R.string.este);
            c[1] = "E";
        }
        else if (d >= 121 && d <= 165) {
            c[0] = getString(R.string.sureste);
            c[1] = "SE";
        }
        else if (d >= 166 && d <= 210) {
            c[0] = getString(R.string.sur);
            c[1] = "S";
        }
        else if (d >= 211 && d <= 255) {
            c[0] = getString(R.string.suroeste);
            c[1] = "SO";
        }
        else if (d >= 256 && d <= 300) {
            c[0] = getString(R.string.oeste);
            c[1] = "O";
        }
        else if (d >= 301 && d <= 345) {
            c[0] = getString(R.string.noroeste);
            c[1] = "NO";
        }
        else if(d >= 346 && d <= 360) {
            c[0] = getString(R.string.norte);
            c[1] = "N";
        }

        return c;
    }

    public void habla(){

        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    cardinal = getCardinals(mTargetDirection)[0];
                    speech.setLanguage(Locale.getDefault());
                    speech.speak((mTargetDirection)+getString(R.string.grados)+","+cardinal, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    public void flash(View v){

        try {
            if(!encendida){
                managerCamara.setTorchMode(idCamara, true);
                boton.setText(getString(R.string.linternaOff));
                encendida = true;

            }
            else {
                managerCamara.setTorchMode(idCamara, false);
                boton.setText(getString(R.string.linternaOn));
                encendida = false;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    /*
    -------------------------------------------------------
     */

    /*
    -------------------------------------------------------
    Metodos de interfaces utilizados
     */
    @Override
    public void onSensorChanged(SensorEvent s) {

        if (s.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(s.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (s.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(s.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }

        manager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        manager.getOrientation(rotationMatrix, mOrientationAngles);

        mTargetDirection = (int) (Math.toDegrees(mOrientationAngles[0]) + 360) % 360;

        manejadorValores.postDelayed(hiloValores, 250);

        moveCompass();
        cargaLocation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.GDC.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }



    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {

        Runnable Hilo = new Runnable() {
            @Override
            public void run() {
                habla();
            }
        };

        new Thread(Hilo).start();

        return true;
    }
    /*
    -------------------------------------------------------
     */

    /*
    -------------------------------------------------------
    Metodos de interfaces NO utilizados
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }
    /*
    -------------------------------------------------------
     */


}
