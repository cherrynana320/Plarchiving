package com.example.appointmap;

// 마커 정보를 담을 데이터 클래스
public class MarkerData {
    private String markerId;
    private double latitude;
    private double longitude;
    private String title;

    public MarkerData(){

    }

    public MarkerData(String markerId, double latitude, double longitude, String title) {
        this.markerId = markerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }


    // 생성자, getter 및 setter 메서드 작성

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    // 추가적인 메서드나 필요한 기능을 구현할 수도 있습니다.
}
