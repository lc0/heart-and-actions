//Copyright (c) Microsoft Corporation All rights reserved.  
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdk.sampleapp;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sdk.sampleapp.streaming.R;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class BandStreamingAppActivity extends Activity {

	private BandClient client = null;
	private Button btnStart;
    private Button btSendLabel;

    private boolean started = false;

	private TextView txtStatus;
    private EditText edtLabel;

    WebSocketClient mWebSocketClient;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtStatus.setText("");
				new appTask().execute();
			}
		});


        edtLabel = (EditText) findViewById(R.id.edtLabel);
        btSendLabel = (Button) findViewById(R.id.btnSendLabel);
        btSendLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                String action = "start";
                if (started) {
                    action = "stop";
                }
                started = !started;

                Log.d("Sending feedback: ", edtLabel.getText().toString() + " -- " + action);

                HashMap<String, String> data = new HashMap<String, String>();
                data.put("action", action);
                data.put("label", edtLabel.getText().toString());
                data.put("timestamp", edtLabel.getText().toString());
                AsyncLabelPost asyncHttpPost = new AsyncLabelPost(data);
                asyncHttpPost.execute("http://teststream.mybluemix.net/label");

            }
        });

        connectWebSocket();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		txtStatus.setText("");
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		if (client != null) {
			try {
				client.getSensorManager().unregisterAccelerometerEventListeners();
			} catch (BandIOException e) {
				appendToUI(e.getMessage());
			}
		}
	}
	
	private class appTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					appendToUI("Band is connected.\n");

                    SampleRate frequency =  SampleRate.MS128;

					client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, frequency);
					client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                    client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, frequency);
                    client.getSensorManager().registerPedometerEventListener(mPedometerEventListener);
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage();
					break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}
	
	private void appendToUI(final String string) {
		this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(string);
            }
        });
	}
	
    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                sendMessage(JsonUtil.toJson("acceleration", event));
//                appendToUI(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", event.getAccelerationX(),
//                        event.getAccelerationY(), event.getAccelerationZ()));
            }
        }
    };

	private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {

		@Override
		public void onBandHeartRateChanged(BandHeartRateEvent event) {
			if (event != null) {
                sendMessage(JsonUtil.toJson("heartrate", event));
                appendToUI(String.format(" Heart rate = %d", event.getHeartRate()));
			}

		}
	};

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {

        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent event) {
            if (event != null) {
                sendMessage(JsonUtil.toJson("skip-temperature", event));
            }
        }
    };

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {

        @Override
        public void onBandPedometerChanged(BandPedometerEvent event) {
            if (event != null) {
                sendMessage(JsonUtil.toJson("pedometer", event));
            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent event) {
            if (event != null) {
                sendMessage(JsonUtil.toJson("gyroscope", event));
            }
        }
    };
    
	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n");
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {

            if(client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                Log.d("connecting", "everything is good");
            } else {
                client.getSensorManager().requestHeartRateConsent(BandStreamingAppActivity.this, heartRateConsentListener);
            }

			return true;
		}
		
		appendToUI("Band is connecting...\n");
		return ConnectionState.CONNECTED == client.connect().await();
	}

    private HeartRateConsentListener heartRateConsentListener = new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            Log.d("connecting", "everything is good we have it now");
        }
    };

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://teststream.mybluemix.net/ws/receive");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        this.mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("message is coming", message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message) {
        mWebSocketClient.send(message);
    }
}

