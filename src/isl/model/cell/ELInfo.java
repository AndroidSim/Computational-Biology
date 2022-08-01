/*
 * ELInfo.java
 * 
 */
package isl.model.cell;

/**
 *
 * @author Brenden
 */
public interface ELInfo {
    
    public int getELThresh();
    public double getELRate();
    public double getELResponse();
    public java.util.Map<String,Double> getELInhibTypes();
       
}