/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package receiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.City;
import objects.Destination;
import objects.User;
import utils.ConnectionManager;

/**
 *
 * @author Cátia
 */
public class Cache {
    private ConnectionManager connManager;
            
    public HashMap<Integer, City> citiesToCache(){
        
        HashMap<Integer, City> citiesCache = new HashMap<Integer, City>();
        connManager = new ConnectionManager();
        
        try(Connection conn = connManager.fetchConnection();){
                
            try(PreparedStatement stmt = conn.prepareStatement("select id, city_name from cities");){
                
                ResultSet rs = stmt.executeQuery();
                
                while(rs.next()){

                    int id = rs.getInt("id");
                    String cityName = rs.getString("city_name");
                    
                    if(!citiesCache.containsKey(id)){
                        City c = new City(id, cityName, null);
                        citiesCache.put(id, c);
                    }
                }
            }
            
            try(PreparedStatement stmt = conn.prepareStatement("select origin, destination, time, cost from routes");){
                
                ResultSet rs = stmt.executeQuery();
                
                while(rs.next()){
                    
                    int origin = rs.getInt("origin");
                    int destination = rs.getInt("destination");
                    int time = rs.getInt("time");
                    int cost = rs.getInt("cost");
                    
                    Destination d = new Destination(destination, time, cost);
                    
                    HashSet<Destination> setDestination = citiesCache.get(origin).getDestinations();
                    
                    if(setDestination == null || setDestination.isEmpty()){
                        citiesCache.get(origin).setDestinations(new HashSet<Destination>());
                    }
                    
                    citiesCache.get(origin).getDestinations().add(d);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return citiesCache;
    }
    
    public HashMap<String, User> usersToCache(){
        
        HashMap<String, User> usersCache = new HashMap<String, User>();
        connManager = new ConnectionManager();
        
        try(Connection conn = connManager.fetchConnection();){
                
            try(PreparedStatement stmt = conn.prepareStatement("select username, is_admin from users");){
                
                ResultSet rs = stmt.executeQuery();
                
                while(rs.next()){

                    String username = rs.getString("username");
                    int is_admin = rs.getInt("is_admin");
                    boolean admin;
                    
                    if(is_admin == 1){
                        admin = true;
                    }
                    else{
                        admin = false;
                    }
                    User u = new User(username, admin);
                    
                    usersCache.put(username, u);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return usersCache;
    }
    
}
