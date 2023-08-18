package com.example.movie_combine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movie_combine.MyHelper.DBHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private LinearLayout layout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.linearLayout);

        // Firebase 초기화
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usernameRef = database.getReference("Users").child("UserName");

        // Firebase에서 사용자 이름 가져오기
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    initializeDBHelperAndLoadData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    private void initializeDBHelperAndLoadData() {
        dbHelper = new DBHelper(this, username); // 사용자 이름으로 DBHelper 초기화
        layout = findViewById(R.id.linearLayout);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // tdmb_database.db 테이블에서 데이터 가져오기
        Cursor cursor = db.query("movies",
                new String[]{"title", "emotion", "genre", "plot", "rating", "image_data"},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            LinearLayout movieLayout = createMovieLayout(cursor);
            layout.addView(movieLayout);
        }

        cursor.close();
        dbHelper.close();
    }

    private LinearLayout createMovieLayout(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String emotion = cursor.getString(cursor.getColumnIndexOrThrow("emotion"));
        String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
        String plot = cursor.getString(cursor.getColumnIndexOrThrow("plot"));
        String ratingString = cursor.getString(cursor.getColumnIndexOrThrow("rating"));
        byte[] image_data = cursor.getBlob(cursor.getColumnIndexOrThrow("image_data"));

        float rating = Float.parseFloat(ratingString);

        LinearLayout movieLayout = new LinearLayout(this);
        movieLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        movieLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleTextView = new TextView(this);
        titleTextView.setText("Title: " + title);
        movieLayout.addView(titleTextView);

        TextView emotionTextView = new TextView(this);
        emotionTextView.setText("Emotion: " + emotion);
        movieLayout.addView(emotionTextView);

        TextView genreTextView = new TextView(this);
        genreTextView.setText("Genre: " + genre);
        movieLayout.addView(genreTextView);

        TextView plotTextView = new TextView(this);
        plotTextView.setText("Plot: " + plot);
        movieLayout.addView(plotTextView);

        TextView ratingTextView = new TextView(this);
        ratingTextView.setText("Rating: " + rating);
        movieLayout.addView(ratingTextView);

        if (image_data != null && image_data.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(image_data, 0, image_data.length, options);
            ImageView movieImageView = new ImageView(this);
            movieImageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            movieImageView.setImageBitmap(imageBitmap);

            // 영화 이미지 클릭 시 영화 정보 저장
            movieImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 영화 정보를 새로운 DB에 저장
                    long result = dbHelper.insertUserMovieInfo(title, emotion, genre, plot, rating, image_data);

                    if (result != -1) {
                        Toast.makeText(MainActivity.this, "시청한 영화 정보 저장 완료: " + title, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "시청한 영화 정보 저장 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            movieLayout.addView(movieImageView);
        }

        return movieLayout;
    }

    // 시청 기록 화면으로 이동하는 메서드
    public void openWatchHistoryActivity(View view) {
        Intent intent = new Intent(this, WatchHistoryActivity.class);
        startActivity(intent);
    }
}