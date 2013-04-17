package badlogic.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View.OnClickListener;

import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.widget.Button;
import com.WazaBe.HoloEverywhere.widget.EditText;
import com.WazaBe.HoloEverywhere.widget.TextView;
import com.WazaBe.HoloEverywhere.widget.Toast;
import com.WazaBe.HoloEverywhere.widget.View;

public class ControlPCActivity extends Activity {

    private Button startButton;
    private EditText IPText;
    private Button sendButtonClient;
    private Button sendButtonServer;
    private Button CreateButton;
    private EditText editMsgText, editMsgTextCilent;
    private TextView recvText;
    private Button StartMouseButton;

    private Context mContext;
    private boolean isConnecting = false;

    private Thread mThreadClient = null;
    private Thread mThreadServer = null;
    private Socket mSocketServer = null;
    private Socket mSocketClient = null;
    static BufferedReader mBufferedReaderServer = null;
    static PrintWriter mPrintWriterServer = null;
    static BufferedReader mBufferedReaderClient = null;
    static PrintWriter mPrintWriterClient = null;
    private String recvMessageClient = "";
    private String recvMessageServer = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

        IPText = (EditText) findViewById(R.id.IPText);
        // IPText.setText("10.0.2.15:");
        IPText.setText("192.168.12.131:59671");
        startButton = (Button) findViewById(R.id.StartConnect);
        startButton.setOnClickListener(StartClickListener);

        editMsgTextCilent = (EditText) findViewById(R.id.clientMessageText);
        editMsgTextCilent.setText("up");

        editMsgText = (EditText) findViewById(R.id.MessageText);
        editMsgText.setText("up");

        sendButtonClient = (Button) findViewById(R.id.SendButtonClient);
        sendButtonClient.setOnClickListener(SendClickListenerClient);
        sendButtonServer = (Button) findViewById(R.id.SendButtonServer);
        sendButtonServer.setOnClickListener(SendClickListenerServer);

        CreateButton = (Button) findViewById(R.id.CreateConnect);
        CreateButton.setOnClickListener(CreateClickListener);

        recvText = (TextView) findViewById(R.id.RecvText);
        recvText.setMovementMethod(ScrollingMovementMethod.getInstance());

