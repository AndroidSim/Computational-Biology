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
public interface LiverNodes
{
  public void stepPhysics();

  public void stepBioChem();

  public double getInletCapPerMobileObject();

  public String describe();

}
