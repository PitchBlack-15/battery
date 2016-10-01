package com.example.batteryinformation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.UUID;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class MainActivity extends Activity {

	private PowerManager.WakeLock wl;
	//widgets
	private TextView batteryInfo;
	Button btnPaired;
	ListView devicelist;
	//Bluetooth
	private BluetoothAdapter myBluetooth = null;
	private Set<BluetoothDevice> pairedDevices;
	String address = null;
	private ProgressDialog progress;
	BluetoothSocket btSocket = null;
	private boolean isBtConnected = false;
	//SPP UUID. Look for it
	static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static String EXTRA_ADDRESS = "device_address";



	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		wl.acquire();
		//Calling widgets
		//batteryInfo=(TextView)findViewById(R.id.textViewBatteryInfo);
		btnPaired = (Button)findViewById(R.id.button_scan);
		devicelist = (ListView)findViewById(R.id.listView);

		//if the device has bluetooth
		myBluetooth = BluetoothAdapter.getDefaultAdapter();
		if(myBluetooth == null)
		{
			//Show a mensag. that the device has no bluetooth adapter
			Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

			//finish apk
			finish();
		}
		else if(!myBluetooth.isEnabled())
		{
			//Ask to the user turn the bluetooth on
			Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnBTon,1);
		}

		btnPaired.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pairedDevicesList();
			}
		});

	}

	private void pairedDevicesList()
	{
		pairedDevices = myBluetooth.getBondedDevices();
		ArrayList list = new ArrayList();

		if (pairedDevices.size()>0)
		{
			for(BluetoothDevice bt : pairedDevices)
			{
				list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
		}

		final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
		devicelist.setAdapter(adapter);
		devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

	}


	private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
		{
			// Get the device MAC address, the last 17 chars in the View
			String info = ((TextView) v).getText().toString();
			address = info.substring(info.length() - 17);
			new ConnectBT().execute(); //Call the class to connect
//			// Make an intent to start next activity.
//			Intent i = new Intent(MainActivity.this, ledControl.class);
//			//Change the activity.
//			i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
//			startActivity(i);
		}
	};


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_device_list, menu);
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}



	private void showText(String msg) {Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		wl.release();
		Disconnect();
	}



	private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
	{
		private boolean ConnectSuccess = true; //if it's here, it's almost connected

		@Override
		protected void onPreExecute()
		{
			progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
		}


		@Override
		protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
		{
			try
			{
				if (btSocket == null || !isBtConnected)
				{
					myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
					BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
					btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					btSocket.connect();//start connection
				}
			}
			catch (IOException e)
			{
				ConnectSuccess = false;//if the try failed, you can check the exception here
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
		{
			if (!ConnectSuccess)
			{
				showText("Connection Failed. Is it a SPP Bluetooth? Try again.");
				wl.release();
			}
			else
			{
				showText("Connected.");
				isBtConnected = true;
				wl.acquire();
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							while (!isInterrupted()) {
								Thread.sleep(10000);
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
										Intent batteryStatus = registerReceiver(null, ifilter);
										if (batteryStatus != null) {
											int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
											int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
											float batteryPct = level / (float) scale;
											showText("percent : "+ batteryPct);
											if (btSocket!=null)
											{
												showText("Send to BT : "+ batteryPct);
//												try
//												{
//													btSocket.getOutputStream().write(String.valueOf(batteryPct).getBytes());
//												}
//												catch (IOException e)
//												{
//													showText("Error");
//												}
											}
										}
									}
								});
							}
						} catch (InterruptedException e) {
						}
					}
				};
				t.start();
			}
			progress.dismiss();
		}
	}


	private void Disconnect()
	{
		if (btSocket!=null) //If the btSocket is busy
		{
			try
			{
				btSocket.close(); //close connection
			}
			catch (IOException e) {
				showText("Error Disconnection");}
		}
	}
}
