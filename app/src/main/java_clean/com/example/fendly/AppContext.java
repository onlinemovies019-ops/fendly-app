package com.example.fendly;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public final class AppContext extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		FirebaseApp.initializeApp(this);
	}
}