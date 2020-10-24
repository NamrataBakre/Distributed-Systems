package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;


import java.io.IOException;
import java.lang.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        /* Below code has been taken from PA1 */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }


        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        /* Below code is referenced from https://developer.android.com/reference/android/widget/Button */
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText1);
                /* Below code is referred from PA1 */
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        private Uri mUri = null;

        /* Below oode has been taken from OnPTestClickListener.java file */
        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

            try {
                int filename = 0;
                    while(true) {
                        Socket socket = serverSocket.accept(); /*This will accept the incoming request to socket */
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        String userInput = dataInputStream.readUTF();

                        /* Referred from PA2 Specifications */
                        /* Inserting <key,value> into Content Provider */
                        ContentValues keyValueToInsert = new ContentValues();
                        keyValueToInsert.put("key", Integer.toString(filename));
                        keyValueToInsert.put("value", userInput);

                        getContentResolver().insert(mUri, keyValueToInsert);
                        publishProgress(userInput);
                        filename = filename + 1;
                    }
            } catch (IOException e) {
            }
            return null;
        }

        /* Below code has been copied from PA1 */
        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");

            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... msgs) {
                try {
                    String msgToSend = msgs[0];
                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT0));
                    DataOutputStream dataOutputStream1 = new DataOutputStream(socket1.getOutputStream());
                    dataOutputStream1.writeUTF(msgToSend);
                    dataOutputStream1.flush();
                    dataOutputStream1.close();
                    socket1.close();

                    Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT1));
                    DataOutputStream dataOutputStream2 = new DataOutputStream(socket2.getOutputStream());
                    dataOutputStream2.writeUTF(msgToSend);
                    dataOutputStream2.flush();
                    dataOutputStream2.close();
                    socket2.close();

                    Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT2));
                    DataOutputStream dataOutputStream3 = new DataOutputStream(socket3.getOutputStream());
                    dataOutputStream3.writeUTF(msgToSend);
                    dataOutputStream3.flush();
                    dataOutputStream3.close();
                    socket3.close();

                    Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT3));
                    DataOutputStream dataOutputStream4 = new DataOutputStream(socket4.getOutputStream());
                    dataOutputStream4.writeUTF(msgToSend);
                    dataOutputStream4.flush();
                    dataOutputStream4.close();
                    socket4.close();

                    Socket socket5 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT4));
                    DataOutputStream dataOutputStream5 = new DataOutputStream(socket5.getOutputStream());
                    dataOutputStream5.writeUTF(msgToSend);
                    dataOutputStream5.flush();
                    dataOutputStream5.close();
                    socket5.close();

                } catch (java.net.UnknownHostException e) {
                } catch (java.io.IOException e) {
                }
            return null;
        }
    }

}