#ifndef FILE_CONVECTION_DISPERSION
#define FILE_CONVECTION_DISPERSION

#include "Model.h"
#include <complex>

class ConvectionDispersion : public Model {
 private:
  double L;
  double v;
 public:
  double ecd(double z, double t);
  double integrand(double z, double u, double t);

  void setL(double L) { this->L = L; }
  double getL() { return this->L; }
  void setV(double v) { this->v = v; }
  double getV() { return this->v; }
};

#endif
