package com.example.tagreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SecondActivity extends Activity implements OnClickListener, OnTouchListener, OnCompletionListener {

	private static final String TAG = SecondActivity.class.getName();

	private ImageButton mBtnPlayPause;
	private SeekBar mSeekBar;

	private TextView mTextView;
	private Button mBtnClose;

	private MediaPlayer mMediaPlayer;
	private int mLength;

	private final Handler handler = new Handler();

	private String mId;
	private String mText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second_activity);

		mTextView = (TextView) findViewById(R.id.tvDescription);
		mTextView.setMovementMethod(new ScrollingMovementMethod());
		mBtnClose = (Button) findViewById(R.id.btnClose);

		if (savedInstanceState == null) {
				mId = getIntent().getExtras().getString("contentId");
				mText = getIntent().getExtras().getString("contentText");
		} else {
			mId = (String) savedInstanceState.getSerializable("contentId");
			mText = (String) savedInstanceState.getSerializable("contentText");
		}

		mTextView.setText(mText);


		mBtnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
		mBtnPlayPause.setOnClickListener(this);

		mSeekBar = (SeekBar) findViewById(R.id.sbPlay);
		mSeekBar.setMax(99);
		mSeekBar.setOnTouchListener(this);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(this);

		mBtnClose.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void seekBarUpdate() {
		mSeekBar.setProgress((int) (((float) mMediaPlayer.getCurrentPosition() / mLength) * 100));

		if (mMediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					seekBarUpdate();
				}
			};

		handler.postDelayed(notification, 1000);
		}
	}

	public void onClick(View view) {
		if (view.getId() == R.id.btnPlayPause) {
			try {
				mMediaPlayer.setDataSource(mId);
				mMediaPlayer.prepare();
			} catch (Exception e) {
				if(isNetworkAvailable() != true){
					Toast.makeText(this, "Unable to play requested track. Check your Internet connection.", Toast.LENGTH_LONG).show();
				}
				Log.e(TAG, "exception", e);
			}

			mLength = mMediaPlayer.getDuration();

			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mBtnPlayPause.setImageResource(R.drawable.button_pause);
			} else {
				mMediaPlayer.pause();
				mBtnPlayPause.setImageResource(R.drawable.button_play);
			}

			seekBarUpdate();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view.getId() == R.id.sbPlay) {
			if (mMediaPlayer.isPlaying()) {
		    	SeekBar sb = (SeekBar)view;
				int playPosition = (mLength / 100) * sb.getProgress();
				mMediaPlayer.seekTo(playPosition);
			}
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mMediaPlayer.seekTo(0);
		mBtnPlayPause.setImageResource(R.drawable.button_play);
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
			 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

