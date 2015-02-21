package com.example.tagreader;

import java.util.Arrays;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getName();

	private TextView mTextView;
	private Button mBtnExit;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mIntentFilters;
	private String[][] mNFCTechLists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = (TextView) findViewById(R.id.tvMain);
		mBtnExit = (Button) findViewById(R.id.btnExit);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


		if (mNfcAdapter != null) {
			if (!mNfcAdapter.isEnabled()) {
				Toast.makeText(this, "NFC is disabled. Please enable NFC.", Toast.LENGTH_LONG).show();
			} else {
				mTextView.setText("Scan an NFC tag!!");
			}
		} else {
			Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
		}

		mPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		Log.d(TAG, "A tag was scanned.");
		vibrate();

		try {
			ndefIntent.addDataType("text/plain");
			mIntentFilters = new IntentFilter[] { ndefIntent };
		} catch (Exception e) {
			Log.e(TAG, "exception", e);
		}

		mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };

		mBtnExit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	public void onNewIntent(Intent intent) {

		Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		if (data != null) {
			Log.d(TAG, "Found " + data.length + " NDEF messages");

			String readTag = "";
			try {
				for (int i = 0; i < data.length; i++) {
					NdefRecord [] recs = ((NdefMessage)data[i]).getRecords();
					for (int j = 0; j < recs.length; j++) {
						if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
								Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {

							byte[] payload = recs[j].getPayload();
							String payloadStr = new String(payload, "UTF-8");
							readTag += payloadStr;
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "exception", e);
			}

			try {
				// Message structure: ID;raw_text_content (splitted by ";")
				String contentId = readTag.split(";", 2)[0];
				String contentText = readTag.split(";", 2)[1];

				try {
					Intent nextScreen = new Intent(getApplicationContext(), SecondActivity.class);
					nextScreen.putExtra("contentId", contentId);
					nextScreen.putExtra("contentText", contentText);

					startActivity(nextScreen);
				} catch (Exception e) {
					Log.e(TAG, "exception", e);
				}
			} catch (Exception e) {
				Log.e(TAG, "exception", e);
				Toast.makeText(this, "Unable to parse tag content", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mNfcAdapter != null){
			mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mNfcAdapter != null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}

	private void vibrate() {
		Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE) ;
		vibe.vibrate(500);
	}

