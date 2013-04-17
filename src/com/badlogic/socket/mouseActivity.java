package com.badlogic.socket;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

import com.WazaBe.HoloEverywhere.widget.Button;
import com.WazaBe.HoloEverywhere.widget.TextView;
import com.WazaBe.HoloEverywhere.widget.Toast;
import com.badlogic.R;

public class mouseActivity extends Activity implements OnTouchListener, OnGestureListener, OnDoubleTapListener {
    private GestureDetector mGestureDetector;

    final int MOUSEEVENTF_CANCEL = 0x0001; /* mouse move */
    final int MOUSEEVENTF_MOVE = 0x0001; /* mouse move */

    final int MOUSEEVENTF_LEFTDOWN = 0x0002; /* left button down */
    final int MOUSEEVENTF_LEFTUP = 0x0003; /* left button up */
    final int MOUSEEVENTF_RIGHTDOWN = 0x0004; /* right button down */
    final int MOUSEEVENTF_RIGHTUP = 0x0005; /* right button up */

    final int MOUSEEVENTF_TAP = 0x0006; /* ���� */
    final int MOUSEEVENTF_DOUBLETAP = 0x0007; /* ˫�� */

    final int MOUSEEVENTF_ROLLUP = 0x0008; /* �����϶����� */
    final int MOUSEEVENTF_ROLLDOWN = 0x0009; /* �����϶����� */