        StartMouseButton = (Button) findViewById(R.id.StartMouse);
        StartMouseButton.setOnClickListener(StartMouseClickListenerServer);
    }

    private OnClickListener StartClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (isConnecting) {
                isConnecting = false;
                try {
                    if (mSocketClient != null) {
                        mSocketClient.close();
                        mSocketClient = null;

                        mPrintWriterClient.close();
                        mPrintWriterClient = null;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mThreadClient.interrupt();

                startButton.setText("��ʼ����");
                IPText.setEnabled(true);
                recvText.setText("��Ϣ:\n");
            } else {
                isConnecting = true;
                startButton.setText("ֹͣ����");
                IPText.setEnabled(false);

                mThreadClient = new Thread(mRunnable);
                mThreadClient.start();
            }
        }
    };

    private OnClickListener SendClickListenerClient = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (isConnecting && mSocketClient != null) {
                String msgText = editMsgTextCilent.getText().toString();// ȡ�ñ༭�����������������
                if (msgText.length() <= 0) {
                    Toast.makeText(mContext, "�������ݲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        mPrintWriterClient.print(msgText);// ���͸������
                        mPrintWriterClient.flush();
                    } catch (Exception e) {
                        // TODO: handle exception
                        Toast.makeText(mContext, "�����쳣��" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(mContext, "û������", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private OnClickListener SendClickListenerServer = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (serverRuning && mSocketServer != null) {
                String msgText = editMsgText.getText().toString();// ȡ�ñ༭�����������������
                if (msgText.length() <= 0) {
                    Toast.makeText(mContext, "�������ݲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        mPrintWriterServer.print(msgText);// ���͸������
                        mPrintWriterServer.flush();
                    } catch (Exception e) {
                        // TODO: handle exception
                        Toast.makeText(mContext, "�����쳣��" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(mContext, "û������", Toast.LENGTH_SHORT).show();
            }
        }
    };
    // �߳�:�����������������Ϣ
    private Runnable mRunnable = new Runnable() {
        public void run() {
            String msgText = IPText.getText().toString();
            if (msgText.length() <= 0) {
                // Toast.makeText(mContext, "IP����Ϊ�գ�",
                // Toast.LENGTH_SHORT).show();
                recvMessageClient = "IP����Ϊ�գ�\n";// ��Ϣ����
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }
            int start = msgText.indexOf(":");
            if ((start == -1) || (start + 1 >= msgText.length())) {
                recvMessageClient = "IP��ַ���Ϸ�\n";// ��Ϣ����
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }
            String sIP = msgText.substring(0, start);
            String sPort = msgText.substring(start + 1);
            int port = Integer.parseInt(sPort);

            Log.d("gjz", "IP:" + sIP + ":" + port);

            try {
                // ���ӷ�����
                mSocketClient = new Socket(sIP, port); // portnum
                // ȡ�����롢�����
                mBufferedReaderClient = new BufferedReader(new InputStreamReader(mSocketClient.getInputStream()));

                mPrintWriterClient = new PrintWriter(mSocketClient.getOutputStream(), true);

                recvMessageClient = "�Ѿ�����server!\n";// ��Ϣ����
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                // break;
            } catch (Exception e) {
                recvMessageClient = "����IP�쳣:" + e.toString() + e.getMessage() + "\n";// ��Ϣ����
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }

            char[] buffer = new char[256];
            int count = 0;
            while (isConnecting) {
                try {
                    // if ( (recvMessageClient =
                    // mBufferedReaderClient.readLine()) != null )
                    if ((count = mBufferedReaderClient.read(buffer)) > 0) {
                        recvMessageClient = getInfoBuff(buffer, count) + "\n";// ��Ϣ����
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    recvMessageClient = "�����쳣:" + e.getMessage() + "\n";// ��Ϣ����
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                recvText.append("Server: " + recvMessageServer); // ˢ��
            } else if (msg.what == 1) {
                recvText.append("Client: " + recvMessageClient); // ˢ��

            }
        }
    };
    // ���������ServerSocket����
    private ServerSocket serverSocket = null;
    private boolean serverRuning = false;
    private OnClickListener CreateClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (serverRuning) {
                serverRuning = false;

                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                    if (mSocketServer != null) {
                        mSocketServer.close();
                        mSocketServer = null;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mThreadServer.interrupt();
                CreateButton.setText("��������");
                recvText.setText("��Ϣ:\n");
            } else {
                serverRuning = true;
                mThreadServer = new Thread(mcreateRunnable);
                mThreadServer.start();
                CreateButton.setText("ֹͣ����");
            }
        }
    };
    // �߳�:�����������������Ϣ
    private Runnable mcreateRunnable = new Runnable() {
        public void run() {
            try {
                serverSocket = new ServerSocket(0);

                SocketAddress address = null;
                if (!serverSocket.isBound()) {
                    serverSocket.bind(address, 0);
                }

                getLocalIpAddress();

                // �������ڵȴ�ͷ�����
                mSocketServer = serverSocket.accept();

                // ���ܿͷ������BufferedReader����
                mBufferedReaderServer = new BufferedReader(new InputStreamReader(mSocketServer.getInputStream()));
                // ��ͷ��˷������
                mPrintWriterServer = new PrintWriter(mSocketServer.getOutputStream(), true);
                // mPrintWriter.println("������Ѿ��յ���ݣ�");

                Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "client�Ѿ������ϣ�\n";
                mHandler.sendMessage(msg);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "�����쳣:" + e.getMessage() + e.toString() + "\n";// ��Ϣ����
                mHandler.sendMessage(msg);
                return;
            }
            char[] buffer = new char[256];
            int count = 0;
            while (serverRuning) {
                try {
                    // if( (recvMessageServer =
                    // mBufferedReaderServer.readLine()) != null )//��ȡ�ͷ������
                    if ((count = mBufferedReaderServer.read(buffer)) > 0)
                        ;
                    {
                        recvMessageServer = getInfoBuff(buffer, count) + "\n";// ��Ϣ����
                        Message msg = new Message();
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    recvMessageServer = "�����쳣:" + e.getMessage() + "\n";// ��Ϣ����
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    return;
                }
            }
        }
    };

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    // if (!inetAddress.isLoopbackAddress())
                    {
                        // return inetAddress.getHostAddress();
                        // recvMessage = inetAddress.getHostAddress()+
                        // "isAnyLocalAddress: "+inetAddress.isAnyLocalAddress()
                        // +
                        // "isLinkLocalAddress: "+inetAddress.isLinkLocalAddress()
                        // +
                        // "isSiteLocalAddress: "+inetAddress.isSiteLocalAddress()+"\n";
                        // mHandler.sendMessage(mHandler.obtainMessage());
                        // if(inetAddress.isSiteLocalAddress())
                        {
                            recvMessageServer += "������IP��" + inetAddress.getHostAddress() + ":" + serverSocket.getLocalPort() + "\n";
                            // Message msg = new Message();
                            // msg.what = 0;
                            // mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            recvMessageServer = "��ȡIP��ַ�쳣:" + ex.getMessage() + "\n";// ��Ϣ����
            Message msg = new Message();
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
        Message msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
        return null;
    }

    private String getInfoBuff(char[] buff, int count) {
        char[] temp = new char[count];
        for (int i = 0; i < count; i++) {
            temp[i] = buff[i];
        }
        return new String(temp);
    }

    private OnClickListener StartMouseClickListenerServer = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if ((serverRuning && mSocketServer != null) || (isConnecting && mSocketClient != null)) {
                Intent intent = new Intent();
                intent.setClass(ControlPCActivity.this, mouseActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "û������", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        if (isConnecting) {
            isConnecting = false;
            try {
                if (mSocketClient != null) {
                    mSocketClient.close();
                    mSocketClient = null;

                    mPrintWriterClient.close();
                    mPrintWriterClient = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mThreadClient.interrupt();
        }
        if (serverRuning) {
            serverRuning = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }
                if (mSocketServer != null) {
                    mSocketServer.close();
                    mSocketServer = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mThreadServer.interrupt();
        }
    }
}