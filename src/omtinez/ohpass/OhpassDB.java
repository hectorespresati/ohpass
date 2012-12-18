package omtinez.ohpass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Base64;

public class OhpassDB extends SQLiteOpenHelper {
	
	public OhpassDB(Context context) {
		super(context, "ohpass", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		// create table
		db.execSQL("CREATE TABLE IF NOT EXISTS ohpass (" +
		"id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, pwd TEXT, comments TEXT, url TEXT)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			if (Ohpass.getCachedPassword() != null) insertMaster(Ohpass.getCachedPassword());
			else {
				
				File tmp = new File(Environment.getExternalStorageDirectory().toString()+"/ohpass.csv");
				
				// export the database to a temp file
				exportDB(tmp);
				
				// empty the database
				for (String n : getNames()) delete(n);
				
				// re-import the previously saved passwords
				importDB(tmp);
			}
		}
	}

	public static void insert(String name, String pwd, String comments, String url) {
		
		// encrypt the password with the master password if possible
		String masterpwd = Ohpass.getCachedPassword();
		if (masterpwd != null) pwd = AES(masterpwd,pwd,true);
		
		Ohpass.getDB().execSQL("INSERT OR REPLACE INTO ohpass (name,pwd,comments,url) VALUES ('" + 
			name.replaceAll("'", "") + "','" + pwd.replaceAll("'", "") + "','" + comments.replaceAll("'", "") + "','" + url.replaceAll("'", "") + "')");
	}
	
	public static void insertMaster(String pwd) {
		
		File tmp = new File(Environment.getExternalStorageDirectory().toString()+"/ohpass.csv");
		
		// export the database to a temp file
		exportDB(tmp);
		
		// empty the database
		for (String n : getNames()) delete(n);
		
		// insert hashed password into database
		Ohpass.getDB().execSQL("INSERT OR REPLACE INTO ohpass (name,pwd,comments,url) VALUES (" + 
			"'Ohpass Master Password','" + SHA1(pwd) + "','This is the app master password hashed','')");
		
		// re-import the previously saved passwords
		importDB(tmp);
	}
	
	public static String getMasterHashed() {
		Cursor c = Ohpass.getDB().rawQuery("SELECT pwd FROM ohpass WHERE name='Ohpass Master Password'", null);
		if(c.moveToNext()) return c.getString(0);
		return null;
	}
	
	public static void delete(String name) {
		Ohpass.getDB().execSQL("DELETE FROM ohpass WHERE name='" + name + "'");
	}
	
	
	
	// info has { pwd, comments, url }
	public static String[] getInfo(String name) {
		
		// decrypt the password with the master password if possible
		String masterpwd = Ohpass.getCachedPassword();
		
		Cursor c = Ohpass.getDB().rawQuery("SELECT pwd,comments,url FROM ohpass WHERE name='" + name + "'", null);
		if(c.moveToNext()) {
			String[] info =  {masterpwd==null?c.getString(0):AES(masterpwd,c.getString(0),false),c.getString(1),c.getString(2)};
			return info;
		}
		return null;
	}
	
	public static List<String> getNames() {
		Cursor c = Ohpass.getDB().rawQuery("SELECT name FROM ohpass", null);
		List<String> names = new ArrayList<String>();
		while (c.moveToNext()) names.add(c.getString(0));
		return names;
	}
	
	public static List<String> getAll() {
		
		Cursor c = Ohpass.getDB().rawQuery("SELECT name,pwd,comments,url FROM ohpass", null);
		List<String> all = new ArrayList<String>();
		while (c.moveToNext()) {
			String[] info = getInfo(c.getString(0));
			all.add("\""+c.getString(0)+"\",\""+info[0]+"\",\""+info[1]+"\",\""+info[2]+"\";");

		}

		return all;
	}
	
	public static boolean exportDB(File out) {
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			for(String x : getAll()) bw.write(x);
			
			// clean up
			bw.flush();
			bw.close();
			
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static void importDB(File in) {
		
		BufferedReader br;
		
		try {
			String line;
			br = new BufferedReader(new FileReader(in));
			while ((line = br.readLine()) != null) {
				String raw = line.replaceAll("\"","");
				
				for (String entry : raw.split(";")) {
					String[] fields = entry.split(",",4);
					if (!entry.equals("") && !fields[0].equals("Ohpass Master Password")) {
						if (Ohpass.getCachedPassword() == null)
							Ohpass.getDB().execSQL("INSERT OR REPLACE INTO ohpass (name,pwd,comments,url) VALUES (" + 
									"'"+fields[0]+"','"+fields[1]+"','"+fields[2]+"','"+fields[3]+"')");
						else insert(fields[0],fields[1],fields[2],fields[3]);
					}
				}
			}
			
			// clean up
			br.close();
			in.delete();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// method used to hash the master password
	public static String SHA1(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
            digest.update(s.getBytes(),0,s.length());
            String hash = new BigInteger(1, digest.digest()).toString(16);
            return hash;
        }	catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }	return "";
    }
	
	// method used to encrypt other passwords with the paster password
	public static String AES(String pwd, String s, boolean ENCRYPT) {

		try {
			
			// get secret key from master password
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(pwd.toCharArray(),"ohpass".getBytes(), 1024, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			// initialize cipher
			Cipher mcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			mcipher.init(ENCRYPT?Cipher.ENCRYPT_MODE:Cipher.DECRYPT_MODE,key,new IvParameterSpec("ohpass1234567890".getBytes()));
			
			// perform encryption/decryption
			if (ENCRYPT) return Base64.encodeToString(mcipher.doFinal(s.getBytes("UTF-8")), Base64.NO_WRAP);
			else return new String(mcipher.doFinal(Base64.decode(s, Base64.NO_WRAP)),"UTF-8");
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
}
