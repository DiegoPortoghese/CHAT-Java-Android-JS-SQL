package com.example.utente.chatclient;

import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


    private static final int SERVERPORT = 5552;
    private static final String SERVER_IP = "ipwnd.ddns.net";
    ClientThread clThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //response = (TextView) findViewById(R.id.textView);
        //button = (Button) findViewById(R.id.button);
    }
    //ON CLICK LOGIN
    public void sendMessageLogin(View view) throws IOException {
        Socket socket;
        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
        socket = new Socket(serverAddr, SERVERPORT);
        clThread=new ClientThread(socket);

        //clThread.start();
    }

    //ON CLICK REGISTATION
    public void sendMessageRegg(View view) {
        new Thread(new ReggThread()).start();
    }
    public void gotoR(View view) {
        setContentView(R.layout.chatroom);
        clThread.newmessage();

    }
    public void back_fromchat(View view){
        setContentView(R.layout.layout2);
        clThread.userlistF();

    }
    public void SendMsgF(View view)
    {
        EditText textboxB = (EditText) findViewById(R.id.editText);
        String testo = String.valueOf(textboxB.getText());
        clThread.SendMsg(testo);
    }


    /** LOGIN **/
    class ClientThread extends Thread {


    //TextView response;
    public PrintWriter out;
    String response="";
    public Socket socket;
    String resultx;
    // ARRAY USER LIST
    List<String> userslist = new ArrayList<String>();
    List<String> msglist = new ArrayList<String>();
    String username;
    String password;
    //Socket socket;
    public ClientThread  (Socket socket_) throws IOException {
        this.socket = socket_;
        out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())),
                true);

        this.start();
    }
                                                        //run

    @Override
    public void run() {
        try {
            EditText userBox = (EditText) findViewById(R.id.usernameText);
            EditText pswBox = (EditText) findViewById(R.id.passwordText);
            //TextView responseBox = (TextView) findViewById(R.id.textView);
            username= String.valueOf(userBox.getText());
            password= String.valueOf(pswBox.getText());

            out.println("mA");
            //JSON Login Rqst
            out.println("{\"type\":\"login-s\",\"nick\":\""+username+"\",\"password\":\""+password+"\"}");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
/*
* notice: inputStream.read() will block if no data return
*/
            while ((bytesRead = inputStream.read(buffer)) != -1) {

                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response = byteArrayOutputStream.toString("UTF-8");
                String type="";
                JSONObject JSmessage = null;

                try {
                    JSmessage = new JSONObject(response);
                    type= JSmessage.getString("type");
                    byteArrayOutputStream.reset();
                }catch (Exception e){}

                if(type.equals("login-r")) {
                    if(JSmessage.getInt("r")!=0) {
                        resultx="LOGGED";
                        SetResutX();
                        out.println("{\"type\":\"usersonline\"}");

                    }else{
                        resultx="ERROR LOGIN";
                        SetResutX();
                        socket.close();
                    }
                }
                //LIST OF USER in LOGIN time
                if(type.equals("users-l"))
                {
                    JSONArray cast = JSmessage.getJSONArray("users");
                    for (int i=0; i<cast.length(); i++) {
                        userslist.add(cast.getString(i));
                    }
                    userlistF();
                }
                //ADD USER
                if(type.equals("adduser")) {
                    userslist.add(JSmessage.getString("user"));
                    addUserF();
                }
                //REMOVE USER
                if(type.equals("rmuser")) {
                    ListIterator listIterator = userslist.listIterator();
                    String userTRm = JSmessage.getString("user");
                    while(listIterator.hasNext())
                    {
                        String a = (String)listIterator.next();
                        if(a.equals(userTRm)) {
                            listIterator.remove();
                        }
                    }
                    rmUserF();
                }

                //MESSAGE RECEIVED
                if(type.equals("message")) {
                    //ListIterator listIterator = msglist.listIterator();
                    String msg ="";
                    msg+= JSmessage.getString("from")+": ";
                    msg+= JSmessage.getString("data");
                    msglist.add(msg);
                    newmessage();
                    //rmUserF();
                }
                response="";
            }
        } catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (JSONException e) {e.printStackTrace();}

    }


    public void SetResutX () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView responseBox = (TextView) findViewById(R.id.textView);
                    responseBox.setText(resultx);
                    String a = "";
                    a = resultx;
                    if (a.equals("LOGGED")) {
                        setContentView(R.layout.layout2);
                    }
                }catch (Exception e){}

            }
        });
    }

    public void userlistF () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //final ArrayList<String> listp = new ArrayList<String>();
                    /**
                    for (int i = 0; i < userslist.length; ++i) {
                        listp.add(userslist[i]);
                    }**/
                    final ListView mylist = (ListView) findViewById(R.id.listView1);
                    final ArrayAdapter <String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                    mylist.setAdapter(adapter);
                }catch (Exception e){}
            }
        });
    }

    public void addUserF () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ListView mylist = (ListView) findViewById(R.id.listView1);
                    final ArrayAdapter <String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                    mylist.setAdapter(adapter);
                }catch (Exception e){}
            }
        });
    }
    public void rmUserF () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    final ListView mylist = (ListView) findViewById(R.id.listView1);
                    final ArrayAdapter <String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                    mylist.setAdapter(adapter);

                }catch (Exception e){}
            }
        });
    }

    public void newmessage () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ListView mylist = (ListView) findViewById(R.id.listMsg);
                    final ArrayAdapter <String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, msglist);
                    mylist.setAdapter(adapter);
                }catch (Exception e){}
            }
        });
    }

     public void SendMsg(String testo)
     {
         String BuffDmsg = "{\"type\":\"message\",\"from\":\"" + username + "\",\"data\":\"" + testo + "\"}";
         try{
             out.println(BuffDmsg);
         }catch (Exception e){}

     }


        //public void OnPostExecute()
    }

    /**   REGISTRATION   **/
    class ReggThread implements Runnable {

        PrintWriter out;
        String response="";

        public Socket socket;
        String resultx;
        public void SetResutX ()
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView responseBox = (TextView) findViewById(R.id.textView);
                    responseBox.setText(resultx);

                }
            });
        }


        @Override
        public void run() {
            try {

                EditText userBox = (EditText) findViewById(R.id.usernameText);
                EditText pswBox = (EditText) findViewById(R.id.passwordText);
                //TextView responseBox = (TextView) findViewById(R.id.textView);

                String username= String.valueOf(userBox.getText());
                String password= String.valueOf(pswBox.getText());

                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                this.socket = new Socket(serverAddr, SERVERPORT);
                this.out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.println("mA");
                //JSON Login Rqst
                out.println("{\"type\":\"regg-s\",\"nick\":\""+username+"\",\"password\":\""+password+"\"}");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                        1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
			/*
             * notice: inputStream.read() will block if no data return
			 */
                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                    String type="";
                    JSONObject JSmessage = null;

                    try {
                        JSmessage = new JSONObject(response);
                        type= JSmessage.getString("type");
                    }catch (Exception e){}

                    if(type.equals("regg-r"))
                    {
                        if(JSmessage.getInt("r")!=0)
                        {
                            resultx="Registered Successfully\n Now you can login";
                            SetResutX();
                            socket.close();
                        }else{
                            resultx="REGISTRATION ERROR";
                            SetResutX();
                            socket.close();
                        }
                    }

                    response="";
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



}