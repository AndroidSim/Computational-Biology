/*
 * IPRL RefModel Model -- abstract superclass of the CD and ECD models.
 *
 * Copyright 2003-2005 - Regents of the University of California, San Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
#ifndef FILE_MODEL_H
#define FILE_MODEL_H

#define PI 3.14159265358979323

#include <iostream>
#include <complex>

class Model {
 protected:
  double D;
  double k_1;
  double k_2;
  double k_e;
  double T;
  
  double M;
  double Q;

  double p;
  double D2;
  double T2;

  bool   extracting;
 public:
  Model() {

  }
  virtual ~Model() {

  }
  
  void setD(double D) { this->D = D; }
  double getD() { return this->D; }
  void setK1(double k_1) { this->k_1 = k_1; }
  double getK1() { return this->k_1; }
  void setK2(double k_2) { this->k_2 = k_2; }
  double getK2() { return this->k_2; }
  void setKE(double k_e) { this->k_e = k_e; }
  double getKE() { return this->k_e; }
  void setT(double T) { this->T = T; }
  double getT() { return this->T; }

  void setM(double M) { this->M = M; }
  void setQ(double Q) { this->Q = Q; }

  void setP(double p) { this->p = p; }
  void setD2(double d2) { this->D2 = d2; }
  void setT2(double t2) { this->T2 = t2; }

  virtual double ecd(double z, double t) = 0;

  void setExtracting(bool on) { this->extracting = on; }
};

#endif
