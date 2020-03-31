package tw.org.iii.brad.brad12;
//參考manifest,要能存取網路 & 網路狀態

import androidx.appcompat.app.AppCompatActivity;

import android.net.InetAddresses;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.input);

        getMyIPV2();
        new Thread(){
            @Override
            public void run() {
                receiveUDP();
            }
        }.start();
    }

    private void getMyIP() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String myip = InetAddress.getLocalHost().getHostAddress();
                    Log.v("brad", "myip = " + myip);
                } catch (Exception e) {
                    Log.v("brad", e.toString());
                }

            }
        }.start();
    }

    private void getMyIPV2() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();//NetworkInterface為介面卡的概念,取得多張介面卡,回傳列舉物件
            while (en.hasMoreElements()) {    //多張介面卡,若有下一張
                NetworkInterface networkInterface = en.nextElement(); //則下一個元素即為介面卡,此處取得單張介面卡
                Enumeration<InetAddress> ips = networkInterface.getInetAddresses();//一張介面卡可以有多個ip
                while (ips.hasMoreElements()) {   //多個ip,若有下一個
                    InetAddress ip = ips.nextElement(); //則下一個ip就是ip,此處取得ip
                    Log.v("brad", "ip = " + ip);
                }
            }


        } catch (Exception e) {
            Log.v("brad", e.toString());
        }
    }

    public void sendUDP(View view) {
        new Thread() {
            @Override
            public void run() {
                byte[] data = input.getText().toString().getBytes();
                try {
                    DatagramSocket socket = new DatagramSocket();   //發送端不用講port號,去哪裡則寫在包裹上
                    DatagramPacket packet = new DatagramPacket(     //封包
                            data, data.length,
                            InetAddress.getByName("192.168.200.2"), 8888);//發送去哪,及port號
                    socket.send(packet);
                    socket.close();
                    Log.v("brad", "send OK");
                } catch (Exception e) {
                    Log.v("brad", e.toString());
                }
            }
        }.start();

    }

    private void receiveUDP() { //在Eclipse寫好的接收端code,直接A過來
        while (true) {
            byte[] buf = new byte[4096];
            try {
                DatagramSocket socket = new DatagramSocket(8888);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                socket.close();

                String who = packet.getAddress().getHostAddress();
                byte[] data = packet.getData();
                int len = packet.getLength();
                String mesg = new String(data, 0, len);
                Log.v("brad", who + ":" + mesg);
                if(mesg.equals("quit")){
                    break;
                }
            } catch (Exception e) {
                Log.v("brad",e.toString());
            }
        }
    }
}
