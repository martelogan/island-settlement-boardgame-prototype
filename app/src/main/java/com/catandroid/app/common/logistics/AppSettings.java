package com.catandroid.app.common.logistics;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.catandroid.app.common.ui.activities.GameManagerActivity;
import com.catandroid.app.R;;

public class AppSettings extends SQLiteOpenHelper {

	private SQLiteDatabase db;
	private SQLiteStatement settingInsert;

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "catAndroid_settings.db";
	private static final String SETTINGS_TABLE = "settings";
	private static final String SETTINGS_INSERT = "INSERT INTO "
			+ SETTINGS_TABLE + " (name,value) VALUES (?,?)";

	private static final String[] QUERY_VALUE = { "value" };

	public AppSettings(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		settingInsert = null;

		getWritableDatabase();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		this.db = db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		Log.d(this.getClass().getName(), "need to initialize database");

		// create key/value pair table
		db.execSQL("CREATE TABLE " + SETTINGS_TABLE
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name TEXT, value TEXT)");

		// addCubic default options and players
		GameManagerActivity.setup(this);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.db = db;
		Log.d(this.getClass().getName(), "database upgrade from version "
				+ oldVersion + " to " + newVersion);
	}

	public void set(String attribute, String value) {
		set(db, attribute, value);
	}

	public void set(String attribute, int value) {
		set(attribute, Integer.toString(value));
	}

	public void set(String attribute, boolean value) {
		set(attribute, value ? "true" : "false");
	}

	public String get(String attribute) {
		String value = null;

		Cursor cursor = db.query(SETTINGS_TABLE, QUERY_VALUE, "name = \""
				+ attribute + "\"", null, null, null, "id desc");

		if (cursor != null && cursor.moveToFirst())
		{
			value = cursor.getString(0);
		}

		if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}

		return value;
	}

	public int getInt(String attribute) {
		try {
			return Integer.parseInt(get(attribute));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public boolean getBool(String attribute) {
		return Boolean.parseBoolean(get(attribute));
	}

	private void set(SQLiteDatabase db, String attribute, String value) {
		if (db.isReadOnly())
		{
			return;
		}

		if (settingInsert == null)
		{
			settingInsert = db.compileStatement(SETTINGS_INSERT);
		}

		settingInsert.bindString(1, attribute);
		settingInsert.bindString(2, value);
		settingInsert.executeInsert();
	}
}
