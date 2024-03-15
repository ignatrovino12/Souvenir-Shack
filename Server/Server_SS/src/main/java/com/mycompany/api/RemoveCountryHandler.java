/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.database.CountriesDAO;
import com.mycompany.database.SessionsDAO;
import com.mycompany.database.VisitedCountriesDAO;
import com.mycompany.objects.Country;
import com.mycompany.objects.Session;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 *
 * @author ignat
 */
public class RemoveCountryHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
       HandlerCommander hc= new HandlerCommander();
        // Set CORS headers
        hc.setCORS(exchange);
        
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;

            StringBuilder requestBody = new StringBuilder();

            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            String request = requestBody.toString();

            // Retrieve the values of "country" and "session" from the JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonrequest = objectMapper.readTree(request);

            String country = jsonrequest.get("country").asText();
            String sessionx = jsonrequest.get("session").asText();
            int equalsIndex = sessionx.indexOf("=");
            String session = sessionx.substring(equalsIndex + 1);

            //delete in the database if the country exists
            try {
                CountriesDAO c = new CountriesDAO();
                if (c.CountryExists(country) == true) {
                    SessionsDAO s = new SessionsDAO();
                    VisitedCountriesDAO v = new VisitedCountriesDAO();
                    Session sesiune = s.findBySession(session);
                    Country tara = c.findByName(country);

                    //association already in database
                    if (v.AssociationExists(sesiune.getId_user(), tara.getId()) == true) {
                        v.deleteAssociation(sesiune.getId_user(), tara.getId());
                        hc.sendResponse(exchange, "true", "Country removed", 200);

                    } //otherwise error
                    else {
                    hc.sendResponse(exchange, "false", "Country is not marked as visited", 200);

                    }

                } else {
                    hc.sendResponse(exchange, "false", "Invalid country", 200);
                }

            } catch (SQLException ex) {
                System.out.println(ex);
            }

        } else {
            hc.sendResponse(exchange, "false", "Invalid request method", 405);
        }

       hc.closeconnection();

    }

   
}
