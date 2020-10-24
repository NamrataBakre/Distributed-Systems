package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SimpleDynamoProvider extends ContentProvider {

	private final Semaphore lock = new Semaphore(1, true);
	private String FILE_NAME = "default";
	String myPort;
	static final int SERVER_PORT = 10000;
	private ArrayList<String> portValues = new ArrayList<String>();
	MatrixCursor matrix = null;
	int flagValue = 0;
	int i=0;
	int j=3;
	public volatile boolean close = false;
	private Map<String, Cursor> elements = new HashMap<String, Cursor>();
	private HashMap<String,ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
	String firstNode= "11108";
	String secondNode = "11112";
	String thirdNode = "11116";
	String fourthNode = "11120";
	String fifthNode = "11124";
	ArrayList<String> myNodes = new ArrayList<String>(Arrays.asList(firstNode,secondNode,thirdNode,fourthNode,fifthNode));


	public Comparator<String> valComparator = new Comparator<String>() {

		public int compare(String val1, String val2) {
			String a = "";
			String b = "";
			try {
				a = genHash(String.valueOf(Integer.parseInt(val1)/2));
			} catch (NumberFormatException e) {
				try {
					a = genHash(val1);
				} catch (java.security.NoSuchAlgorithmException q) {
					System.out.println("Exception caught");
				}
			} catch (java.security.NoSuchAlgorithmException e) {
				System.out.println("Exception caught");
			}
			try {
				b = genHash(String.valueOf(Integer.parseInt(val2)/2));
			} catch (NumberFormatException e) {
				try {
					b = genHash(val2);
				} catch (java.security.NoSuchAlgorithmException f) {
					System.out.println("Exception caught");
				}
			} catch (java.security.NoSuchAlgorithmException p) {
				System.out.println("Exception caught");
			}
			return a.compareTo(b);
		}
	};

	public void callClient4(String val1, String val2, String val3, String val4){
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, val1 + "#" + val2 + "#" + val3 + "#" + val4);
	}

	public void callClient5(String val1, String val2, String val3, String val4, String val5){
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, val1 + "#" + val2 + "#" + val3 + "#" + val4 + "#" + val5);
	}

	public void callClient(String message){
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message);
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		if(selection.equals("@")){

			//Referred from https://stackoverflow.com/questions/48387796/sharedpreferences-data-not-deleting
			SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
			sharedPreferences.edit().clear();
			boolean isCleared = sharedPreferences.edit().commit();

			if(isCleared)
				Log.d(TAG, "Cleared");
			else
				Log.d(TAG, "Not Cleared");

		}
		else if(selection.equals("*")){
			for(int i = 0 ; i < portValues.size() ; i++){
				String currPortVal = portValues.get(i);
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"ALLDelete" + "#" + "is" + "#" + currPortVal);
			}
		}
		else{
			ArrayList<Integer> indexValues = new ArrayList<Integer>();
			ArrayList<String> array = new ArrayList<String>();
			array.addAll(portValues);
			array.add(selection);
			Collections.sort(array,valComparator);
			if (array.indexOf(selection)*2 == 10){indexValues.add(i); indexValues.add(i+1); indexValues.add(i+2);}
			else if (array.indexOf(selection)*2 == 6){indexValues.add(j); indexValues.add(j+1); indexValues.add(i);}
			else if (array.indexOf(selection)*2 == 8){indexValues.add(j+1); indexValues.add(i); indexValues.add(i+1);}
			else {indexValues.add(array.indexOf(selection)); indexValues.add(array.indexOf(selection) + 1); indexValues.add(array.indexOf(selection) + 2); }
			for(int i = 0 ; i < indexValues.size() ; i++){
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"Delete" + "#" + "is" + "#" + portValues.get(indexValues.get(i)) + "#" + selection);
			}
		}
		return 0;
	}


	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		String key = (String)values.get("key");
		String value = (String)values.get("value");
		if(portValues.size() == 1){
			SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
			sharedPreferences.edit().putString(key,value).apply();
			return uri;
		}

		ArrayList<Integer> indexValues = new ArrayList<Integer>();
		ArrayList<String> array = new ArrayList<String>();
		array.addAll(portValues);
		array.add(key);
		Collections.sort(array,valComparator);
		if (array.indexOf(key)*2 == 10){indexValues.add(i); indexValues.add(i+1); indexValues.add(i+2);}
		else if (array.indexOf(key)*2 == 6){indexValues.add(j); indexValues.add(j+1); indexValues.add(i);}
		else if (array.indexOf(key)*2 == 8){indexValues.add(j+1); indexValues.add(i); indexValues.add(i+1);}
		else {indexValues.add(array.indexOf(key)); indexValues.add(array.indexOf(key) + 1); indexValues.add(array.indexOf(key) + 2);}
		for(int i = 0 ; i < indexValues.size() ; i++){
			String getVal = portValues.get(indexValues.get(i));
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"Insert" + "#" + myPort + "#" + getVal + "#" + key + "#" + value);
		}
		return null;
	}

	// below code has been referred from
	//https://stackoverflow.com/questions/22089411/how-to-get-all-keys-of-sharedpreferences-programmatically-in-android
	//How to get all keys of SharedPreferences programmatically in Android?
	public void functionCall() {
		SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		Map<String, ?> allEntries = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			String key = entry.getKey();
			String value = (String)entry.getValue();
			matrix.addRow(new String[]{key, value});
		}
	}

	public void queryCall(){

		for (String valPort : portValues) {
			if (valPort.equals(myPort)) continue;
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "ALLQuery" + "#" + myPort + "#" + valPort);
			synchronized (matrix) {
				try {
					matrix.wait();
				} catch (java.lang.InterruptedException e) {
					System.out.println("Execution Interrupted!");
				}
			}
		}
	}

	public void cursorCommonCall(){
		synchronized (matrix){
			matrix.notify();
		}
	}

	public void getKeyProcessing(String val1, String val2){
		ArrayList<String> array = hashMap.get(val1);
		int arraySize = array.size();
		for(int i = 0 ; i < arraySize ; i++){
			String sort[] = array.get(i).split("@@");
			String key = sort[0];
			String value = sort[1];
			val2 += key + ":" + value + "@";
		}
		callClient(val2);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub

		portValues.add(firstNode);
		portValues.add(secondNode);
		portValues.add(thirdNode);
		portValues.add(fourthNode);
		portValues.add(fifthNode);
		Collections.sort(portValues,valComparator);
		TelephonyManager tel = (TelephonyManager)this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		try {
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

		} catch (IOException e) {
			return false;
		}
		for (int i = 0; i < portValues.size(); i++) {
			if(portValues.get(i).equals(myPort))
				continue;
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"GetKey" + "#" + myPort + "#" + portValues.get(i));
		}
		return false;
	}

	public void clientInsert(String val1, String val2, String val3){
		if(!hashMap.containsKey(val1)){
			hashMap.put(val1,new ArrayList<String>());
		}
		hashMap.get(val1).add(val2 + "@@" + val3);
	}

	public void cursorCall(MatrixCursor cursor, String val1, String val2){
		synchronized (cursor) {
			cursor.addRow(new String[]{val1,val2});
			cursor.notify();
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		Cursor matrix1 = new MatrixCursor(new String[] {"key","value"});
		matrix = new MatrixCursor(new String[] {"key","value"});

		if(selection.equals("@")){
			functionCall();
			return matrix;
		}
		else if(selection.equals("*")) {
			functionCall();
			if ((portValues.size() != 1)) {
				queryCall();
			}
		}
		else{
			elements.put(selection, matrix1);

			ArrayList<Integer> indexValues = new ArrayList<Integer>();
			ArrayList<String> array = new ArrayList<String>();
			array.addAll(portValues);
			array.add(selection);
			Collections.sort(array,valComparator);
			if (array.indexOf(selection)*2 == 10){indexValues.add(i); indexValues.add(i+1); indexValues.add(i+2);}
			else if (array.indexOf(selection)*2 == 6){indexValues.add(j); indexValues.add(j+1); indexValues.add(i);}
			else if (array.indexOf(selection)*2 == 8){indexValues.add(j+1); indexValues.add(i); indexValues.add(i+1);}
			else {indexValues.add(array.indexOf(selection)); indexValues.add(array.indexOf(selection) + 1);indexValues.add(array.indexOf(selection) + 2); }
			elements.put(selection, matrix1);
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"Query" + "#" + myPort + "#" + portValues.get(indexValues.get(0)) + "#" + selection);
			return run(matrix1,selection);
		}
		System.out.println("returning cursor");
		return matrix;
	}

	synchronized public Cursor run(Cursor curs, String selection){
		synchronized (curs){
			try{
				curs.wait();
				elements.remove(selection);

			}catch(java.lang.InterruptedException e){
				System.out.println("Interruption");
			}
		}
		return curs;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}


	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}



	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			String msgToSend="";
			try {
				while (true) {
					Socket connection = serverSocket.accept();
					InputStream input = connection.getInputStream();
					DataInputStream data = new DataInputStream(input);
					msgToSend = data.readUTF();
					OutputStream out = connection.getOutputStream();
					DataOutputStream dataoutput = new DataOutputStream(out);
					dataoutput.writeUTF("acknowledged!");
					dataoutput.flush();
					String[] array = msgToSend.split("#");
					String actionToTake = array[0];

					if(actionToTake.equals("Insert")){
						SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
						sharedPreferences.edit().putString(array[3],array[4]).apply();
					}


					else if(actionToTake.equals("Query")){
						String stringToSend = array[3];
						String fetchPort = array[1];
						SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
						if(sharedPreferences.getString(stringToSend,"blank").equals("blank")){
							callClient4("Query",array[1],array[4],array[3]);
						}
						else{
							callClient5("Query1","default",fetchPort,stringToSend,sharedPreferences.getString(stringToSend,"blank"));
						}
					}

					else if(actionToTake.equals("Query1")){
						MatrixCursor c = (MatrixCursor) elements.get(array[3]);
						cursorCall(c,array[3],array[4]);
					}

					else if(actionToTake.equals("ALLQuery")){
						String queryMessage = "ALLQueryProcessed" + "#" + "default" + "#" + array[1] + "#";
						if(FILE_NAME.isEmpty()){
							callClient4("ALLQueryProcessed","default",array[1],"SendAsNull");
						}
						else{
							//https://stackoverflow.com/questions/22089411/how-to-get-all-keys-of-sharedpreferences-programmatically-in-android
							SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
							Map<String, ?> allEntries = sharedPreferences.getAll();
							for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
								String key = entry.getKey();
								String value = entry.getValue().toString();
								queryMessage += key + ":" + value + "@";
							}
							callClient(queryMessage);
						}
					}

					else if(actionToTake.equals("ALLQueryProcessed")){
						flagValue ++;
						if(array[3].equals("SendAsNull")){
							cursorCommonCall();
						}
						else{
							String array1[] = array[3].split("@");
							for(int i = 0 ; i < array1.length ; i++){
								String storage[] = array1[i].split(":");
								matrix.addRow(new String[]{storage[0],storage[1]});
							}
							cursorCommonCall();
						}
					}

					else if(actionToTake.equals("Delete")){
						SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
						sharedPreferences.edit().remove(array[3]).apply();
					}

					else if(actionToTake.equals("ALLDelete")){
						SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
						sharedPreferences.edit().clear();
						boolean isCleared = sharedPreferences.edit().commit();

						if(isCleared)
							Log.d(TAG, "Cleared");
						else
							Log.d(TAG, "Not Cleared");
					}

					else if(actionToTake.equals("GetKey")){
						String getMessage = "GetKeyProcessed" + "#" + "default" + "#" + array[1] + "#";
						if(hashMap.containsKey(array[1]) && hashMap.size()!= 0){
							getKeyProcessing(array[1],getMessage);
						}
						else{
							callClient4("GetKeyProcessed","default",array[1],"SendAsNullVal");
						}
					}
					else if(actionToTake.equals("GetKeyProcessed")){
						if(!array[3].equals("SendAsNullVal")){
							String vals[] = array[3].split("@");
							for(int i = 0 ; i < vals.length ; i++) {
								String sortVals[] = vals[i].split(":");
								SharedPreferences sharedPreferences = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
								sharedPreferences.edit().putString(sortVals[0],sortVals[1]).apply();
							}
						}
					}

				}
			} catch (IOException e) {
				System.out.println("IO Exception occured in server block");
			}
			return null;
		}
	}


	private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {
			try {
				//https://stackoverflow.com/questions/2332765/lock-mutex-semaphore-whats-the-difference
				lock.acquire();
			}
			catch (java.lang.InterruptedException e){

			}
			String msgToSend = msgs[0];
			String[] array = msgToSend.split("#");
			String takeAction = array[0];

			try {
				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(array[2]));
				DataOutputStream dataoutputstream = new DataOutputStream(socket.getOutputStream());
				socket.setSoLinger(true,30000000);
				dataoutputstream.writeUTF(msgToSend);
				dataoutputstream.flush();

				InputStream in = socket.getInputStream();
				DataInputStream data = new DataInputStream(in);
				data.readUTF();
			}
			catch (UnknownHostException e) {
				Log.e(TAG, "ClientTask UnknownHostException");
			}

			catch (Exception exp) {
				if(takeAction.equals("Insert")){

					String val1 = array[2];
					String val2 = array[3];
					String val3 = array[4];

					clientInsert(val1, val2, val3);
				}

				else if(takeAction.equals("Query")){

					String val1 = array[1];
					String val2 = array[2];
					String val3 = array[3];
					try{
						Thread.sleep(5000);
					}
					catch (java.lang.InterruptedException e){
						System.out.println("Interrupted Exception.");
					}
					ArrayList<Integer> indexValues = new ArrayList<Integer>();
					ArrayList<String> arraylist = new ArrayList<String>();
					arraylist.addAll(portValues);
					arraylist.add(val3);
					Collections.sort(arraylist,valComparator);
					if (arraylist.indexOf(val3)*2 == 10){indexValues.add(i); indexValues.add(i+1); indexValues.add(i+2);}
					else if (arraylist.indexOf(val3)*2 == 6){indexValues.add(j); indexValues.add(j+1); indexValues.add(i);}
					else if (arraylist.indexOf(val3)*2 == 8){indexValues.add(j+1); indexValues.add(i); indexValues.add(i+1);}
					else {indexValues.add(arraylist.indexOf(val3)); indexValues.add(arraylist.indexOf(val3) + 1); indexValues.add(arraylist.indexOf(val3) + 2);}
					String valAtIndex2 = portValues.get(indexValues.get(2));
					String valAtIndex1 = portValues.get(indexValues.get(1));

					int valSend;
					if (portValues.indexOf(val2) == j+1) valSend =0; else valSend=portValues.indexOf(val2)+1;
					String portV = portValues.get(valSend);

					try{
						Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(portV));
						DataOutputStream data = new DataOutputStream(socket.getOutputStream());
						socket.setSoLinger(true,30000000);
						data.writeUTF("Query" + "#" + val1 + "#" + valAtIndex2 + "#" + val3 + "#" + valAtIndex1);
						data.flush();
					}catch (java.io.IOException e){
						System.out.println("IO Exception in Client block");
					}
				}
				else if(takeAction.equals("ALLQuery")){
					cursorCommonCall();
				}
			}
			finally {
				//https://stackoverflow.com/questions/2332765/lock-mutex-semaphore-whats-the-difference
				lock.release();
			}
			return null;
		}
	}


}
