package omtinez.ohpass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;

public class AddPwdScreen extends SherlockActivity {
	
	boolean edit = false;
	String namestring = null;
	String[] info = null;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpwd);
        
        // get intent extra to see if edit mode is needed
        Intent i = this.getIntent();
        edit = i.getBooleanExtra("edit", false);
        if(edit) {
        	namestring = i.getStringExtra("name");
        	info = i.getStringArrayExtra("info");
        }
        
        final EditText name = (EditText)findViewById(R.id.editText1);
        if(edit)name.setText(namestring);
        final EditText pwd = (EditText)findViewById(R.id.editText2);
        if(edit)pwd.setText(info[0]);
        final EditText comments = (EditText)findViewById(R.id.editText3);
        if(edit)comments.setText(info[1]);
        final EditText url = (EditText)findViewById(R.id.editText4);
        if(edit)url.setText(info[2]);
        
        Button addbutton = (Button)findViewById(R.id.button1);
        if(edit)addbutton.setText(this.getResources().getString(R.string.editbutton));
        addbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// parse and edit url value if necessary
				String uri = !url.getText().toString().startsWith("http://") && !url.getText().toString().equals("") ? "http://"+url.getText().toString() : url.getText().toString();
				// connect to database and store values
				if(edit)OhpassDB.delete(namestring);
				OhpassDB.insert(name.getText().toString(), pwd.getText().toString(), comments.getText().toString(), uri);
				// go back to passwords screen
				Intent i = new Intent(AddPwdScreen.this,PwdListScreen.class);
				startActivity(i);
		}});
    }
}