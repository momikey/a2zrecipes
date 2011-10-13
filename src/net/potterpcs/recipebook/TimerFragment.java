package net.potterpcs.recipebook;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class TimerFragment extends Fragment {
	static final String TAG = "TimerFragment";
	
	Button startButton;
	Button stopButton;
	TextView display;
	NumberPicker picker;
	CountDownTimer timer;
	EditText timermin;
	EditText timersec;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timerlayout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		startButton = (Button) getActivity().findViewById(R.id.timerstartbutton);
		stopButton = (Button) getActivity().findViewById(R.id.timerstopbutton);
		display = (TextView) getActivity().findViewById(R.id.timerdisplay);
		picker = (NumberPicker) getActivity().findViewById(R.id.numberpicker);
		timermin = (EditText) getActivity().findViewById(R.id.timerdisplayminutes);
		timersec = (EditText) getActivity(). findViewById(R.id.timerdisplayseconds);
		
		if (picker != null) {
			picker.setMinValue(0);
			picker.setMaxValue(99);
		}
		
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int seconds;
				if (picker != null) {
					seconds = picker.getValue() * 60;
				} else {
					int m = Integer.parseInt(timermin.getEditableText().toString());
					int s = Integer.parseInt(timersec.getEditableText().toString());
					seconds = m * 60 + s;
					timermin.setText(timermin.getText(), BufferType.NORMAL);
					timersec.setText(timersec.getText(), BufferType.NORMAL);
				}
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				timermin.setEnabled(false);
				timersec.setEnabled(false);
				if (display != null) {
					display.setText(DateUtils.formatElapsedTime(seconds));
				}
				
				timer = new CountDownTimer(seconds * 1000, 1000) {
					
					@Override
					public void onTick(long millisUntilFinished) {
						Log.i(TAG, "tick " + millisUntilFinished);
						if (display != null) {
							display.setText(DateUtils.formatElapsedTime(millisUntilFinished / 1000));
						} else {
							int totalsec = (int) (millisUntilFinished / 1000);
							int s = totalsec % 60;
							int m = totalsec / 60;
							timermin.setText(String.format("%02d", m));
							timersec.setText(String.format("%02d", s));
						}
					}
					
					@Override
					public void onFinish() {
						MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.ding);
						mp.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.release();
							}
						});
						mp.setVolume(10.0f, 10.0f);
						Log.i(TAG, "timer done");

						Toast.makeText(getActivity(), "Done!", Toast.LENGTH_LONG).show();
						mp.start();
						clearToZero();
					}
				};
				
				timer.start();
			}
		});
		
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				timer.cancel();
				clearToZero();
			}
		});
	}

	void clearToZero() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		timermin.setEnabled(true);
		timersec.setEnabled(true);
		if (display != null) {
		display.setText(DateUtils.formatElapsedTime(0));
		} else {
			timermin.setText("00", BufferType.EDITABLE);
			timersec.setText("00", BufferType.EDITABLE);
		}
	}
}
