/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package receiver;

/**
 *
 * @author Cátia
 */
public class mainClass {
    
    //FOR DEBUG PURPOSES ONLY
    public static void main(String[] args) {
        
        RequestReceiver rec = new RequestReceiver();
        
        System.out.println(rec.getShortestPath("Amadora", "Bragança", "time"));
    }
}
