package omtinez.ohpass;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Ohpass extends Application {
	
	// static variables to be retrieved by any class of this application
	private static SQLiteOpenHelper openHelper;
	private static Context mContext;
	private static String mPassword;
	
	@Override
    public void onCreate() {
        super.onCreate();
        
        // store the static variables
        openHelper = new OhpassDB(this);
        mContext = this.getApplicationContext();
        
	}
	
	public void cachePassword(String pwd) {
		mPassword = pwd;
	}
	
	public static String getCachedPassword() {
		return mPassword;
	}
	
	public static SQLiteDatabase getDB() {
        return openHelper.getWritableDatabase();
    }
    
    public static Context getContext() {
    	return mContext;
    }
    
    public static Ohpass getApp() {
    	return (Ohpass) mContext;
    }

}
