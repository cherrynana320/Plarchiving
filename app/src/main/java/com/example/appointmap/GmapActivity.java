package com.example.appointmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GmapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // 어댑터 생성
    List<MarkerData> markerList = new ArrayList<>(); // 마커 데이터 리스트

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference markersRef = database.getReference("markers");// 파이어베이스 db

    GoogleMap googleMap;
    private FirebaseAuth mAuth ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmap);

        // ** 마커 리스트
        MarkerAdapter markerAdapter = new MarkerAdapter(this, markerList);
        ListView lv_markers = findViewById(R.id.lv_markers);
        lv_markers.setAdapter(markerAdapter);

        // ** 지도
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.gmap);

        mapFragment.getMapAsync(this); //GoogleMap 인스턴스를 사용할 준비가 되면 트리거될 콜백 객체를 설정
        // 즉, Async라는 뜻은 비동기화 = 순서가 상관없이 이루어짐.
        // this(콜백)이 실행될 때 준비되었을 때, 프레그먼트들한테 붙여줘라 라는 뜻.



        // 1. 읽기
        markersRef.addValueEventListener( new ValueEventListener( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 경로의 전체 내용을 읽고 변경사항을 수신대기함
                // 이벤트 발생 시점에 특정 경로에 있던 콘텐츠의 정적 스냅샷을 읽을 수 있음.
                // 리스너가 연결될 때 한 번 트리거된 후, 하위 요소가 포함된 데이터가 변경될 때마다 다시 트리거됨.(트리거:호출)
                // 하위 데이터를 포함하여 해당 위치의 모든 데이터를 포함하는 스냅샷이 이벤트 콜백에 전달됩니다.
                // 데이터가 없는 경우 스냅샷은 exists() 호출 시 false를 반환하고 getValue() 호출 시 null을 반환합니다.

                googleMap.clear(); // 변경 내용 쓰기 전에, 기존의 마커들 지우기
                markerList.clear(); // 리스트의 모든 아이템 제거

                // 향상된 for 반복문
                for (DataSnapshot markerSnapshot : dataSnapshot.getChildren()) {
                    // 스냅샷에 대해 getValue()를 호출 -> 데이터의 자바 객체 표현이 반환
                    // 해당 위치에 데이터가 없는 경우 getValue()를 호출 -> null 반환
                    MarkerData markerData = markerSnapshot.getValue(MarkerData.class);

                    // ** 마커 리스트 채우기 **
                    markerList.add(markerData); // 리스트에 추가


                    if (markerData.getIsChecked()) {
                        int itemIndex = markerList.indexOf(markerData);
                        if (itemIndex != -1) {
                            // 아이템의 위치를 확인하고 인덱스에 해당하는 뷰를 가져옴
                            int startIndex = lv_markers.getFirstVisiblePosition(); // 리스트뷰에서 보이는 첫 번째 아이템의 인덱스
                            View item = lv_markers.getChildAt(itemIndex - startIndex);
                            if (item != null) {
                                CheckBox checkBox = item.findViewById(R.id.checkbox);
                                checkBox.setChecked(true);
                            }
                        }
                    }
                    else{
                        int itemIndex = markerList.indexOf(markerData);
                        if (itemIndex != -1) {
                            // 아이템의 위치를 확인하고 인덱스에 해당하는 뷰를 가져옴
                            int startIndex = lv_markers.getFirstVisiblePosition(); // 리스트뷰에서 보이는 첫 번째 아이템의 인덱스
                            View item = lv_markers.getChildAt(itemIndex - startIndex);
                            if (item != null) {
                                CheckBox checkBox = item.findViewById(R.id.checkbox);
                                checkBox.setChecked(false);
                            }
                        }
                    }

                    markerAdapter.notifyDataSetChanged(); // 어댑터 갱신

                    // ** 마커 추가 **
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();

                    BitmapDrawable bitmapdraw1 = (BitmapDrawable)getResources().getDrawable(R.drawable.heart_green);
                    Bitmap a = bitmapdraw1.getBitmap();
                    Bitmap marker_green = Bitmap.createScaledBitmap(a,100,140,false);

                    BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.heart_orange);
                    Bitmap b = bitmapdraw2.getBitmap();
                    Bitmap marker_orange = Bitmap.createScaledBitmap(b,100,140,false);

                    if(markerData.getUserEmail().equals(user.getEmail())) { // 내 마커
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(markerData.getLatitude( ), markerData.getLongitude( )))
                                .title(markerData.getTitle( ))
                                .icon(BitmapDescriptorFactory.fromBitmap(marker_orange));
                        googleMap.addMarker(markerOptions); // 구글맵 화면에 마커 추가
                    }
                    else { // 친구 마커
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(markerData.getLatitude( ), markerData.getLongitude( )))
                                .title(markerData.getTitle( ))
                                .icon(BitmapDescriptorFactory.fromBitmap(marker_green));
                        googleMap.addMarker(markerOptions);
                    }


                }
            }

            // ** 읽기가 취소되면 호출되는 메서드
            // 클라이언트에 Firebase 데이터베이스 위치에서 데이터를 읽을 수 있는 권한이 없으면 읽기가 취소됨.
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", "Database Error: " + error.getMessage());
            }
        });


        Button btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 입력값 변수에 담기
//                double str_latitude = Double.parseDouble(et_latitude.getText().toString()); // String -> Double로 형 변환

                Intent intent = new Intent(GmapActivity.this, SearchActivity.class);

                startActivity(intent); // 다른 액티비티로 이동

            }
        });
    }

    // 지도 준비
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        LatLng KOREA= new LatLng(37.5429545, 127.0565597);
        map.moveCamera(CameraUpdateFactory.newLatLng(KOREA));
        map.animateCamera(CameraUpdateFactory.zoomTo(12));

        googleMap = map; // 만들어진 값 화면에 보여주기
    }


    public class MarkerAdapter extends ArrayAdapter<MarkerData> {

        private Context mContext;
        private List<MarkerData> mMarkerList;

        public MarkerAdapter(Context context, List<MarkerData> markerList) {
            super(context, 0, markerList);
            mContext = context;
            mMarkerList = markerList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_marker, parent, false);
            }

            // 처음에 마커 리스트에 이름 채우기.
            MarkerData selectedMarkerData = mMarkerList.get(position); // 선택된 줄의 마커데이터
            String title = selectedMarkerData.getTitle(); // 선택된 줄의 마커데이터의 이름
            TextView textViewTitle = convertView.findViewById(R.id.tv_markerTitle); // 마커 텍스트뷰
            textViewTitle.setText(title); // 마커텍스트뷰 안에 선택 된 줄의 마커데이터 이름 넣기

            CheckBox cb = convertView.findViewById(R.id.checkbox);
            cb.setChecked(selectedMarkerData.getIsChecked()); // 체크박스 상태 설정

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 체크 상태가 변경되었을 때 호출되는 콜백 메서드
                    selectedMarkerData.setChecked(isChecked); // 마커 데이터의 isChecked 필드 업데이트

                    // 파이어베이스에 변경된 데이터 저장
                    markersRef.child(selectedMarkerData.getMarkerId()).child("isChecked").setValue(isChecked);
                }
            });

            return convertView;
        }
    }



}








