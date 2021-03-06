package icomp.ufam.micaelyanlab1android.view;

import android.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.activeandroid.ActiveAndroid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

import icomp.ufam.micaelyanlab1android.R;
import icomp.ufam.micaelyanlab1android.controller.api.APICountries;
import icomp.ufam.micaelyanlab1android.model.bean.Country;
import icomp.ufam.micaelyanlab1android.model.dao.DAOCountry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap ;
    private Button voltar, mudarpais;
    private List<DAOCountry> countryDAOList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            countryDAOList = DAOCountry.getCountries();
            if ( countryDAOList.isEmpty() ) {
                fetchData();
                countryDAOList = DAOCountry.getCountries();
            }
        } catch (NullPointerException ex) {
            System.err.println("erro ao recuperar countries do BD");
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ActionBar actionBar = getActionBar();
        voltar = findViewById(R.id.voltarButton);
        mudarpais = findViewById(R.id.outropaisButton);

        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mudarpais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countryDAOList != null) {
                    Random random = new Random();
                    int randomInt = random.nextInt(countryDAOList.size());

                    DAOCountry randomCountry = countryDAOList.get(randomInt);
                    LatLng countryLatlng = new LatLng(
                            randomCountry.getLatitude(),
                            randomCountry.getLongitude()
                    );

                    mMap.clear();
                    Marker randomCountryMarker = mMap.addMarker(new MarkerOptions()
                            .position(countryLatlng)
                            .title(randomCountry.getName()));
                            // .snippet("population: " + randomCountry.getPopulation()));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(countryLatlng, 20));
                }
            }
        });
    }



    public void fetchData() {
        APICountries
            .getRestCountriesClient()
            .getCountries()
            .enqueue(new Callback<List<Country>>() {
                @Override
                public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                    if ( response.isSuccessful() ) {
                        List<Country> countryList = response.body();

                        ActiveAndroid.beginTransaction(); // para inserir todos os countries ou nenhum
                        try {
                            for (Country country : countryList) {
                                if (country.getLatlng().size() > 0) {
                                    DAOCountry daoCountry = new DAOCountry(country);
                                    daoCountry.save();
                                }
                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction(); // commit
                        }

                    } else System.err.println( response.errorBody() );
                }

                @Override
                public void onFailure(Call<List<Country>> call, Throwable t) {
                        t.printStackTrace();
                }
        });

    }




}
