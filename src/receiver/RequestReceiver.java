/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package receiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.Dijkstra;
import objects.City;
import objects.Destination;
import objects.User;
import utils.ConnectionManager;

/**
 *
 * @author Cátia
 */
public class RequestReceiver {
    private HashMap<Integer, City> cities;
    private HashMap<String, User> users;
    private ConnectionManager connManager = new ConnectionManager();
    private boolean init = false;

    public RequestReceiver() {
        
    }

    public void init(){
        
        Cache cache = new Cache();
        
        cities = cache.citiesToCache();
        users = cache.usersToCache();
        init = true;
    }
    
    //CITIES
    
    public String requestCreateCity(String username, String cityName){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }
        
        //check if is already created
        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();
            City c = entry.getValue();

            if(c.getName().equals(cityName)){
                return "Duplicate found";
            }
        }

        int id = -1;

        try(Connection conn = connManager.fetchConnection();){

            try(PreparedStatement stmt = conn.prepareStatement("insert into cities(city_name) values (?)");){
                stmt.setString(1, cityName);

                stmt.executeUpdate();
            }

            //auto-commit
            //conn.commit();

            try(PreparedStatement stmt = conn.prepareStatement("select id from cities where city_name = ?");){
                stmt.setString(1, cityName);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){
                    id = rs.getInt("id");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(id != -1){
            cities.put(id, new City(id, cityName, null));
        }
        else
            return "Cannot fetch auto-generated id";
        
        return "City has been inserted";
    }
    
    public String requestReadCity(String cityName){
        
        if(!init){
            init();
        }
        
        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(cityName)){
                cityOrigin = entry.getValue();
                break;
            }
        }

        if(cityOrigin == null){
            return "City not found";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(cityName);
        sb.append(" - City found");
        
        return sb.toString();
    }
    
    public String requestUpdateCity(String username, String oldCityName, String newCityName){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }
                
        boolean hasDuplicate = false;

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(oldCityName)){
                cityOrigin = entry.getValue();
            }

            if(entry.getValue().getName().equals(newCityName)){
                hasDuplicate = true;
            }

            if(hasDuplicate){
                //stop the search because there's nothing more to do
                break;
            }
        }

        if(hasDuplicate || cityOrigin == null){
            return "City can't be updated";
        }

        try(Connection conn = connManager.fetchConnection();){

            try(PreparedStatement stmt = conn.prepareStatement("update cities set city_name = ? where id = ?");){
                stmt.setString(1, newCityName);
                stmt.setInt(2, cityOrigin.getId());

                stmt.executeUpdate();
            }

            //auto-commit
            //conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        cities.get(cityOrigin.getId()).setName(newCityName);
        
        return "City updated successfully";
    }
    
    public String requestDeleteCity(String username, String cityName){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();
            
            if(entry.getValue().getName().equals(cityName)){
                cityOrigin = entry.getValue();
                break;
            }
        }

        if(cityOrigin == null){
            return "City not found";
        }

        try(Connection conn = connManager.fetchConnection();){

            try(PreparedStatement stmt = conn.prepareStatement("delete from routes where origin = ? or destination = ?");){
                stmt.setInt(1, cityOrigin.getId());
                stmt.setInt(2, cityOrigin.getId());

                stmt.executeUpdate();
            }

            //auto-commit
            //conn.commit();

            try(PreparedStatement stmt = conn.prepareStatement("delete from cities where id = ?");){
                stmt.setInt(1, cityOrigin.getId());

                stmt.executeUpdate();
            }

            //auto-commit
            //conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        Iterator<Map.Entry<Integer, City>> allCities = cities.entrySet().iterator();

        while(allCities.hasNext()){
            Map.Entry<Integer, City> entry = allCities.next();

            Iterator<Destination> routesToDelete = entry.getValue().getDestinations().iterator();

            while(routesToDelete.hasNext()){
                if(routesToDelete.next().getId() == cityOrigin.getId()){
                    routesToDelete.remove();
                }
            }
        }

        cities.remove(cityOrigin.getId());

        return "City has been deleted";
    }

    
    
    //ROUTES
    
    public String requestCreateRoute(String username, String origin, String destination, int time, int cost){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }

        boolean foundOrigin = false;
        boolean foundDestination = false;

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;
        City cityDestination = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(origin)){
                foundOrigin = true;
                cityOrigin = entry.getValue();
            }

            if(entry.getValue().getName().equals(destination)){
                foundDestination = true;
                cityDestination = entry.getValue();
            }

            if(foundOrigin && foundDestination){
                break;
            }
        }

        if(!foundOrigin || !foundDestination || cityOrigin == null || cityDestination == null){
            return "City not found";
        }

        if(cityOrigin.getDestinations() != null && !cityOrigin.getDestinations().isEmpty()){
            Iterator<Destination> findExistingRoute = cityOrigin.getDestinations().iterator();

            while(findExistingRoute.hasNext()){
                if(findExistingRoute.next().getId() == cityDestination.getId()){
                    return "Route already exists, won't be created";
                }
            }
        }
        else{
            //initialize it before insert to avoid NPE
            cityOrigin.setDestinations(new HashSet<Destination>());
        }

        try(Connection conn = connManager.fetchConnection();){

            try(PreparedStatement stmt = conn.prepareStatement("insert into routes(origin, destination, time, cost) "
                    + "values (?,?,?,?)");){
                stmt.setInt(1, cityOrigin.getId());
                stmt.setInt(2, cityDestination.getId());
                stmt.setInt(3, time);
                stmt.setInt(4, cost);

                stmt.executeUpdate();
            }

            //auto-commit
            //conn.commit();
            
        } catch (SQLException ex) {
            Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        Destination toBeInserted = new Destination(cityDestination.getId(), time, cost);

        cityOrigin.getDestinations().add(toBeInserted);

        return "Route inserted successfully";

    }
    
    public String requestReadRoute(String origin, String destination){
        
        if(!init){
            init();
        }
        
        boolean foundOrigin = false;
        boolean foundDestination = false;

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;
        City cityDestination = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(origin)){
                foundOrigin = true;
                cityOrigin = entry.getValue();
            }

            if(entry.getValue().getName().equals(destination)){
                foundDestination = true;
                cityDestination = entry.getValue();
            }

            if(foundOrigin && foundDestination){
                break;
            }
        }

        if(!foundOrigin && !foundDestination || cityOrigin == null || cityDestination == null){
            return "City not found";
        }

        if(cityOrigin.getDestinations() != null && !cityOrigin.getDestinations().isEmpty()){
            Iterator<Destination> findExistingRoute = cityOrigin.getDestinations().iterator();

            while(findExistingRoute.hasNext()){
                Destination checkForDestination = findExistingRoute.next();
                if(checkForDestination.getId() == cityDestination.getId()){

                    StringBuilder sb = new StringBuilder();
                    
                    sb.append(cityOrigin.getName());
                    sb.append(" ");
                    sb.append(cityDestination.getName());
                    sb.append(" Time: ");
                    sb.append(checkForDestination.getTime());
                    sb.append(" Cost: ");
                    sb.append(checkForDestination.getCost());
                    
                    return sb.toString();
                }
            }
        }
        return "Route not found";
    }
    
    public String requestUpdateRoute(String username, String origin, String destination, int time, int cost){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }

        boolean foundOrigin = false;
        boolean foundDestination = false;

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;
        City cityDestination = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(origin)){
                foundOrigin = true;
                cityOrigin = entry.getValue();
            }

            if(entry.getValue().getName().equals(destination)){
                foundDestination = true;
                cityDestination = entry.getValue();
            }

            if(foundOrigin && foundDestination){
                break;
            }
        }

        if(!foundOrigin && !foundDestination || cityOrigin == null || cityDestination == null){
            return "City not found";
        }

        if(cityOrigin.getDestinations() != null && !cityOrigin.getDestinations().isEmpty()){
            Iterator<Destination> findExistingRoute = cityOrigin.getDestinations().iterator();

            while(findExistingRoute.hasNext()){
                Destination checkForDestination = findExistingRoute.next();
                if(checkForDestination.getId() == cityDestination.getId()){

                    try(Connection conn = connManager.fetchConnection();){

                        try(PreparedStatement stmt = conn.prepareStatement("update routes set time=?, cost=? "
                                + "where origin=? and destination=?");){
                            stmt.setInt(1, time);
                            stmt.setInt(2, cost);
                            stmt.setInt(3, cityOrigin.getId());
                            stmt.setInt(4, cityDestination.getId());

                            stmt.executeUpdate();
                        }

                        //auto-commit
                        //conn.commit();
            
                    } catch (SQLException ex) {
                        Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    checkForDestination.setTime(time);
                    checkForDestination.setCost(cost);

                    return "Route updated successfully";
                }
            }
        }

    return "Route not found";
    }
    
    public String requestDeleteRoute(String username, String origin, String destination){
        
        if(!init){
            init();
        }
        
        if(!users.containsKey(username)){
            return "User not found";
        }
        else{
            if(!users.get(username).isAdmin()){
                return "User is not admin. Permission denied.";
            }
        }

        boolean foundOrigin = false;
        boolean foundDestination = false;

        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();

        City cityOrigin = null;
        City cityDestination = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(origin)){
                foundOrigin = true;
                cityOrigin = entry.getValue();
            }

            if(entry.getValue().getName().equals(destination)){
                foundDestination = true;
                cityDestination = entry.getValue();
            }

            if(foundOrigin && foundDestination){
                break;
            }
        }

        if(!foundOrigin && !foundDestination || cityOrigin == null || cityDestination == null){
            return "City not found";
        }

        if(cityOrigin.getDestinations() != null && !cityOrigin.getDestinations().isEmpty()){
            Iterator<Destination> findExistingRoute = cityOrigin.getDestinations().iterator();

            while(findExistingRoute.hasNext()){
                Destination checkForDestination = findExistingRoute.next();
                if(checkForDestination.getId() == cityDestination.getId()){

                    try(Connection conn = connManager.fetchConnection();){

                        try(PreparedStatement stmt = conn.prepareStatement("delete from routes where origin=? and destination=?");){
                            stmt.setInt(1, cityOrigin.getId());
                            stmt.setInt(2, cityDestination.getId());

                            stmt.executeUpdate();
                        }

                        //auto-commit
                        //conn.commit();
            
                    } catch (SQLException ex) {
                        Logger.getLogger(RequestReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    cityOrigin.getDestinations().remove(checkForDestination);

                    return "Route deleted successfully";
                }
            }
        }

        return "Route not found";
    }
    
    //SHORTEST PATH
    
    public String getShortestPath(String origin, String destination, String measure){
        
        if(!init){
            init();
        }
        
        boolean foundOrigin = false;
        boolean foundDestination = false;
        
        Iterator<Map.Entry<Integer, City>> entries = cities.entrySet().iterator();
        
        City cityOrigin = null;
        City cityDestination = null;

        while(entries.hasNext()){
            Map.Entry<Integer, City> entry = entries.next();

            if(entry.getValue().getName().equals(origin)){
                foundOrigin = true;
                cityOrigin = entry.getValue();
            }
            
            if(entry.getValue().getName().equals(destination)){
                foundDestination = true;
                cityDestination = entry.getValue();
            }
            
            if(foundOrigin && foundDestination){
                break;
            }
        }
        
        if(!foundOrigin && !foundDestination || cityOrigin == null || cityDestination == null){
            return "City not found";
        }
        
        if(!measure.equals("time") && !measure.equals("cost")){
            return "Measure not suitable";
        }
        
        ArrayList<City> getPath = new Dijkstra().getBestRoute(cityOrigin.getId(), cityDestination.getId(), measure, cities);
        
        if(getPath == null){
            return "Can't find a route between the cities";
        }
        else{
            StringBuilder sb = new StringBuilder();

            Iterator<City> iterPath = getPath.iterator();
            
            while(iterPath.hasNext()){
                sb.append(iterPath.next().getName());
                sb.append(" ");
            }
            
            return sb.toString();
        }
    }
}
