#ifndef FILE_EXTENDED_CONVECTION_DISPERSION
#define FILE_EXTENDED_CONVECTION_DISPERSION

#include <iostream>

#define INTEGRATION_DW .01
#define INTEGRATION_INFINITY 10

#include "Model.h"

class ExtendedConvectionDispersion : public Model {
 private:
  std::complex <double> a;
  std::complex <double> b;
 public:
  double ecd(double z, double t);
  double ecd(double t);
  void setA(std::complex<double> a) { this->a = a; }
  void setB(std::complex<double> b) { this->b = b; }

  std::complex<double> l_ecd(std::complex <double> s); // laplace version of ext c-d model
  std::complex<double> g(std::complex<double> s);
};

#endif
