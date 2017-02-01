/*
 * 
 * Created by Diego Portoghese
 * 
 */
package chatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.json.*;

public class Chatserver {
    //array of clients (for broadcast the message)
    static List<connect> connects = Collections.<connect>synchronizedList(new ArrayList<connect>());

    public static class connect extends Thread{
	// dichiarazione delle variabili socket e degli Stream
	Socket client;
        OutputStream out;
        InputStream in ;
        //class websocket for read and send messages to client in Websocket
        websocket ClientWB;
        BufferedReader inBF;
        PrintStream outBF;
        String device;
        
        
        //CONNECT
	public connect(Socket client) throws IOException, NoSuchAlgorithmException{
            //inizialize Buffer in e Stream Out
            inBF = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outBF = new PrintStream(client.getOutputStream(), true);
            
            System.out.println("new client IN");
            

            //set socket
            this.client = client;
            //set Stream of the socket
            this.in =  client.getInputStream();
            this.out = client.getOutputStream();
            //MYCLASS WEBSOCKET
            
            this.ClientWB = new websocket(in,out);

            // invoca automaticamente il metodo run() //start
            this.start();
            System.out.println("thread start");
	}
        final void streamout(String stringa) throws IOException{
            SendMsg(stringa);
            
        }
        
        final void SendMsg(String message) throws IOException{
            if(device.equals("mA")){
                outBF.println(message);
            }else{
                System.out.println("FOR WEB");
                ClientWB.SendMessage(message);
            }
        }

        
        //CLIENT RUN
	public void run()
	{
            try{
                //Init
                //multiplattform IF
                //HANDSHAKING (the wbsocket need this for life)
                System.out.println("try handshake");
               // String datas=inBF.readLine();
                if(ClientWB.HandShake()!=0){
                    System.out.println("websocket device");
                    device="websocket"; // for javascript
                }else{
                    device="mA";
                    System.out.println(device);
                    //for android    
                }
                //ClientWB.HandShake(); // for javascript
                //inizializzazione 
                user utente = null;
                int userstatus=0;
                System.out.println("While");
                //Reading message from client
                while (true){
                    String message; //message Buffer
                    
                    if(device.equals("mA")){
                        message=inBF.readLine();
                        System.out.println(message);
                    }else{
                        message=ClientWB.GetMessage();
                        System.out.println(message);
                    }
                    System.out.println(message);
                    
                    //                           READING DATA
                    //STRING TO JSON OBJECT
                    JSONObject messJson;
                    String type;
                    try{
                        messJson = new JSONObject(message);
                        type=messJson.getString("type");              //TYPE FROM JSON DATA
                        System.out.println("Request Type: "+type);
                    }catch (Exception e){break;}
                   
                    // what type of request?
                    if (type != null){
                       
                    /**------------------------------LOGIN-----------------------------------**/
                    if(type.equals("login-s")){
                        //NICK AND PSW
                        String nick = messJson.getString("nick");
                        String pass = messJson.getString("password");
                        System.out.println(nick);
                        //setup user
                        utente = new user(nick,pass);
                        if (utente.login() != 0){
                            //ClientWB.SendMessage("1"); //debug
                            System.out.println("User: \""+ nick + "\" Connected ");
                            userstatus = 1;
                            JSONObject obj = new JSONObject("{\"type\":\"login-r\",\"r\":1}");
                            SendMsg(obj.toString());
                            
                            String msgB = "{\"type\":\"adduser\",\"user\":\""+utente.username+"\"}";
                            //System.out.println(msgB); //for debug
                            //ALLERT ALL CLIENTS
                            for (connect conn : connects) {
                                try{
                                    if(conn == this){}else{conn.streamout(msgB);}
                                }catch(Exception e){ /**System.out.println(e);**/}
                            }
                        }else{
                            JSONObject obj = new JSONObject("{\"type\":\"login-r\",\"r\":0}");
                            
                            SendMsg(obj.toString());
                            client.close();
                            break;
                        }
                        //System.out.println("User: \""+ nick + "\" Connected");
                    }
                    
                    /**---------------------------------- REGISTRATION------------------------------**/
                    if(type.equals("regg-s")){
                        // Extract Username and PSW from Json String
                        String nick = messJson.getString("nick");
                        String pass = messJson.getString("password");
                        //make new user obj
                        utente = new user(nick,pass);
                        //Check if registration is possible
                        int result = utente.registration();
                        if(result!= 0){
                            //user created
                            System.out.println("User: \""+ nick + "\" Registered");
                            JSONObject obj = new JSONObject("{\"type\":\"regg-r\",\"r\":"+result+"}");
                            SendMsg(obj.toString());
                        }else{
                            //error in registration
                            JSONObject obj = new JSONObject("{\"type\":\"regg-r\",\"r\":"+result+"}");
                            SendMsg(obj.toString());
                        }
                        //break while for closing connection 
                        break;
                        //client.close();
                    }

                                                        //** USER ONLINE LIST **/
                    if(type.equals("usersonline")){
                        //array of users online
                        String[] users;
                        users= utente.usersonline();
                        //**"cars":[ "Ford", "BMW", "Fiat" ] **/
                        String UsersStringJSON="{\"type\":\"users-l\",\"users\":[ ";
                        //FOR USER ONLINE PUT IN THE JSON STRING THE USER
                        int i;
                        for(i=0;i<users.length;i++){
                            System.out.println(users[i]);
                            UsersStringJSON+="\""+users[i]+"\"";
                            if((i+1)==users.length){    
                                
                            }else{
                                UsersStringJSON+=", ";
                            }
                        }
                        UsersStringJSON+=" ]}";
                        //SEND TO CLIENT List of User
                        JSONObject obj = new JSONObject(UsersStringJSON);
                        SendMsg(obj.toString());
                        
                        System.out.println(UsersStringJSON);
                        
                    }
                    
                    
                    //new message request
                    if(type.equals("offline")){
                        //write in the DB the user is offline
                        utente.offline();
                        userstatus =0;
                        //ALLERT ALL CLIENTS
                        String msgB = "{\"type\":\"rmuser\",\"user\":\""+utente.username+"\"}";
                        //broadcast one user OFFLINE to all 
                        for (connect conn : connects) {
                            try{
                                //System.out.println("ooooooooooo");
                                conn.streamout(msgB);
                                }catch(Exception e){ System.out.println(e);}
                                //conn.streamout();
                        }
                        //ClientWB.SendMessage(lastmsg);
                        client.close();
                        break;
                    //System.out.println("User: "+utente.NickName+" Disconnected");
                    }
                    
                    
                    //Messaggio
                    if(type.equals("message")){
                        String castmsg = messJson.getString("data");
                        castmsg = castmsg.replace("\\","\\\\");
                        castmsg = castmsg.replace("\"","\\\"");
                        
                        String msgB="{\"type\":\"message\",\"from\":\""+utente.username+"\",\"data\":\""+castmsg+"\"}";
                        System.out.println("msg "+msgB);
                        //broadcast the message to all
                        for (connect connect : connects) {
                            try{
                            connect.streamout(msgB);
                            //conn.streamout();
                            }catch(Exception e){}
                        }
                            //lastmsg=utente.username+": "+ClientWB.GetMessage();
                        //System.out.println(lastmsg);
                    }
                    
                   }else {break;}
                }
                    
                //System.out.println("message: "+message);
                
                //if User Crash or not press DISCONNECT on client put offline this user
                //if client is one user online
                if(userstatus !=0){
                    utente.offline();
                    
                    userstatus =0;
                        //ALLERT ALL CLIENTS
                        String msgB = "{\"type\":\"rmuser\",\"user\":\""+utente.username+"\"}";
                        for (connect conn : connects) {
                            try{
                                //System.out.println("ooooooooooo");
                                conn.streamout(msgB);
                                }catch(Exception e){ System.out.println(e);}
                                //conn.streamout();
                        }
                }
                
                System.out.println("one OUT!");
                // chiusura del socket
                in.close();
                out.close();
                client.close();
		}
            catch(Exception e){
                    e.printStackTrace();
            }
        }
    }
    
    
    //--------------------------------------------MAIN----------------------------------------------------
    public static void main(String[] args) {
        try{
            ServerSocket server = new ServerSocket(5552);
            // ciclo infinito, in attesa di connessioni
            while(true)
            {
                // chiamata bloccante, in attesa di una nuova connessione
                Socket client = server.accept();
                // la nuova richiesta viene gestita da un thread indipendente, si ripete il ciclo
                connect nuovaConnessione = new connect(client);
                // add for broadcast functions
                connects.add(nuovaConnessione);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
