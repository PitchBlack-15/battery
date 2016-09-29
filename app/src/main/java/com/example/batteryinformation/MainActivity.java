package com.example.batteryinformation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class MainActivity extends Activity {
	//PowerConnectionReceiver pcr, pcr2;
	//TextView tv1, tv2;
	private TextView batteryInfo;
	Button btnPaired;
	ListView devicelist;
	private BluetoothAdapter myBluetooth = null;
	private Set pairedDevices;
    /** Called when the activity is first created. */



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//batteryInfo=(TextView)findViewById(R.id.textViewBatteryInfo);
		btnPaired = (Button)findViewById(R.id.button_scan);
		devicelist = (ListView)findViewById(R.id.listView);
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		myBluetooth = BluetoothAdapter.getDefaultAdapter();
		if(myBluetooth == null)
		{
			//Show a mensag. that thedevice has no bluetooth adapter
			Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
			//finish apk
			finish();
		}
		else
		{
			if (myBluetooth.isEnabled())
			{ }
			else
			{
				//Ask to the user turn the bluetooth on
				Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(turnBTon,1);
			}
		}


		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(5000);
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



	private void pairedDevicesList()
	{
		pairedDevices = myBluetooth.getBondedDevices();
		ArrayList list = new ArrayList();

		if (pairedDevices.size()>0)
		{
//			for(BluetoothDevice bt : pairedDevices)
//			{
//				list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
//			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
		}

		final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
		devicelist.setAdapter(adapter);
		//devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

	}

	private void showText(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {



		@Override
		public void onReceive(Context context, Intent intent) {

			int  health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
			int  icon_small= intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL,0);
			int  level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int  plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			boolean  present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT); 
			int  scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
			int  status= intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
			int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
			int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

//			batteryInfo.setText(
//					"Health: " + health + "\n" +
//							"Icon Small :" + icon_small + "\n" +
//							"Level: " + level + "\n" +
//							"Plugged: " + plugged + "\n" +
//							"Present: " + present + "\n" +
//							"Scale: " + scale + "\n" +
//							"Status: " + status + "\n" +
//							"Technology: " + technology + "\n" +
//							"Temperature: " + temperature + "\n" +
//							"Voltage: " + voltage + "\n");
//
		}
	};
}
