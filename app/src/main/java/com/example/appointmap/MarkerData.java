package com.example.appointmap;

// 마커 정보를 담을 데이터 클래스
public class MarkerData {
    private String markerId;
    private String userEmail;
    private double latitude;
    private double longitude;
    private String title;
    private Boolean isChecked ;

    public MarkerData(){

    }

    public MarkerData(String markerId,String userEmail, double latitude, double longitude, String title) {
        this.markerId = markerId;
        this.userEmail = userEmail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        isChecked = false;
    }

    // 생성자, getter 및 setter 메서드 작성

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public String getMarkerId() {
        return markerId;
    }

    public String getUserEmail() { return userEmail; }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getIsChecked() {return isChecked;}
}
