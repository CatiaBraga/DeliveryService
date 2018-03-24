/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

/**
 *
 * @author Cátia
 */
public class DijkstraNode {
    private City current;
    private City parent;
    int measure;

    public DijkstraNode(City current, City parent, int measure) {
        this.current = current;
        this.parent = parent;
        this.measure = measure;
    }

    public City getCurrent() {
        return current;
    }

    public void setCurrent(City current) {
        this.current = current;
    }

    public City getParent() {
        return parent;
    }

    public void setParent(City parent) {
        this.parent = parent;
    }

    public int getMeasure() {
        return measure;
    }

    public void setMeasure(int measure) {
        this.measure = measure;
    }
    
}
