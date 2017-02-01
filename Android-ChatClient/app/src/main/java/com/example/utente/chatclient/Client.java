package com.example.utente.chatclient;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, Void> {

    String response = "";
    TextView textResponse;
    Socket socket = null;
    PrintWriter out;



    Client(Socket socket_) throws IOException {
        this.socket= socket_;
    }

    public void sendMsgFunc(String msgtoS_) {
        String toSend= msgtoS_;
        out.println(toSend);
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        return null;
    }


}