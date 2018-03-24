/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cátia
 */
public class ConnectionManager {
    private String connectionString = "jdbc:mysql://localhost:3306/deliveryservice";
    private String username = "root";
    private String password = "root";

    public ConnectionManager() {
    }
    
    public Connection fetchConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            
            return DriverManager.getConnection(connectionString, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
