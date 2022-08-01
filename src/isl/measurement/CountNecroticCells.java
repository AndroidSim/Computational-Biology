/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import isl.model.cell.Hepatocyte;
import isl.model.ISL;

import java.util.Map;

public class CountNecroticCells extends EventObs {
  private static final long serialVersionUID = 3849153245834540132L;
  
  public CountNecroticCells(ISL tgt, String outputFile) {
    super(tgt, outputFile);
  }
  
  @Override
  public void setHeaders() {
    headers.add("Time");
    super.setHeaders();
  }

  protected Map<Hepatocyte, Double> getTgtData(isl.model.SS ss) {
    return ss.necroticHepatocytes;
  }
}
