package z.prespuestos.brujula;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Valores extends Fragment {
    private TextView labelGrados;
    private float target;
    private String label;


    public Valores() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmento = inflater.inflate(R.layout.fragment_valores, container, false);

        labelGrados = fragmento.findViewById(R.id.labelGrados);

        if (getArguments() != null) {
            target = getArguments().getInt("GRADOS");
            label = getArguments().getString("LABEL");

            labelGrados.setText(label + " " + ((int) target) + "º");
        }
        return fragmento;
    }

}
