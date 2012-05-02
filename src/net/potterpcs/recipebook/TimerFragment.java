package net.potterpcs.recipebook;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class TimerFragment extends Fragment {
	// Tag for logging
	//	static final String TAG = "TimerFragment";

	Button startButton;
	Button stopButton;
	TextView display;
	NumberPicker minutePicker;
	NumberPicker secondPicker;
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
		minutePicker = (NumberPicker) getActivity().findViewById(R.id.minutepicker);
		secondPicker = (NumberPicker) getActivity().findViewById(R.id.secondpicker);
		timermin = (EditText) getActivity().findViewById(R.id.timerdisplayminutes);
		timersec = (EditText) getActivity(). findViewById(R.id.timerdisplayseconds);

		if (minutePicker != null) {
			minutePicker.setMinValue(0);
			minutePicker.setMaxValue(99);
			secondPicker.setMinValue(0);
			secondPicker.setMaxValue(59);
			secondPicker.setFormatter(new Formatter() {
				@Override
				public String format(int value) {
					return String.format("%02d", value);
				}
			});
			getActivity().findViewById(R.id.pickerlayout).setVisibility(View.VISIBLE);
			display.setVisibility(View.GONE);
		}

		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int seconds;
				if (minutePicker != null) {
					seconds = minutePicker.getValue() * 60 + secondPicker.getValue();
				} else {
					int m = Integer.parseInt(timermin.getEditableText().toString());
					int s = Integer.parseInt(timersec.getEditableText().toString());
					seconds = m * 60 + s;
					timermin.setText(timermin.getText(), BufferType.NORMAL);
					timersec.setText(timersec.getText(), BufferType.NORMAL);
				}
				startButton.setEnabled(false);
				stopButton.setEnabled(true);

				if (minutePicker == null) {
					timermin.setEnabled(false);
					timersec.setEnabled(false);
				} else {
					minutePicker.setEnabled(false);
					secondPicker.setEnabled(false);
					getActivity().findViewById(R.id.pickerlayout).setVisibility(View.GONE);
					display.setVisibility(View.VISIBLE);
				}

				if (display != null) {
					display.setText(DateUtils.formatElapsedTime(seconds));
				}

				timer = new CountDownTimer(seconds * 1000, 1000) {

					@Override
					public void onTick(long millisUntilFinished) {
						//						Log.i(TAG, "tick " + millisUntilFinished);
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
						// Play a sound and show a message when the timer ends
						MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.ding);
						mp.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.release();
							}
						});
						mp.setVolume(1.0f, 1.0f);
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

		if (minutePicker == null) {
			timermin.setEnabled(true);
			timersec.setEnabled(true);
		} else {
			minutePicker.setEnabled(true);
			secondPicker.setEnabled(true);
			getActivity().findViewById(R.id.pickerlayout).setVisibility(View.VISIBLE);
			display.setVisibility(View.GONE);
		}

		if (display != null) {
			display.setText(DateUtils.formatElapsedTime(0));
		} else {
			timermin.setText("00", BufferType.EDITABLE);
			timersec.setText("00", BufferType.EDITABLE);
		}
	}
}
