/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**         
 * TABELLA DEL DATABASE UTENTI
 * 
CREATE TABLE `UTENTI` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `email` varchar(50) NOT NULL,          //EMAIL NOW NOT is USED but IN THE FUTURE I don't KNOW
  `status` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/

public class user {
    
    public String username;
    private String password;
    private String email;
    private int id = 0;
    
    private String connectionString="jdbc:mysql://"
            +"localhost:3306/" //host
            +"chatserver" //db
            +"?user=root" //id
            +"&password=asdasd123" //psw
            +"&useSSL=True"; //ssl connection = true
    
    //COSTRUCTOR
    public user(String username_,String Password_){
            username=username_;
            password=Password_;
            
    }
    
    
    //-------------------------------------LOGIN-QUERY---------------------------------------
    public int login(){
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            // Query SEARCH USER WITH PASSWORD
            ResultSet chs = ch.executeQuery("select * from utenti");
            
            PreparedStatement prepar = connection.prepareStatement("select * from utenti where username=? and password=?;");
            prepar.setString(1, username);
            prepar.setString(2, password);
            ResultSet updateST_e = prepar.executeQuery();
            
            
            //PreparedStatement prepared = connection.prepareStatement("UPDATE utenti SET status=1 WHERE id=?");

            int found = 0;
            while (chs.next()) {
                
                int idr=chs.getInt("id");
                String username_=chs.getString("username");
                String password_=chs.getString("password");
                //System.out.println("User: "+ username_ + " "+password_+ "  id :"+ idr);
                //s
                if(username_.equals(username) && password_.equals(password) ){
                    //remember ID
                    id=idr;
                    System.out.println("User: "+ username + " Logged "+ "id :"+ id);
                    found =1 ;
                }else{
                    //user not found or password is incorrect
                    found = 0;
                }
            }
            if(updateST_e.next()==true){
                System.out.print("LOGIN");
            }else{
                return 0;
            }
            //check if the query find the user if not (found=0) return 0;
            //if(found==0){return 0;} // se non lo trova (quindi found rimane 0) RETURN 0

            /** SET STATUS TO 1 (is online) **/
            PreparedStatement prepared = connection.prepareStatement("UPDATE utenti SET status=1 WHERE id=?");
            prepared.setInt(1, id);
            //ex query
            prepared.executeUpdate();

            // Gestione degli errori
            }catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if (connection != null)
                        connection.close();
                } catch (SQLException e) {
                    // gestione errore in chiusura
                }
        }
        return 1;
    }
    
    
    
    //------------------------------ONLINE_USER_DB_RQST---------------------------------
    public String[] usersonline(){
        //array of user
        ArrayList list = new ArrayList();
        String[] strArray=null;
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            //coonessione al DB
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from utenti where status=1");
            while (chs.next()) {
                String user= chs.getString("username");
                list.add(user); 
                
            }
            strArray = new String[ list.size() ];
            
            for( int j = 0; j < strArray.length; j++ )
                strArray[ j ] = list.get( j ).toString();
            
            } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return strArray;
    }
    
    
    //---------------------------REGISTRATION-QUERY----------------------------
    public int registration(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            //connessione al DB
            connection = DriverManager.getConnection(connectionString);
            
            //Quering if the username already exist
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from utenti");
            while (chs.next()) {
                    if(chs.getString("username").equals(username))
                    {
                        System.out.println("ERRORE:"+chs.getString("username"));
                        return 0;
                    }else{
                        
                        }
            }
            
            PreparedStatement prepared = connection.prepareStatement("insert into utenti (username, password, email, status) values (?,?,?,?)");
            prepared.setString(1, username);
            prepared.setString(2, password);
            prepared.setString(3, "");
            prepared.setInt(4, 0);
            //ex query for add the user
            int updateST_done = prepared.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
    return 1;
    }
    
    
    //------------------------------SET-OFFLINE----------------------
    public void offline(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
        connection = DriverManager.getConnection(connectionString);
        
                /** SET STATUS TO 0 (is offline) **/
        
        
        PreparedStatement updateST = connection.prepareStatement("update`utenti` set `status` = '0'  where `username` = '"+username+"'");
        int updateST_done = updateST.executeUpdate();
        
        }catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
    }
    
    
    
    
}