    private TextView touchInof;
    private Button mouseRightButton, mouseLeftButton, rollUpButton, rollDownButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mouse_layout);

        touchInof = (TextView) findViewById(R.id.touchInfo);

        mouseRightButton = (Button) findViewById(R.id.rightButton);
        mouseRightButton.setOnClickListener(buttonClickListener);
        mouseLeftButton = (Button) findViewById(R.id.leftButton);
        mouseLeftButton.setOnClickListener(buttonClickListener);

        rollUpButton = (Button) findViewById(R.id.rollerButtonUp);
        rollUpButton.setOnClickListener(buttonClickListener);
        rollUpButton.setOnLongClickListener(buttonlongClickListener);
        rollUpButton.setLongClickable(true);

        rollDownButton = (Button) findViewById(R.id.rollerButtonDown);
        rollDownButton.setOnClickListener(buttonClickListener);
        rollDownButton.setLongClickable(true);
        rollDownButton.setOnLongClickListener(buttonlongClickListener);

        mGestureDetector = new GestureDetector((OnGestureListener) this);
        RelativeLayout viewSnsLayout = (RelativeLayout) findViewById(R.id.touchLayout);
        viewSnsLayout.setOnTouchListener(this);
        viewSnsLayout.setLongClickable(true);
    }

    private OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(android.view.View arg0) {
            // TODO Auto-generated method stub
            switch (arg0.getId()) {
            case R.id.rightButton:// �һ�
                sendMessage(MOUSEEVENTF_RIGHTUP + "");
                break;
            case R.id.leftButton:// ����
                sendMessage(MOUSEEVENTF_TAP + "");
                break;

            case R.id.rollerButtonUp:// �����϶�����
                if (clickButton != 0) {
                    clickButton = 0;
                    mThread.interrupt();
                    sendMessage("long click up");
                } else
                    sendMessage(MOUSEEVENTF_ROLLUP + "");
                break;

            case R.id.rollerButtonDown:// �����϶�����
                if (clickButton != 0) {
                    clickButton = 0;
                    mThread.interrupt();
                    sendMessage("long click dwon");
                } else
                    sendMessage(MOUSEEVENTF_ROLLDOWN + "");
                break;
            }
        }
    };
    private Thread mThread = null;
    private int clickButton = 0;
    private OnLongClickListener buttonlongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(android.view.View v) {
            // TODO Auto-generated method stub
            clickButton = v.getId();
            mThread = new Thread(mLongClickRunnable);
            mThread.start();
            return false;
        }
    };

    private Runnable mLongClickRunnable = new Runnable() {
        public void run() {
            while (clickButton != 0) {
                if (clickButton == R.id.rollerButtonDown)
                    sendMessage(MOUSEEVENTF_ROLLDOWN + "");
                else if (clickButton == R.id.rollerButtonUp)
                    sendMessage(MOUSEEVENTF_ROLLUP + "");
            }
        }
    };

    private void sendMessage(String msgText) {
        if (ControlPCActivity.mPrintWriterClient != null) {
            try {
                ControlPCActivity.mPrintWriterClient.print(msgText);// ���͸������
                ControlPCActivity.mPrintWriterClient.flush();
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(this, "�����쳣��" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (ControlPCActivity.mPrintWriterServer != null) {
            try {
                ControlPCActivity.mPrintWriterServer.print(msgText);// ���͸������
                ControlPCActivity.mPrintWriterServer.flush();
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(this, "�����쳣��" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLongPress = false;
    private float touchMoveX = 0, touchMoveY = 0;

    @Override
    public boolean onTouch(android.view.View v, MotionEvent event) {
        // TODO Auto-generated method stub
        // return false;

        if (isLongPress) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (touchMoveX == 0) {
                    touchMoveX = event.getX();
                    touchMoveY = event.getY();
                } else {
                    sendMessage(MOUSEEVENTF_MOVE + ":" + (event.getX() - touchMoveX) + ";" + (event.getY() - touchMoveY));// �����

                    touchInof.setText("onTouch move: " + (event.getX() - touchMoveX) + " : " + (event.getY() - touchMoveY));
                    touchMoveX = event.getX();
                    touchMoveY = event.getY();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                touchInof.setText("onTouch up: " + event.getX() + " : " + event.getY());
                isLongPress = false;
                sendMessage(MOUSEEVENTF_LEFTUP + "");// �����
            } else {
                touchMoveX = 0;
                isLongPress = false;
                sendMessage(MOUSEEVENTF_CANCEL + "");// �����
            }
            return false;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        // TODO Auto-generated method stub
        touchInof.setText("onDown: " + event.getX() + " : " + event.getY());
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        // �û����´������������ƶ����ɿ�,���ʱ�������ָ�˶����м��ٶȵġ�
        // ��1��MotionEvent ACTION_DOWN,
        // ���ACTION_MOVE, 1��ACTION_UP����
        // e1����1��ACTION_DOWN MotionEvent
        // e2�����һ��ACTION_MOVE MotionEvent
        // velocityX��X���ϵ��ƶ��ٶȣ�����/��
        // velocityY��Y���ϵ��ƶ��ٶȣ�����/��
        touchInof.setText("onFling:" + "\n���������:" + event1.getX() + " : " + event1.getY() + "\n�������յ�:" + event2.getX() + " : "
                + event2.getY() + "\nˮƽ������ٶ�:" + velocityX + "\n��ֱ������ٶ�:" + velocityY);

        sendMessage(MOUSEEVENTF_MOVE + ":" + velocityX + ";" + velocityY);// �����

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onLongPress: \n" + e.getX() + " : " + e.getY());
    }

    // ����ʱ������e1Ϊdownʱ��MotionEvent��e2Ϊmoveʱ��MotionEvent
    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub

        touchInof.setText("onScroll:" + "\n���������:" + event1.getX() + " : " + event1.getY() + "\n�������յ�:" + event2.getX() + " : "
                + event2.getY() + "\nˮƽ����ľ���:" + distanceX + "\n��ֱ����ľ���:" + distanceY);

        sendMessage(MOUSEEVENTF_MOVE + ":" + distanceX + ";" + distanceY);// �����

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onShowPress: \n" + e.getX() + " : " + e.getY());
        isLongPress = true;
        sendMessage(MOUSEEVENTF_LEFTDOWN + "");// �����
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onSingleTapUp: \n" + e.getX() + " : " + e.getY());
        return false;
    }

    // �ڶ��ε���downʱ������eΪ��һ��downʱ��MotionEvent
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onDoubleTap: " + e.getX() + " : " + e.getY());

        sendMessage(MOUSEEVENTF_DOUBLETAP + "");
        return false;
    }

    // �ڶ��ε���down,move��upʱ��������eΪ��ͬʱ���µ�MotionEvent
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onDoubleTapEvent: " + e.getX() + " : " + e.getY());
        return false;
    }

    // ���һ�ε�������ȷ��û�ж����¼��󴥷���300ms����eΪdownʱ��MotionEvent
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onSingleTapConfirmed: " + e.getX() + " : " + e.getY());

        sendMessage(MOUSEEVENTF_TAP + "");
        return false;
    }

}
