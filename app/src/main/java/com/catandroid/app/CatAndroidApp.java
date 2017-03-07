package com.catandroid.app;

import android.app.Application;
import android.content.Context;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.logistics.AppSettings;
import com.catandroid.app.common.ui.graphics_controllers.TextureManager;

public class CatAndroidApp extends Application {

	private AppSettings appSettingsInstance;
	private TextureManager textureManagerInstance;
	private Board boardInstance;
	
	private static Context context;
	private static CatAndroidApp instance;

	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		
		context = getBaseContext();

		// load settings
		appSettingsInstance = new AppSettings(getBaseContext());

		setTextureManagerInstance(null);
		setBoardInstance(null);
	}
	
	public static CatAndroidApp getInstance() {
		return instance;
	}
	
	public Context getContext() {
		return context;
	}

	public void setBoardInstance(Board boardInstance) {
		this.boardInstance = boardInstance;
	}

	public Board getBoardInstance() {
		return boardInstance;
	}

	public void setTextureManagerInstance(TextureManager textureManagerInstance) {
		this.textureManagerInstance = textureManagerInstance;
	}

	public TextureManager getTextureManagerInstance() {
		return textureManagerInstance;
	}

	public AppSettings getAppSettingsInstance() {
		return appSettingsInstance;
	}
}
