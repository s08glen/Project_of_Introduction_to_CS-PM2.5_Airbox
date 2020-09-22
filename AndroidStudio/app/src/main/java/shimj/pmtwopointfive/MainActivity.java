package shimj.pmtwopointfive;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{
    private Button button_paired;
    private Button button_find;
    private TextView show_data;
    private ListView event_listView;
    private TextView DangerText;
    private ImageView image;

    private ArrayAdapter<String> deviceName;
    private ArrayAdapter<String> devicelD;
    private String choseID;
    private BluetoothDevice bleDevice;
    private InputStream mmInputStream;
    Thread workerThread;
    volatile boolean stopWorker;
    private int readBufferPosition;
    private byte[] readBuffer;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    public int PMvalue=0;
    public Resources res;
    public Bitmap a;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        res = getResources();
        a=BitmapFactory.decodeResource(res, R.drawable.a000);
        getView();
        setListener();
        deviceName = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        devicelD = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        DangerText.setText(" ");
        image.setImageResource(R.drawable.a000);
    }

    private void getView() {
        button_paired = (Button) findViewById(R.id.btn_paired);
        show_data = (TextView) findViewById(R.id.txtShow);
        event_listView = (ListView) findViewById(R.id.Show_B_List);
        button_find = (Button) findViewById(R.id.btn_conn);
        DangerText = (TextView) findViewById(R.id.DangerText);
        image = (ImageView) findViewById(R.id.pmimage);
    }

    private void setListener() {
        button_paired.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPBT();
            }
        });
        button_find.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                findPBT();
            }
        });
        event_listView.setAdapter(deviceName);
        event_listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> partent,View view, int position, long id){
                choseID = devicelD.getItem(position);
                try {
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "選擇了::" + choseID, Toast.LENGTH_SHORT).show();
                deviceName.clear();
            }
        });
    }

    private void findPBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {

        }
        else show_data.setText("No bluetooth adapter available");


        assert mBluetoothAdapter != null;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth,1);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String str = "已配對完成的裝置有 " + device.getName() + " " + device.getAddress() + "\n";
                //String
                String uid = device.getAddress();
                bleDevice = device;
                deviceName.add(str);
                devicelD.add(uid);
            }
            event_listView.setAdapter(deviceName);
        }
    }

    private void openBT() throws IOException {
        UUID uuid= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //00001101-0000-1000-8000-00805F9B34FB
        if (bleDevice != null) {
            BluetoothSocket bluesoccket = bleDevice.createRfcommSocketToServiceRecord(uuid);

            bluesoccket.connect();
            OutputStream mmOuputStream = bluesoccket.getOutputStream();
            mmInputStream = bluesoccket.getInputStream();

            beginListenForData();

            //show_data.setText("Bluetooth Opened: " + bleDevice.getName() + "" + bleDevice.getAddress());
            View bl = findViewById(R.id.btn_conn);
            View b2 = findViewById(R.id.btn_paired);
            View b3 = findViewById(R.id.Show_B_List);

            bl. setVisibility (View.INVISIBLE);
            b2. setVisibility (View.INVISIBLE);
            b3. setVisibility (View.INVISIBLE);

        }
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];


        workerThread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {


                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {

                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                readBufferPosition++;
                                if (b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes);
                                    //  PMvalue = 0;
                                    //for (int j = 0; j < encodedBytes.length - 1; j++) {
                                     //   PMvalue = PMvalue * 10 + encodedBytes[j] - 48;
                                    //}
                                    final int cco = Integer.parseInt(data.replaceAll("[\\D]",""));
                                    String tmp1 = String.valueOf(PMvalue);
                                    Log.d("value", tmp1);
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            long date = System.currentTimeMillis();
                                            TextView tvDisplayDate = (TextView) findViewById(R.id.DATE);
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd a h:mm:ss");
                                            String dateString = sdf.format(date);
                                            tvDisplayDate.setText("Update: " + dateString);
                                            if (cco < 36) {

                                                if (cco > 23)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a03);
                                                else if (cco > 11)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a02);
                                                else if (cco > 0)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a01);
                                                image.setImageBitmap(a);
                                                DangerText.setText("良好 :-)");
                                                show_data.setText(data);
                                            }
                                            else if (cco < 54) {
                                                if (cco > 47)
                                                    a = BitmapFactory.decodeResource(res,R.drawable.a06);
                                                else if (cco > 41)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a05);
                                                else if (cco > 35)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a04);
                                                image.setImageBitmap(a);
                                                DangerText.setText("警戒 :-(");
                                                show_data.setText(data);
                                            }
                                            else if (cco < 71) {
                                                if (cco > 64)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a09);
                                                else if (cco > 58)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a08);
                                                else if (cco > 53)
                                                    a = BitmapFactory.decodeResource(res, R.drawable.a07);
                                                image.setImageBitmap(a);
                                                DangerText.setText("過量QAQ");
                                                show_data.setText(data);
                                            } else if (cco > 70) {
                                                a = BitmapFactory.decodeResource(res, R.drawable.a10);
                                                image.setImageBitmap(a);
                                                DangerText.setText("危險!!!!");
                                                show_data.setText(data);
                                            }
                                        }


                                    });
                                } else //若沒有換行，一直存進來
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }

        });
        workerThread.start();
    }

}
