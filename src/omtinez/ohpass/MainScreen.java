package omtinez.ohpass;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;

public class MainScreen extends SherlockListActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        
        // Create an ArrayAdapter, that will hold the options from the menu and display them in the listview
		this.setListAdapter(new ArrayAdapter<String>(
				this,android.R.layout.simple_list_item_1, this.getResources().getStringArray(R.array.menu_options)));
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	menu: switch(position) {
    	case 0:		Intent i = new Intent(MainScreen.this,PwdListScreen.class);
    				startActivity(i);
    				break menu;
    	
    	case 1:		Toast.makeText(this, "Password Generation under development", Toast.LENGTH_LONG).show();
    				break menu;
    	
    	case 2:		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

	    				// get database info
//						final List<String> all = OhpassDB.getAll();
						
						// display textbox to select output file
						File sd = Environment.getExternalStorageDirectory();
						final AlertDialog alert = (new AlertDialog.Builder(MainScreen.this)).create();
						alert.setTitle(MainScreen.this.getResources().getString(R.string.typepath));
						final EditText input = new EditText(MainScreen.this);
						input.setText(sd.toString()+"/ohpass.csv");	// initialize textbox with sd path in it and ohpass.csv file
						alert.setView(input);
						
						// save button
						alert.setButton(DialogInterface.BUTTON_POSITIVE, MainScreen.this.getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// save the file
								if (!OhpassDB.exportDB(new File(input.getText().toString())))
									Toast.makeText(MainScreen.this, MainScreen.this.getResources().getString(R.string.notsaved), Toast.LENGTH_LONG).show();

//								try {
//									File out = new File(input.getText().toString());
//									BufferedWriter bw = new BufferedWriter(new FileWriter(out));
//									for(String x : all) {
//										bw.write(x);
////										bw.newLine();
//									}
//									
//									// clean up
//									bw.flush();
//									bw.close();
//								} catch (IOException e) {
//									Toast.makeText(MainScreen.this, MainScreen.this.getResources().getString(R.string.notsaved), Toast.LENGTH_LONG).show();
//								}
						}});
						
					
						// cancel button
						alert.setButton(DialogInterface.BUTTON_NEGATIVE, MainScreen.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								alert.dismiss();
						}});
						
						alert.show();
						} break menu;
    	
    	case 3:		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
							
						// display textbox to select output file
						File sd = Environment.getExternalStorageDirectory();
						final AlertDialog alert = (new AlertDialog.Builder(MainScreen.this)).create();
						alert.setTitle(MainScreen.this.getResources().getString(R.string.typepath));
						final EditText input = new EditText(MainScreen.this);
						input.setText(sd.toString()+"/ohpass.csv");	// initialize textbox with sd path in it and ohpass.csv file
						alert.setView(input);
						
						// open button
						alert.setButton(DialogInterface.BUTTON_POSITIVE, MainScreen.this.getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								File in = new File(input.getText().toString());
								OhpassDB.importDB(in);
								Toast.makeText(MainScreen.this, MainScreen.this.getResources().getString(R.string.notopen), Toast.LENGTH_LONG).show();
						}});
						
						// choose button
						alert.setButton(DialogInterface.BUTTON_NEUTRAL, MainScreen.this.getResources().getString(R.string.choose), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								try {	// try using the open intents file browser
								Intent intent = new Intent("org.openintents.action.PICK_FILE");
								startActivityForResult(intent, 1);
								} catch (Exception e) {
									Toast.makeText(MainScreen.this, MainScreen.this.getResources().getString(R.string.notpick), Toast.LENGTH_LONG).show();
								}
						}});
						
						// cancel button
						alert.setButton(DialogInterface.BUTTON_NEGATIVE, MainScreen.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								alert.dismiss();
						}});
						
						alert.show();
    				} break menu;
				}
    }
    
    @Override
    public void onBackPressed() {
       this.moveTaskToBack(true);
    }
}