package omtinez.ohpass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginScreen extends SherlockActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        (Ohpass.getApp()).onCreate();
//        ((Ohpass) getApplicationContext()).onCreate();
        // get user's master password
		final String pass = OhpassDB.getMasterHashed();
		
        // if the user already has stored a master password, present regular login screen.
        // otherwise present an extra field to set up a new master password
        setContentView(pass==null?R.layout.nlogin:R.layout.login);
        
        final EditText pwd1 = (EditText)findViewById(R.id.pwd1);
        final EditText pwd2 = (EditText)findViewById(R.id.pwd2);
        final TextView nomatchlabel = (TextView)findViewById(R.id.nomatchlabel);
        
        Button b = (Button)findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// retrieve the plain passwords
				String pwd = pwd1.getText().toString();
				String chkpwd = pass==null ? pwd2.getText().toString() : "";
				
				// if there is no password stored and both fields are blank, continue
				if (pass==null && pwd.equals("") && chkpwd.equals("")) {
					startActivity(new Intent(LoginScreen.this,MainScreen.class));
				
				// if there is no password stored and both fields match
				} else if (pass==null && pwd.equals(chkpwd)) {
					startActivity(new Intent(LoginScreen.this,MainScreen.class));
					OhpassDB.insertMaster(pwd);
					
					// update global state variable cached password
					(Ohpass.getApp()).cachePassword(pwd);
					
				// if there is not password stored and fields don't match
				} else if (pass==null && !pwd.equals(chkpwd)) {
					pwd1.setText("");
					pwd2.setText("");
					nomatchlabel.setVisibility(TextView.VISIBLE);
				
				// if pass is not null then there check against pwd in database
				} else if (pass!=null && OhpassDB.SHA1(pwd).equals(pass)) {
					startActivity(new Intent(LoginScreen.this,MainScreen.class));
					
					// update global state variable cached password
					(Ohpass.getApp()).cachePassword(pwd);
				
				// if pass is not null and the text field does not match
				} else if (pass!=null & !OhpassDB.SHA1(pwd).equals(pass)) {
					pwd1.setText("");
					nomatchlabel.setVisibility(TextView.VISIBLE);
				}
			}
        });
    }
}