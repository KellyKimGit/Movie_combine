package com.example.movie_combine;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movie_combine.MyHelper.DBHelper;

public class WatchHistoryActivity extends AppCompatActivity {
    private static final String TAG = "WatchHistoryActivity";

    private DBHelper dbHelper;
    private LinearLayout layout;
    private String username = "user123"; // 사용자명을 동적으로 얻어오는 로직 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_history);

        dbHelper = new DBHelper(this, username); // 사용자명 추가
        layout = findViewById(R.id.linearLayoutWatchHistory);

        SQLiteDatabase recordsDb = dbHelper.getReadableDatabase();

        if (recordsDb == null) {
            Log.e(TAG, "Failed to open database");
            return;
        }

        Cursor recordsCursor = recordsDb.query(dbHelper.getUserRecordsTableName(username),
                new String[]{"title", "emotion", "genre", "plot", "rating", "image_data"},
                null, null, null, null, null);

        if (recordsCursor == null) {
            Log.e(TAG, "Failed to query the movie_records table");
            return;
        }

        if (recordsCursor.moveToFirst()) {
            do {
                LinearLayout movieLayout = createMovieLayout(recordsCursor);
                layout.addView(movieLayout);
            } while (recordsCursor.moveToNext());
        } else {
            Toast.makeText(this, "No data found in movie_records table.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No data found in movie_records table.");
        }

        recordsCursor.close();
        dbHelper.close();
    }

    private LinearLayout createMovieLayout(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String emotion = cursor.getString(cursor.getColumnIndexOrThrow("emotion"));
        String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
        String plot = cursor.getString(cursor.getColumnIndexOrThrow("plot"));
        String ratingString = cursor.getString(cursor.getColumnIndexOrThrow("rating"));
        byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("image_data"));

        float savedRating = Float.parseFloat(ratingString);

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
        ratingTextView.setText("Rating: " + savedRating);
        movieLayout.addView(ratingTextView);

        if (imageData != null && imageData.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
            ImageView movieImageView = new ImageView(this);
            movieImageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            movieImageView.setImageBitmap(imageBitmap);

            movieLayout.addView(movieImageView);
        }

        return movieLayout;
    }
}
