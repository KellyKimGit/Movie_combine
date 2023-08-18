package com.example.movie_combine.MyHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;


public class DBHelper extends SQLiteOpenHelper {
    private final static String TAG = "DBHelper";
    private static final String MOVIE_DB_NAME = "tdmb_database.db";
    private static String DB_PATH = "";

    //tdmb_database.db
    private static final String MOVIE_TABLE_NAME = "movies";
    private static final String MOVIE_COLUMN_ID = "_id";
    private static final String MOVIE_COLUMN_TITLE = "title";
    private static final String MOVIE_COLUMN_EMOTION = "emotion";
    private static final String MOVIE_COLUMN_GENRE = "genre";
    private static final String MOVIE_COLUMN_PLOT = "plot";
    private static final String MOVIE_COLUMN_RATING = "rating";
    private static final String MOVIE_COLUMN_IMAGE = "image_data";

    //시청자 기록 테이블
    private static final String RECORDS_TABLE_NAME = "movie_records";
    private static final String RECORDS_COLUMN_ID = "_id";
    private static final String RECORDS_COLUMN_TITLE = "title";
    private static final String RECORDS_COLUMN_EMOTION = "emotion";
    private static final String RECORDS_COLUMN_GENRE = "genre";
    private static final String RECORDS_COLUMN_PLOT = "plot";
    private static final String RECORDS_COLUMN_RATING = "rating";
    private static final String RECORDS_COLUMN_IMAGE = "image_data";

    private final Context mContext;
    private final String username;

    private static final int MOVIE_DB_VERSION = 9;

    public DBHelper(Context context, String username) {
        super(context, MOVIE_DB_NAME, null, MOVIE_DB_VERSION);
        DB_PATH = context.getDatabasePath(MOVIE_DB_NAME).getAbsolutePath();
        this.mContext = context;
        this.username = username; // 사용자명 저장

        // tdmb_database.db 내부 저장소 존재 여부
        if (!databaseExists()) {
            if (dbCopy(MOVIE_DB_NAME, DB_PATH)) {
                Log.d(TAG, "tdmb_database.db 파일 복사 완료.");
            } else {
                Log.e(TAG, "tdmb_database.db 파일 복사 실패.");
            }
        }

        // 테이블 없으면 생성
        SQLiteDatabase db = getWritableDatabase();
        createUserMovieRecordsTable(db);
        db.close();
    }

    private boolean databaseExists() {
        File dbFile = mContext.getDatabasePath(MOVIE_DB_NAME);
        return dbFile.exists();
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createMovieTable(db);
        Log.d(TAG, "onCreate() - 영화 정보 테이블 생성 완료!");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.d(TAG, "onOpen() - 내부 저장된 DB 오픈 완료.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    private boolean dbCopy(String dbName, String targetPath) {
        try {
            InputStream inputStream = mContext.getAssets().open(dbName);
            OutputStream outputStream = new FileOutputStream(targetPath);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = inputStream.read(mBuffer)) > 0) {
                outputStream.write(mBuffer, 0, mLength);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.e(TAG, "dbCopy() - 데이터베이스 복사 완료.");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "dbCopy() - 데이터베이스 복사 중 IOException 발생");
            return false;
        }
    }

    private void createMovieTable(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + MOVIE_TABLE_NAME + " (" +
                MOVIE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MOVIE_COLUMN_TITLE + " TEXT, " +
                MOVIE_COLUMN_EMOTION + " TEXT, " +
                MOVIE_COLUMN_GENRE + " TEXT, " +
                MOVIE_COLUMN_PLOT + " TEXT, " +
                MOVIE_COLUMN_RATING + " REAL, " +
                MOVIE_COLUMN_IMAGE + " BLOB)";
        db.execSQL(createTableQuery);
        Log.d(TAG, "createMovieTable() - 영화 정보 테이블 생성 완료.");
    }

    private void createUserMovieRecordsTable(SQLiteDatabase db) {
        String tableName = getUserRecordsTableName(username);
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                RECORDS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RECORDS_COLUMN_TITLE + " TEXT, " +
                RECORDS_COLUMN_EMOTION + " TEXT, " +
                RECORDS_COLUMN_GENRE + " TEXT, " +
                RECORDS_COLUMN_PLOT + " TEXT, " +
                RECORDS_COLUMN_RATING + " REAL, " +
                RECORDS_COLUMN_IMAGE + " BLOB)";
        db.execSQL(createTableQuery);
        Log.d(TAG, "createUserMovieRecordsTable() - User movie records table created: " + tableName);
    }

    public long insertUserMovieInfo(String movieTitle, String emotion, String genre, String plot, float rating, byte[] image_data) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(RECORDS_COLUMN_TITLE, movieTitle);
        values.put(RECORDS_COLUMN_EMOTION, emotion);
        values.put(RECORDS_COLUMN_GENRE, genre);
        values.put(RECORDS_COLUMN_PLOT, plot);
        values.put(RECORDS_COLUMN_RATING, String.format(Locale.US, "%.2f", rating));
        values.put(RECORDS_COLUMN_IMAGE, image_data);

        String tableName = getUserRecordsTableName(username);
        long result = db.insert(tableName, null, values);

        if (result != -1) {
            Log.d(TAG, "insertUserMovieInfo - 영화 정보 업데이트 성공: " + movieTitle);
            Log.d(TAG, "insertUserMovieInfo - 영화 제목: " + movieTitle);
            Log.d(TAG, "insertUserMovieInfo - 감정: " + emotion);
            Log.d(TAG, "insertUserMovieInfo - 장르: " + genre);
            Log.d(TAG, "insertUserMovieInfo - 줄거리: " + plot);
            Log.d(TAG, "insertUserMovieInfo - 평점: " + String.format(Locale.US, "%.2f", rating));
            Log.d(TAG, "insertUserMovieInfo - 이미지 데이터 길이: " + (image_data != null ? image_data.length : 0));
            db.setTransactionSuccessful(); // 트랜잭션 성공으로 설정
        } else {
            Log.e(TAG, "insertUserMovieInfo - 영화 정보 업데이트 실패: " + movieTitle);
            Log.e(TAG, "insertUserMovieInfo - 실패 원인: 데이터베이스에 데이터를 삽입하는 중에 오류가 발생했습니다.");
        }

        db.endTransaction(); // 트랜잭션 종료
        db.close();

        return result;
    }
    public String getUserRecordsTableName(String username) {
        return "user_" + username + "_records";
    }

}
