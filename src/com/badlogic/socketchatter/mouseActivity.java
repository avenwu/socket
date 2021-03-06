package com.badlogic.socketchatter;

import com.badlogic.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class mouseActivity extends Activity implements OnTouchListener, OnGestureListener, OnDoubleTapListener {
    private GestureDetector mGestureDetector;

    final int MOUSEEVENTF_CANCEL = 0x0001; /* mouse move */
    final int MOUSEEVENTF_MOVE = 0x0001; /* mouse move */

    final int MOUSEEVENTF_LEFTDOWN = 0x0002; /* left button down */
    final int MOUSEEVENTF_LEFTUP = 0x0003; /* left button up */
    final int MOUSEEVENTF_RIGHTDOWN = 0x0004; /* right button down */
    final int MOUSEEVENTF_RIGHTUP = 0x0005; /* right button up */

    final int MOUSEEVENTF_TAP = 0x0006; /* 锟斤拷锟斤拷 */
    final int MOUSEEVENTF_DOUBLETAP = 0x0007; /* 双锟斤拷 */

    final int MOUSEEVENTF_ROLLUP = 0x0008; /* 锟斤拷锟斤拷锟较讹拷锟斤拷锟斤拷 */
    final int MOUSEEVENTF_ROLLDOWN = 0x0009; /* 锟斤拷锟斤拷锟较讹拷锟斤拷锟斤拷 */

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
            case R.id.rightButton:// 锟揭伙拷
                sendMessage(MOUSEEVENTF_RIGHTUP + "");
                break;
            case R.id.leftButton:// 锟斤拷锟斤拷
                sendMessage(MOUSEEVENTF_TAP + "");
                break;

            case R.id.rollerButtonUp:// 锟斤拷锟斤拷锟较讹拷锟斤拷锟斤拷
                if (clickButton != 0) {
                    clickButton = 0;
                    mThread.interrupt();
                    sendMessage("long click up");
                } else
                    sendMessage(MOUSEEVENTF_ROLLUP + "");
                break;

            case R.id.rollerButtonDown:// 锟斤拷锟斤拷锟较讹拷锟斤拷锟斤拷
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
                ControlPCActivity.mPrintWriterClient.print(msgText);// 锟斤拷锟酵革拷锟斤拷锟斤拷锟�
                ControlPCActivity.mPrintWriterClient.flush();
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(this, "锟斤拷锟斤拷锟届常锟斤拷" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (ControlPCActivity.mPrintWriterServer != null) {
            try {
                ControlPCActivity.mPrintWriterServer.print(msgText);// 锟斤拷锟酵革拷锟斤拷锟斤拷锟�
                ControlPCActivity.mPrintWriterServer.flush();
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(this, "锟斤拷锟斤拷锟届常锟斤拷" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    sendMessage(MOUSEEVENTF_MOVE + ":" + (event.getX() - touchMoveX) + ";" + (event.getY() - touchMoveY));// 锟斤拷锟斤拷锟�

                    touchInof.setText("onTouch move: " + (event.getX() - touchMoveX) + " : " + (event.getY() - touchMoveY));
                    touchMoveX = event.getX();
                    touchMoveY = event.getY();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                touchInof.setText("onTouch up: " + event.getX() + " : " + event.getY());
                isLongPress = false;
                sendMessage(MOUSEEVENTF_LEFTUP + "");// 锟斤拷锟斤拷锟�
            } else {
                touchMoveX = 0;
                isLongPress = false;
                sendMessage(MOUSEEVENTF_CANCEL + "");// 锟斤拷锟斤拷锟�
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
        // 锟矫伙拷锟斤拷锟铰达拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟狡讹拷锟斤拷锟缴匡拷,锟斤拷锟绞憋拷锟斤拷锟斤拷锟斤拷指锟剿讹拷锟斤拷锟叫硷拷锟劫度的★拷
        // 锟斤拷1锟斤拷MotionEvent ACTION_DOWN,
        // 锟斤拷锟紸CTION_MOVE, 1锟斤拷ACTION_UP锟斤拷锟斤拷
        // e1锟斤拷锟斤拷1锟斤拷ACTION_DOWN MotionEvent
        // e2锟斤拷锟斤拷锟揭伙拷锟紸CTION_MOVE MotionEvent
        // velocityX锟斤拷X锟斤拷锟较碉拷锟狡讹拷锟劫度ｏ拷锟斤拷锟斤拷/锟斤拷
        // velocityY锟斤拷Y锟斤拷锟较碉拷锟狡讹拷锟劫度ｏ拷锟斤拷锟斤拷/锟斤拷
        touchInof.setText("onFling:" + "\n锟斤拷锟斤拷锟斤拷锟斤拷锟�" + event1.getX() + " : " + event1.getY() + "\n锟斤拷锟斤拷锟斤拷锟秸碉拷:" + event2.getX() + " : "
                + event2.getY() + "\n水平锟斤拷锟斤拷锟斤拷俣锟�" + velocityX + "\n锟斤拷直锟斤拷锟斤拷锟斤拷俣锟�" + velocityY);

        sendMessage(MOUSEEVENTF_MOVE + ":" + velocityX + ";" + velocityY);// 锟斤拷锟斤拷锟�

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onLongPress: \n" + e.getX() + " : " + e.getY());
    }

    // 锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷e1为down时锟斤拷MotionEvent锟斤拷e2为move时锟斤拷MotionEvent
    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub

        touchInof.setText("onScroll:" + "\n锟斤拷锟斤拷锟斤拷锟斤拷锟�" + event1.getX() + " : " + event1.getY() + "\n锟斤拷锟斤拷锟斤拷锟秸碉拷:" + event2.getX() + " : "
                + event2.getY() + "\n水平锟斤拷锟斤拷木锟斤拷锟�" + distanceX + "\n锟斤拷直锟斤拷锟斤拷木锟斤拷锟�" + distanceY);

        sendMessage(MOUSEEVENTF_MOVE + ":" + distanceX + ";" + distanceY);// 锟斤拷锟斤拷锟�

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onShowPress: \n" + e.getX() + " : " + e.getY());
        isLongPress = true;
        sendMessage(MOUSEEVENTF_LEFTDOWN + "");// 锟斤拷锟斤拷锟�
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onSingleTapUp: \n" + e.getX() + " : " + e.getY());
        return false;
    }

    // 锟节讹拷锟轿碉拷锟斤拷down时锟斤拷锟斤拷锟斤拷e为锟斤拷一锟斤拷down时锟斤拷MotionEvent
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onDoubleTap: " + e.getX() + " : " + e.getY());

        sendMessage(MOUSEEVENTF_DOUBLETAP + "");
        return false;
    }

    // 锟节讹拷锟轿碉拷锟斤拷down,move锟斤拷up时锟斤拷锟斤拷锟斤拷锟斤拷e为锟斤拷同时锟斤拷锟铰碉拷MotionEvent
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onDoubleTapEvent: " + e.getX() + " : " + e.getY());
        return false;
    }

    // 锟斤拷锟揭伙拷蔚锟斤拷锟斤拷锟斤拷锟饺凤拷锟矫伙拷卸锟斤拷锟斤拷录锟斤拷蟠シ锟斤拷锟�00ms锟斤拷锟斤拷e为down时锟斤拷MotionEvent
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        touchInof.setText("onSingleTapConfirmed: " + e.getX() + " : " + e.getY());

        sendMessage(MOUSEEVENTF_TAP + "");
        return false;
    }

}
