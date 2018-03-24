/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.util.HashSet;

/**
 *
 * @author Cátia
 */
public class City {
    private int id;
    private String name;
    private HashSet<Destination> destinations;

    public City(int id, String name, HashSet<Destination> cityRoutes) {
        this.id = id;
        this.name = name;
        this.destinations = cityRoutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(HashSet<Destination> cityRoutes) {
        this.destinations = cityRoutes;
    }
    
    
}
