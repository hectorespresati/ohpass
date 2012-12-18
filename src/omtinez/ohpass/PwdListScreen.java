package omtinez.ohpass;

import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

@SuppressWarnings("deprecation")
public class PwdListScreen extends SherlockListActivity {
	boolean EDIT_MODE, DELETE_MODE;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        
        // action bar home button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        // retrieve passwords from database
        List<String> names = OhpassDB.getNames();
        
        // Create an ArrayAdapter, that will hold the options from the menu and display them in the listview
		this.setListAdapter(new ArrayAdapter<String>(
				this,android.R.layout.simple_list_item_1, names));
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	// Get the item that was clicked
		String name = (String)this.getListAdapter().getItem(position);
		final String info[] = OhpassDB.getInfo(name);	// retrieve rest of info
		
    	if (EDIT_MODE) { // when the user has previously clicked the edit button
    		
    		Intent i = new Intent(PwdListScreen.this,AddPwdScreen.class);
			i.putExtra("edit", true);			// send edit mode on
			i.putExtra("name", name);			// send name
			i.putExtra("info", info);			// send rest of info
			startActivity(i);					// launch intent
			EDIT_MODE = false;					// exit edit mode
    	
    	} else if (DELETE_MODE) { // when the user has previously clicked the delete button
    		
    		OhpassDB.delete(name);	// delete entry
    		DELETE_MODE = false;	// exit delete mode
			
			// reload the list view
			this.onCreate(null);
    	
    	} else {
    	
			// retrieve info from database
			final AlertDialog alert = (new AlertDialog.Builder(this)).create();
			alert.setTitle(name);
			alert.setMessage(getResources().getString(R.string.password) + "\n" + info[0] + 
					(!info[1].equals("") ? "\n\n" + getResources().getString(R.string.comments) + "\n" + info[1] : "") +
					(!info[2].equals("") ? "\n\n" + getResources().getString(R.string.url) + "\n" + info[2] : ""));
			alert.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.copybutton), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
					clipboard.setText(info[0]);
			}});
			alert.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.gotourl), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(info[2]));
					try {
						startActivity(i);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(PwdListScreen.this, PwdListScreen.this.getResources().getString(R.string.badurl), Toast.LENGTH_LONG).show();
					} finally {
						alert.dismiss();
					}
			}});
			alert.show();
    	}
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.layout.pwdlist_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
			case android.R.id.home:
		        startActivity(new Intent(PwdListScreen.this,MainScreen.class));
				return true;
		
			case R.id.addItem:
				startActivity(new Intent(this,AddPwdScreen.class));
				return true;
			
			case R.id.editItem:
				if (!EDIT_MODE) Toast.makeText(this, "Click on an entry to edit it", Toast.LENGTH_SHORT).show();
				EDIT_MODE = !EDIT_MODE;
				DELETE_MODE = false;
				return true;
				
			case R.id.delItem:
				if (!DELETE_MODE) Toast.makeText(this, "Click on an entry to delete it", Toast.LENGTH_SHORT).show();
				DELETE_MODE = !DELETE_MODE;
				EDIT_MODE = false;
				return true;
		}
		
		return(super.onOptionsItemSelected(item));
	}
    
    @Override
    public void onBackPressed() {		// handle back button ourselves
        startActivity(new Intent(PwdListScreen.this,MainScreen.class));
    }
}