package com.example.appointmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken; // 자동 완성 세션에 대한 새 토큰을 만들어서 FindAutocompletePredictionsRequest에 전달

    private PlacesAdapter placesAdapter;
    private EditText et_search;
    // private ProgressBar progressBar;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference markersRef = database.getReference("markers");// 파이어베이스 db

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //String apiKey = BuildConfig.PLACES_API_KEY;
        String apiKey = "AIzaSyAyzkxnFRqbC8Daa2x6Ahc9AIF3oXx2HgU";

        // Android용 Places SDK를 초기화하고, Places.initialize()를 호출할 때 API 키를 전달합니다.
        if(!Places.isInitialized()){
            Places.initialize(this,apiKey);
        }
        placesClient = Places.createClient(this);

        sessionToken = AutocompleteSessionToken.newInstance();

        et_search = findViewById(R.id.et_search);
        // progressBar = findViewById(R.id.progressBar);
        ListView lv_places = findViewById(R.id.lv_places);

        // progressBar.setVisibility(View.GONE);

        placesAdapter = new PlacesAdapter(this);
        lv_places.setAdapter(placesAdapter);
        lv_places.setOnItemClickListener(new AdapterView.OnItemClickListener( ) {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(placesAdapter.getCount() > 0){
                    detailPlace(placesAdapter.predictions.get(position).getPlaceId());
                }
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener( ) {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    if(et_search.length() > 0){
                        searchPlaces();
                    }
                }
                return false;
            }
        });
    }

    private void  searchPlaces() {
        // progressBar.setVisibility(View.VISIBLE);

        // LocationBias 지정된 지리적 경계 내에서 결과를 선호
        final LocationBias bias = RectangularBounds.newInstance(
                new LatLng(33.0041, 124.0014), // 대한민국 남서쪽 좌표
                new LatLng(38.6781, 131.8749)  // 대한민국 북동쪽 좌표
        );

        // 자동 완성 요청
        // 자동 완성 세션에 대한 새 토큰을 만듭니다. FindAutocompletePredictionsRequest에 전달합니다,
        // 사용자가 선택을 할 때(ex..fetchPlace()를 호출할 때) 다시 한 번 선택합니다.
        final FindAutocompletePredictionsRequest newRequest
                = FindAutocompletePredictionsRequest
                .builder( )
                .setSessionToken(sessionToken)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(et_search.getText( ).toString( ))
                .setLocationBias(bias)
                .setCountries("KR")
                .build( );


        // PlaceAutocomplete 자동완성
        // 앱은 placesClient.findAutocompletePredictions(newRequest)를 불러 사용함으로써 자동 완성 API에서 예상 장소 이름 및/또는 주소 목록을 가져올 수 있습니다.
        placesClient.findAutocompletePredictions(newRequest).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>( ) {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                // 예상결과를 리스트에 넣기
                List<AutocompletePrediction> predictions = findAutocompletePredictionsResponse.getAutocompletePredictions();
                placesAdapter.setPredictions(predictions);
                //progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener( ) {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ApiException){
                    ApiException apiException = (ApiException) e;
                    Log.e("SearchActivity", "Place not found: "+ apiException.getStatusCode());
                }

            }
        });
    }

    // ** 장소정보 가져오는 메서드
    private void detailPlace(String placeId){
        //ProgressDialog progressDialog = new ProgressDialog(this);
        //progressDialog.setMessage("Loading");
        //progressDialog.show();

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        //
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>( ) {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                //progressDialog.dismiss();
                Place place = fetchPlaceResponse.getPlace();
                LatLng latLng = place.getLatLng();

                if(latLng != null){
                    // Toast.makeText(SearchActivity.this, "LatLng : " + latLng, Toast.LENGTH_LONG).show();
                    // 위도, 데이터 intent에 담아서 보내기

                    saveMarkerData(latLng.latitude, latLng.longitude, place.getName());

                    Intent Intent = new Intent(SearchActivity.this, GmapActivity.class);
                    startActivity(Intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener( ) {
            @Override
            public void onFailure(@NonNull Exception e) {
                //progressDialog.dismiss();
                if(e instanceof ApiException){
                    final ApiException apiException = (ApiException) e;
                    Log.e("SearchActivity", "Place not found: "+ e.getMessage());
                    final int statusCode = apiException.getStatusCode();
                }
            }
        });
    }

    private static class PlacesAdapter extends BaseAdapter{
        private final List<AutocompletePrediction> predictions = new ArrayList<>();
        private final Context context;

        private PlacesAdapter(Context context) {
            this.context = context;
        }

        public void setPredictions(List<AutocompletePrediction> predictions){
            this.predictions.clear();
            this.predictions.addAll(predictions);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return predictions.size();
        }

        @Override
        public Object getItem(int position) {
            return predictions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_item_place, parent, false);
            TextView tv_shortAddress = v.findViewById(R.id.tv_shortAddress);
            TextView tv_longAddress = v.findViewById(R.id.tv_longAddress);

            tv_shortAddress.setText(predictions.get(position).getPrimaryText(null));
            tv_longAddress.setText(predictions.get(position).getSecondaryText(null));
            return v;
        }
    }


    // ** 사용자가 저장 버튼을 눌러 db에 마커데이터를 저장.
    private void saveMarkerData(double latitude, double longitude, String title) {
        //DatabaseReference markersRef = database.getReference("markers");
        String markerId = markersRef.push().getKey();

        MarkerData markerData = new MarkerData(markerId, latitude, longitude, title);
        markersRef.child(markerId).setValue(markerData); // db에 저장.
    }




}