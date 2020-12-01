package com.jyq.petidentifyapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *数据库辅助类
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String CREATE_PET_TABLE = "create table pet_data (" +
            "id integer primary key autoincrement, " +
            "name text, " +
            "type text, " +
            "sex text, " +
            "age int, " +
            "path text)";
    private SQLiteDatabase db;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, "database", null, 1);
        db = getWritableDatabase();
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PET_TABLE);
        Log.d(TAG, "onCreate: " + "table created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String saveBitmapToLocal(Bitmap bitmap) {
        int petNum = db.query("pet_data",
                null, null, null, null, null, null).getCount() + 1;
        try {
            String filePath = mContext.getFilesDir() + "/pet" + petNum + ".png";
            File file = new File(filePath);
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PetInfo> query() {
        Cursor cursor = db.query("pet_data", null, null, null, null, null, null);
        List<PetInfo> petList = new ArrayList<>();
        if (cursor.moveToNext()) {
            do {
                PetInfo pet = new PetInfo();
                pet.setPetName(cursor.getString(cursor.getColumnIndex("name")));
                pet.setPetType(cursor.getString(cursor.getColumnIndex("type")));
                pet.setPetSex(cursor.getString(cursor.getColumnIndex("sex")));
                pet.setPetAge(cursor.getInt(cursor.getColumnIndex("age")));
                pet.setPetPicPath(cursor.getString(cursor.getColumnIndex("path")));
                petList.add(pet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return petList;
    }

    public void insert(PetInfo petInfo) {
        ContentValues values = new ContentValues();
        values.put("name", petInfo.getPetName());
        values.put("type", petInfo.getPetType());
        values.put("sex", petInfo.getPetSex());
        values.put("age", petInfo.getPetAge());
        values.put("path", petInfo.getPetPicPath());
        db.insert("pet_data", null, values);
        values.clear();
    }

    public void close() {
        db.close();
    }
}
