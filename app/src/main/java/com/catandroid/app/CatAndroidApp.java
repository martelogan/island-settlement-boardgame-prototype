/*
 * island-settlement-boardgame-prototype
 * Copyright (C) 2017, Logan Martel, Frederick Parsons
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
