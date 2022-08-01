/*
 * IPRL RefModel liver_model.cc -- main function group
 *
 * Copyright 2003-2005 - Regents of the University of California, San Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "Model.h"
#include "ConvectionDispersion.h"
#include "ExtendedConvectionDispersion.h"

#define MODEL_CONVECTION_DISPERSION 0
#define MODEL_EXTENDED_CONVECTION_DISPERSION 1

/**
 * C++ Version of Traditional and Extended Convection Dispersion Model
 *     6/18/02 - 7/13/02. Dev Nag, Tempus Dictum.
 *     
 *  Usage:
 *    ./liver_model -model traditional -K1 <k1> -L <L> -time_start 0 -time_end 15 ....   
 *                    or
 *    ./liver_model -model extended -K1 <k1> -A <a> -time_start 0 -time_end 20 ...
 * 
 *  Tab-delimited data is printed to stdout. 
 */

// Model
int model_type = MODEL_EXTENDED_CONVECTION_DISPERSION;

// Model parameters: Taken from caption of Figure 4.
double k1 = .03; // ecd 
double k2 = .01; // ecd
double ke = .1; //ecd
double D = 0.265;    // ecd  D_N: .265
double T = 6.35;   //  ecd T: 6.35 sec
double M = 1.0; // ecd bolus mass
double Q = 0.312; // ecd perfusate flow  30ml/min = .5ml/sec

double p = 0.858;
double D2 = 3.77;
double T2 = 35.4;

// Traditional CD parameters
double L = 7; // length -- chosen to visually fit Figure 4A for the traditional model
double V = 1; // axial perfusate velocity
double z = L;

// Extended CD parameters
double a = .00654; // ecd f/V1 (f is flow between 1st and 2nd vascular compartments; V1 & V2 are compartments volumes)
double b = .0248; // ecd f/V2 

// Run-time boundary parameters
double time_start = 7; // ecd
double time_now = 0; // ecd
double time_end = 60; // ecd
double time_interval = .1; // ecd

// running outflow concentration value
double outflow_conc = 0xffffffff;
double outflow_conc_extracted = 0xffffffff;

Model *model = NULL;

extern "C" {

int ecd_init(double st, double te, double tint, double k1, double k2, 
             double ke, double D, double T, double M, double Q, 
             double a, double b)
{
  time_start = st;
  time_end = te;
  time_interval = tint;

  time_now = time_start;
  outflow_conc = 0.0;

  if (model_type == MODEL_CONVECTION_DISPERSION) {
    model = new ConvectionDispersion();
  } else {
    model = new ExtendedConvectionDispersion();
  }

  // set model generic params
  model->setK1(k1);
  model->setK2(k2);
  model->setKE(ke);
  model->setD(D);
  model->setT(T);
  model->setM(M);
  model->setQ(Q);

  //model->setP(p);
  //model->setD2(D2);
  //model->setT2(T2);

  // Set model-specific parameters
  if (model_type == MODEL_CONVECTION_DISPERSION) {
    ((ConvectionDispersion *)model)->setL(L);
    ((ConvectionDispersion *)model)->setV(V);
  } else {
    ((ExtendedConvectionDispersion *)model)->setA(a);
    ((ExtendedConvectionDispersion *)model)->setB(b);
  }

  return 0;
}

int ecd_destroy()
{
  delete model;
  return 0;
}

int ecd_step()
{
  // Run over time
  if (time_now < time_start) {
    outflow_conc = 0.0;
  } else
  {
    double time_diff = time_now - time_start;
    
    model->setExtracting(false);
    outflow_conc = model->ecd(z, time_diff);
    
    model->setExtracting(true);
    outflow_conc_extracted = model->ecd(z,time_diff);
  }

  time_now += time_interval;
  return 0;
}

double get_current_cout() {
  return outflow_conc;
}

double get_current_extracted_cout() {
  return outflow_conc_extracted;
}

double get_current_time() {
  return time_now - time_interval;  // because of where we do the integration
}

}
