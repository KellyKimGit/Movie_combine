package com.example.movie_combine.MyHelper;

import android.content.Context;
import android.util.Log;

public class MyImageClickListener {
    private static final String TAG = "MyImageClickListener";

    private final String username; // 사용자명 변수 추가

    public MyImageClickListener(String username) {
        this.username = username;
    }

    public void onImageClick(String movieTitle, String emotion, String genre, String plot, float rating, byte[] imageData, Context context) {
        DBHelper dbHelper = new DBHelper(context, username); // DBHelper 생성 시 사용자명 전달
        long result = dbHelper.insertUserMovieInfo(movieTitle, emotion, genre, plot, rating, imageData);
        if (result != -1) {
            Log.d(TAG, "영화 정보 업데이트 성공");
        } else {
            Log.e(TAG, "영화 정보 업데이트 실패");
        }
        dbHelper.close();
    }
}
