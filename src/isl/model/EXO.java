/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

public /*strictfp*/ class EXO extends MobileObject implements bsg.util.TypeString {

    public int srcID = -1;

    public int getvHPCid() {
        return srcID;
    }
    public int srcProp = -1;

    public int getSrcProp() {
        return srcProp;
    }
    public String tgtProp = null;

    public String getTgtProp() {
        return tgtProp;
    }

    public EXO(MobileObjectType t, int v, int g, String m) {
        super(t);
        srcID = v;
        srcProp = g;
        tgtProp = m;
    }
}