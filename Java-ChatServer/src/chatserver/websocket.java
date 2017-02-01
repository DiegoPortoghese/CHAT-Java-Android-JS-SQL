/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Utente
 */
public class websocket {
    InputStream in;
    OutputStream out;
    
    public websocket(InputStream in_,OutputStream out_){
        in =in_;
        out=out_;
        
    }
    
    //MESSAGE DECODING
    public String GetMessage() throws IOException{
            String DecodeMsg = null;
            int len = 0;            
            byte[] b = new byte[200];
            //rawIn is a Socket.getInputStream();
                len = in.read(b);
                if(len!=-1){
                    byte rLength = 0;
                    int rMaskIndex = 2;
                    int rDataStart = 0;
                    //b[0] is always text in my case so no need to check;
                    byte dataM = b[1];
                    byte op = (byte) 127;
                    rLength = (byte) (dataM & op);
                    if(rLength==(byte)126) rMaskIndex=4;
                    if(rLength==(byte)127) rMaskIndex=10;
                    byte[] masks = new byte[4];
                    int j=0;
                    int i=0;
                    for(i=rMaskIndex;i<(rMaskIndex+4);i++){
                        masks[j] = b[i];
                        j++;
                    }
                    rDataStart = rMaskIndex + 4;
                    int messLen = len - rDataStart;
                    byte[] message = new byte[messLen];
                    for(i=rDataStart, j=0; i<len; i++, j++){
                        message[j] = (byte) (b[i] ^ masks[j % 4]);
                    }
                    DecodeMsg=new String(message); 
                    //parseMessage(new String(b));
                    b = new byte[200];
                }
                return DecodeMsg;
        }
    //HANDSHKAE HTTP
    public int HandShake() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException{
                
                Scanner s = new Scanner(in,"UTF-8");
                Scanner x= s;
                //String dataR=s.next();
                try{
                String dataR=x.findInLine("mA");
                System.out.println(dataR);
                if(dataR.equals("mA")){return 0;}
                }catch(Exception e){}
                
                String data=s.useDelimiter("\\r\\n\\r\\n").next(); 
            //data = data_;//      
                
                
                System.out.println("2222");
                
                System.out.println("data:"+data);
                
                //HANDSHAKE TIME
                Matcher get = Pattern.compile("^GET").matcher(data);
                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + DatatypeConverter
                            .printBase64Binary(
                                    MessageDigest
                                    .getInstance("SHA-1")
                                    .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                            .getBytes("UTF-8")))
                            + "\r\n\r\n")
                            .getBytes("UTF-8");
                    out.write(response, 0, response.length);
                    return 1;
                }
                return 0;
    }
    
    
    public void SendMessage(String mess) throws IOException{
        byte[] rawData = mess.getBytes();
        int frameCount  = 0;
        byte[] frame = new byte[10];
        frame[0] = (byte) 129;
        if(rawData.length <= 125){
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        }else if(rawData.length >= 126 && rawData.length <= 65535){
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte)((len >> 8 ) & (byte)255);
            frame[3] = (byte)(len & (byte)255); 
            frameCount = 4;
        }else{
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte)((len >> 56 ) & (byte)255);
            frame[3] = (byte)((len >> 48 ) & (byte)255);
            frame[4] = (byte)((len >> 40 ) & (byte)255);
            frame[5] = (byte)((len >> 32 ) & (byte)255);
            frame[6] = (byte)((len >> 24 ) & (byte)255);
            frame[7] = (byte)((len >> 16 ) & (byte)255);
            frame[8] = (byte)((len >> 8 ) & (byte)255);
            frame[9] = (byte)(len & (byte)255);
            frameCount = 10;
        }
        int bLength = frameCount + rawData.length;
        byte[] reply = new byte[bLength];
        int bLim = 0;
        for(int i=0; i<frameCount;i++){
            reply[bLim] = frame[i];
            bLim++;
        }
        for(int i=0; i<rawData.length;i++){
            reply[bLim] = rawData[i];
            bLim++;
        }
        out.write(reply);
        out.flush();

    }
    
}
