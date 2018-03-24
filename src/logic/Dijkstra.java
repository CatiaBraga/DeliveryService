/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.print.attribute.standard.Finishings;
import objects.City;
import objects.Destination;
import objects.DijkstraNode;

/**
 *
 * @author Cátia
 */
public class Dijkstra {
    private HashMap<Integer, City> citiesCache;
    private HashMap<Integer, DijkstraNode> border;
    private HashMap<Integer, DijkstraNode> visited;
    private City firstCity;

    public Dijkstra() {
        
    }
    
    public ArrayList<City> getBestRoute(int origin, int destination, String measure, HashMap<Integer, City> dataFromCache){
    
        border = new HashMap<Integer, DijkstraNode>();
        visited = new HashMap<Integer, DijkstraNode>();
        citiesCache = (HashMap < Integer, City >)dataFromCache.clone();
        
        //remove the direct route so it won't be considered in the best route algorithm
        removeDirectRoute(origin, destination);
        
        ArrayList<City> bestRoute = new ArrayList<City>();
        
        firstCity = citiesCache.get(origin);
        
        //origin is the first to be visited, thus has no parent
        visited.put(firstCity.getId(), new DijkstraNode(firstCity, null, -1));
        
        
        //fetch the first border cities (which are the destinations of the first city)
        Iterator<Destination> iter = firstCity.getDestinations().iterator();
        
        while(iter.hasNext()){
            Destination destinationInBorder = iter.next();
            City destinationFromOrigin = citiesCache.get(destinationInBorder.getId());
            if(measure.equals("time")){
                border.put(destinationInBorder.getId(), new DijkstraNode(destinationFromOrigin, firstCity, destinationInBorder.getTime()));
            }
            else{
                border.put(destinationInBorder.getId(), new DijkstraNode(destinationFromOrigin, firstCity, destinationInBorder.getCost()));
            }
        }
        
        
        boolean found = false;
        
        while(!found && border.size()>0){
            if(border.containsKey(destination)){
                found = true;
                visited.put(destination, border.get(destination));
            }

            else{
                DijkstraNode leastValue = new DijkstraNode(null, null, Integer.MAX_VALUE);
                Iterator<Integer> iterBorder = border.keySet().iterator();
                
                while(iterBorder.hasNext()){
                    DijkstraNode nextDijkstraNode = border.get(iterBorder.next());
                    if(nextDijkstraNode.getMeasure() < leastValue.getMeasure()){
                        leastValue = nextDijkstraNode;
                    }
                }
                
                border.remove(leastValue.getCurrent().getId());
                visited.put(leastValue.getCurrent().getId(), leastValue);
                
                Iterator<Destination> iterDestinationsLeastValue = leastValue.getCurrent().getDestinations().iterator();
                
                while(iterDestinationsLeastValue.hasNext()){
                    Destination soonToBeVisited = iterDestinationsLeastValue.next();
                    //the destination was never visited
                    if(!border.containsKey(soonToBeVisited.getId()) && !visited.containsKey(soonToBeVisited.getId())){
                        DijkstraNode nextToVisit;
                        if(measure.equals("time")){
                            //the current becomes the parent
                            nextToVisit = new DijkstraNode(citiesCache.get(soonToBeVisited.getId()),leastValue.getCurrent(),soonToBeVisited.getTime());
                        }
                        else{
                            nextToVisit = new DijkstraNode(citiesCache.get(soonToBeVisited.getId()),leastValue.getCurrent(),soonToBeVisited.getCost());
                        }
                        
                        border.put(soonToBeVisited.getId(), nextToVisit);
                    }
                }
                
            }
        }
        
        if(found){
            DijkstraNode lastDijkstraNode = visited.get(destination);
            //stop till the first city is reached, the first city doesn't have a parent
            while(lastDijkstraNode.getParent() != null){
                bestRoute.add(0, lastDijkstraNode.getCurrent());
                //from the last city to the first
                lastDijkstraNode = visited.get(lastDijkstraNode.getParent().getId());
            }
            
            //add the origin at last
            bestRoute.add(0, lastDijkstraNode.getCurrent());
            
            return bestRoute;
        }
        
        return null;
    }
    
    private void removeDirectRoute(int origin, int destination){
        
        HashSet<Destination> setDestinations = citiesCache.get(origin).getDestinations();
        
        //time and cost values don't matter
        Destination directDestination = new Destination(destination, -1, -1);
        
        if(setDestinations.contains(directDestination)){
            setDestinations.remove(directDestination);
        }
        
    }
}
