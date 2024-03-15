/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.api;

import com.mycompany.database.Connection_Database;
import com.mycompany.database.SessionsDAO;
import com.mycompany.database.UsersDAO;
import com.mycompany.objects.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ignat
 */
public class SignupHandler implements HttpHandler {

    public SignupHandler() {

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

       HandlerCommander hc= new HandlerCommander();
        // Set CORS headers
        hc.setCORS(exchange);

        if ("POST".equals(exchange.getRequestMethod())) {

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;

            StringBuilder usernameBody = new StringBuilder();
            StringBuilder passwordBody = new StringBuilder();
            StringBuilder password_confirmBody = new StringBuilder();
            int line_number = 0;

            //Extract the data
            while ((line = br.readLine()) != null) {
                line_number++;

                if (line_number == 4) {
                    usernameBody.append(line);
                }
                if (line_number == 8) {
                    passwordBody.append(line);
                }
                if (line_number == 12) {
                    password_confirmBody.append(line);
                }

            }
            br.close();

            String username = usernameBody.toString();
            String password = passwordBody.toString();
            String password_confirm = password_confirmBody.toString();
         

            // Verify login credentials
            
            // login credentials are bad
            if (username.length() < 6 || username.length() > 12 || password.length() < 6 || password.length() > 12) {

                hc.sendResponse(exchange, "false", "Invalid credentials", 401);

            } else {
                //check in database if user exists or not, if not create it

                var u = new UsersDAO();

                try {
                    //username already exists
                    if (u.UsernameExists(username) == true) {
                        hc.sendResponse(exchange, "false", "User already exists", 403);
                    } //login credentials are good
                    else {
                        //password and password_confirm are the same
                        if (password.equals(password_confirm)) {
                            
                            u.create(username, password);                           
                            //create Session
                            User user=u.findByUsername(username);
                            String sesiune=createSession(user.getId());
                            
                            hc.sendResponse(exchange, "true", sesiune, 200);
                            
                           
                        }
                        else  hc.sendResponse(exchange, "false", "Passwords do not match", 401);

                    }

                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            }

        } else {
            hc.sendResponse(exchange, "false", "Invalid request method", 405);
        }
        
       
        hc.closeconnection();

    }

 
      private String createSession(int id_user) {
        
       String id_session= UUID.randomUUID().toString();
       SessionsDAO s = new SessionsDAO();
        try {
            s.create(id_session, id_user);
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        
     return id_session;
    }
}
