/*
 * Copyright 2003-2014 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.model;

/**
 *
 * @author gepr
 */
public class CompartmentType {
  public static final CompartmentType GRID = new CompartmentType("GRID");
  public static final CompartmentType POOL = new CompartmentType("POOL");
  public static final CompartmentType QUEUE = new CompartmentType("QUEUE");

   public String name = null;
   public CompartmentType(String n) {
     name = n;
   }
}
