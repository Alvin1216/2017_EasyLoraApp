package com.example.aaron.lora;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;



public class MainActivity extends Activity {

    private TextView value,status,connect_status;
    private ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initial_components();
        String url="tcp://broker.mqttdashboard.com:1883";



        MqttClient client=null;
        try {
            client = new MqttClient(url_test, MqttClient.generateClientId(),new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        //options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        try {
            client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        if(client.isConnected()) connect_status.setText("Connect to: "+url_test+"success!");
        else connect_status.setText("Connect to"+url+"failed!");

        client.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i("topic:",topic);
                //topic_s.setText("topic"+topic);
                byte[] result_src = message.getPayload();
                String result = new String(result_src);
                Log.i("message",result);

                JSONObject j = null;
                j = new JSONObject(result);
                String jdata = j.getString("data");
                Log.i("jdata",jdata);
                byte[] bytesresult = Base64.decode(jdata.getBytes(), Base64.DEFAULT);
                String light_value = new String(bytesresult);

                Message msg = new Message();
                msg.what =1;
                Bundle countBundle = new Bundle();
                countBundle.putString("light_value",light_value);
                msg.setData(countBundle);
                mHandler.sendMessage(msg);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
            }
            @Override
            public void connectionLost(Throwable arg0) {
                System.out.println("connectionLost");
                // TODO Auto-generated method stub
            }
        });
        //client.connect();
        try {
            client.subscribe("tester/hello", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void flash_light(String light_value){
        Log.i("light_value",light_value);
        //Camera cam = Camera.open();
        //Parameters p = cam.getParameters();
        if (Integer.valueOf(light_value)>50000) { // Lifht off
            Log.i("light_off"," ");
            status.setText("Lifht off");
            value.setText(light_value);
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null; // Usually front camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            try {
                camManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else { // light_on
            Log.i("light_on"," ");
            status.setText("Lifht on");
            value.setText(light_value);
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null; // Usually front camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            try {
                camManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }


        }


    }

    private void initial_components(){
        //TextView value,status;
        value=(TextView)findViewById(R.id.value);
        status=(TextView)findViewById(R.id.status);
        connect_status=(TextView)findViewById(R.id.connect_status);
        image=(ImageView)findViewById(R.id.image);
        image.setImageResource(R.drawable.night);
    }

    private Handler mHandler = new Handler() {
        //sub thread 的東西要更新的話要放到hander去更新
        //第二線程 拿來看手電筒要不要開
        //主線程 專門拿來收subscribe下來的東西
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String str_light = msg.getData().getString("light_value");
                flash_light(str_light);
            }
        }

    };
}
