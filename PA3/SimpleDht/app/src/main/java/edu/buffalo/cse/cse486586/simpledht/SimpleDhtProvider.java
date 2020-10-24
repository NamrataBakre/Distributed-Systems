package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import static android.content.Context.MODE_PRIVATE;

public class SimpleDhtProvider extends ContentProvider
{
    static final int SERVER_PORT = 10000;
    private Uri mUri=null;
    HashMap<String,String> hashMap1=new HashMap<String, String>();
    Map<String, String> mapPorts = new HashMap<String, String>();
    ArrayList hashPortValues=new ArrayList();
    String initialPort = "11108";
    String queryVal="";
    String ALLQuery="";
    ArrayList portValues = new ArrayList();
    ArrayList<String> hashedPorts=new ArrayList<String>();
    HashMap<String,String> hashMap=new HashMap<String, String>();
    String myPort="";
    String returned="";
    String firstNode= "11108";
    String secondNode = "11112";
    String thirdNode = "11116";
    String fourthNode = "11120";
    String fifthNode = "11124";
    ArrayList<String> myNodes = new ArrayList<String>(Arrays.asList(firstNode,secondNode,thirdNode,fourthNode,fifthNode));
    int returnLength=0;
    int check = 0;


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // TODO Auto-generated method stub

        for(int i=0;i<portValues.size();i++)
        {
            String currPort = (String) portValues.get(i);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currPort, "Delete"+ "#" + selection);
            try {
                Thread.sleep(1000);
            } catch (java.lang.InterruptedException e) {
            }
            if(selection == "")
            {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void cursorOperation(String val1, String val2, MatrixCursor cursor){
        String[] operation = new String[2];
        operation[0] = val1;
        operation[1] = val2;
        cursor.addRow(operation);
    }

    public String fileReader(String key)
    {
        String returnedVal="";
        if(hashedPorts.contains(key))
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getContext().openFileInput(key)));
                returnedVal = bufferedReader.readLine();
            }
            catch(Exception e)
            {
                System.out.println("File not found");
            }
        }
        return returnedVal;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method
        String key = (String) values.get("key");
        String value = (String) values.get("value");
        String getPort = getPort();

        if (!getPort.equals(initialPort))
        {
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getPort, "firstOperation" + "#");
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }

        if ((portValues.size()==0||portValues.size()==1 && getPort.equals(initialPort)))
        {
            try {
                FileOutputStream outputStream = this.getContext().openFileOutput(key, MODE_PRIVATE);
                outputStream.write(value.getBytes());
                hashMap.put(key,value);
                hashedPorts.add(key);
                outputStream.close();
            }
            catch (java.io.FileNotFoundException e){
            }
            catch (java.io.IOException e){
            }
        }

        else
        {
            for(int i=0;i<portValues.size();i++)
            {
                hashMap1.put(getHash(mapPorts.get((String)portValues.get(i))),(String)portValues.get(i));
                hashPortValues.add(getHash(mapPorts.get(portValues.get(i))));
            }
            Collections.sort(hashPortValues);
            for (int j = 0; j < hashPortValues.size(); j++)
            {
                if (getHash(key).compareTo((String)hashPortValues.get(j)) < 0)
                {
                    if (hashMap1.get(hashPortValues.get(j)).equals(myPort))
                    {
                        try {
                            FileOutputStream outputStream = this.getContext().openFileOutput(key, MODE_PRIVATE);
                            outputStream.write(value.getBytes());
                            outputStream.close();
                            hashedPorts.add(key);
                            hashMap.put(key,value);
                        }
                        catch (java.io.FileNotFoundException e){
                        }
                        catch (java.io.IOException e){
                        }
                    }
                    else
                    {
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hashMap1.get(hashPortValues.get(j)), "Insert" + "#" + key + "#" + value);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                    }
                    return uri;
                }
            }
            if (hashMap1.get(hashPortValues.get(0)).equals(myPort))
            {
                try {
                    FileOutputStream outputStream = this.getContext().openFileOutput(key, MODE_PRIVATE);
                    outputStream.write(value.getBytes());
                    outputStream.close();
                    hashedPorts.add(key);
                    hashMap.put(key,value);
                }
                catch (java.io.FileNotFoundException e){
                }
                catch (java.io.IOException e){
                }
            }
            else
            {
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hashMap1.get(hashPortValues.get(0)), "Insert" + "#" + key + "#" + value);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }

            return uri;
        }
        return uri;
    }



    @Override
    public boolean onCreate()
    {
        getPort();
        MapperPorts();
        try
        {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch (java.io.IOException e)
        {
            return false;
        }

        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, "OnCreate" + "#");
        return true;

    }

    public String getPort(){
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        return myPort;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        // TODO Auto-generated method stub
        String returnedVal = "";
        MatrixCursor cursor = new MatrixCursor(new String[] {"key", "value"});

        if ((portValues.size() == 1 && myPort.equals(initialPort)) || portValues.size() == 0)
        {
            if (selection.equals("*") || selection.equals("@"))
            {
                for (int i = 0; i < hashedPorts.size(); i++)
                {
                    String key=hashedPorts.get(i);
                    returnedVal=fileReader(key);
                    cursorOperation(key,returnedVal,cursor);
                }
                System.out.println("operation successful. Returning the cursor value.");
                return cursor;
            }

            else {
                returnedVal=fileReader(selection);
                cursorOperation(selection,returnedVal,cursor);
                System.out.println("operation successful. Returning the cursor value.");
                return cursor;
            }
        }

        else
        {
            if(!selection.equals("*") && !selection.equals("@"))
            {
                if(hashedPorts.contains(selection))
                {
                    returnedVal=fileReader(selection);
                    cursorOperation(selection,returnedVal,cursor);
                    System.out.println("operation successful. Returning the cursor value.");
                    return cursor;
                }
                else
                {
                    for(int i=0;i<portValues.size();i++)
                    {
                        String port=(String)portValues.get(i);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, "Query" + "#" + selection);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }

                        if(queryVal.length()>1)
                        {
                            cursorOperation(selection,queryVal,cursor);
                            System.out.println("operation successful. Returning the cursor value.");
                            return cursor;
                        }
                    }
                }
            }
            if (selection.equals("@"))
            {
                for (int i = 0; i < hashedPorts.size(); i++)
                {
                    String key = hashedPorts.get(i);
                    returnedVal=fileReader(key);
                    cursorOperation(key,returnedVal,cursor);
                }
                System.out.println("operation successful. Returning the cursor value.");
                return cursor;
            }
            if(selection.equals("*"))
            {
                for(int i=0;i<portValues.size();i++)
                {
                    String port=(String)portValues.get(i);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, "ALLQuery" + "#");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    String[] splitter=ALLQuery.split(",");
                    for(int j=0;j<splitter.length;j++)
                    {
                        if(splitter[j].length()>5 && splitter[j+1].length()>5) {
                            cursorOperation(splitter[j],splitter[j+1],cursor);
                            j++;
                        }
                    }
                }
                System.out.println("operation successful. Returning the cursor value.");
                return cursor;
            }
        }
        return null;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    public void MapperPorts()
    {
        mapPorts.put("11108", "5554");
        mapPorts.put("11112", "5556");
        mapPorts.put("11116", "5558");
        mapPorts.put("11120", "5560");
        mapPorts.put("11124", "5562");
    }

    private String getHash(String value){

        String hashVal = "";
        try{
            hashVal = genHash(value);
        }catch (NoSuchAlgorithmException e){
        }
        return hashVal;
    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void>
    {

        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
        @Override
        protected Void doInBackground(ServerSocket... sockets)
        {
            int checkpoint = 0;
            Boolean flag = null;
            ServerSocket serverSocket = sockets[0];
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
            String messageDelivery="";

            try {
                while (true)
                {
                    Socket connect = serverSocket.accept();
                    InputStreamReader isr = new InputStreamReader(connect.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String msg = br.readLine();
                    if (!portValues.contains(msg) && portValues != null){
                        flag = true;
                    }

                    String array[] = msg.split("#");
                    String takeAction = array[0];

                    if (flag = true && myNodes.contains(msg) )
                    {
                        portValues.add(msg);
                    }

                    if(takeAction.equals("Insert"))
                    {
                        String key=array[1];
                        String value=array[2];
                        try {
                            FileOutputStream outputStream = getContext().openFileOutput(key, MODE_PRIVATE);
                            outputStream.write(value.getBytes());
                            outputStream.close();
                            hashedPorts.add(key);
                            hashMap.put(key,value);
                        }
                        catch (java.io.FileNotFoundException e){
                        }
                        catch (java.io.IOException e){
                        }
                    }


                    if(takeAction.equals("firstOperation"))
                    {
                        Collections.sort(portValues);

                        https://stackoverflow.com/questions/1978933/a-quick-and-easy-way-to-join-array-elements-with-a-separator-the-opposite-of-sp
                        messageDelivery = TextUtils.join("", portValues);
                    }

                    else
                    {
                        messageDelivery=Integer.toString(portValues.size());
                    }

                    if(takeAction.equals("Delete"))
                    {
                        String filetoDelete=array[1];
                        if(hashedPorts.contains(filetoDelete))
                        {
                            Context context=getContext();
                            context.deleteFile(filetoDelete);
                            messageDelivery=filetoDelete;
                        }
                        else
                        {
                            messageDelivery="Not";
                        }
                    }

                    if(takeAction.equals("Query"))
                    {
                        String portV = array[1];
                        if(hashedPorts.contains(portV)){
                            messageDelivery=fileReader(portV);
                        }
                        else { messageDelivery=""; }
                    }


                    if(takeAction.equals("ALLQuery"))
                    {
                        messageDelivery=messageDelivery+",";
                        for(int u=0;u<hashedPorts.size();u++)
                        {
                            String file=hashedPorts.get(u);
                            String val=fileReader(file);
                            messageDelivery=messageDelivery+file+","+val+",";
                        }
                    }

                    PrintWriter printwriter = new PrintWriter(connect.getOutputStream(), true);
                    printwriter.println(messageDelivery);
                    printwriter.flush();
                    checkpoint = checkpoint +1;
                }
            }

            catch (IOException e)
            {
            }
            return null;
        }

    }

    public String functionCall (String val1, String val2){
        String readVal = "";
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(val1));
            PrintWriter p0 = new PrintWriter(socket.getOutputStream(), true);
            p0.println(val2);
            p0.flush();

            InputStreamReader input = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(input);
            readVal = br.readLine();
            socket.close();
        }
        catch (java.net.UnknownHostException e){

        }
        catch (java.io.IOException e){

        }
        return readVal;
    }

    public void operate(String returned, int check){
        String val=returned.substring(check,check+5);
        if(!portValues.contains(val)) {
            portValues.add(val);
        }
    }


    private class ClientTask extends AsyncTask<String, Void, Void>
    {
        int bool=0;
        @Override
        protected Void doInBackground(String... msgs)
        {
            try
            {
                String msgToSend = msgs[1];
                String[] array = msgToSend.split("#");
                String actionToTake = array[0];


                if(actionToTake.equals("Delete"))
                {
                    bool=1;
                    functionCall(msgs[0],msgs[1]);

                }

                
                if(actionToTake.equals("Query"))
                {
                    bool=1;
                    queryVal = functionCall(msgs[0],msgs[1]);

                }


                if(actionToTake.equals("ALLQuery"))
                {
                    bool=1;
                    ALLQuery = functionCall(msgs[0],msgs[1]);

                }


                if(actionToTake.equals("Insert"))
                {
                    returned = functionCall(msgs[0],msgs[1]);

                }

                else
                {
                    if(bool==0)
                    {
                        String val="";
                        if(array[0].equals("OnCreate")) {
                            val=msgs[0];
                        }
                        else {
                            val=msgs[1];
                        }
                        try
                        {
                            returned = functionCall(initialPort,val);
                            while(returned != null)
                            {
                                returnLength=returned.length();
                                break;
                            }
                            if(returnLength%5==0)
                            {
                                while(check<returnLength)
                                { operate(returned,check); check = check + 5; }
                            }
                            else
                            {
                                System.out.println("Fulfilled");
                            }

                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
            catch (Exception e)
            {
            }
            return null;
        }
    }
}