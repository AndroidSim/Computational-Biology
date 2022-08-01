
#include <math.h>
#include <iostream>
#include "ConvectionDispersion.h"

using namespace std;


double ConvectionDispersion::ecd(double z, double t) {
  // Eq (11), = inv laplace Eq (10)  
  double c_f_p = 0;

  double first_term = (this->M/this->Q) * (z * sqrt(this->T) / (2 * this->L)) * exp( (z / (2 * this->D * this->L)) - (t * (this->k_2 + this->k_e))   );

  // Now, integrate the exp*j_0 function, and find derivative with respect to t.
  // Essentially, the expression is (d/dt) INTEGRAL_0^t  {  f(u,t) } du
  //   Numerically, this is (INTEGRAL_t^{t+delta} { f(u,t) } du) / delta
  //   So, choose a small delta (~.01), and then choose even smaller du (~.0005)
  double delta = .01;
  double du = .0005;
  double int_sum = 0;
  for (double u_int = (t + du); u_int < (t + delta); u_int += du) {
    double int_value = this->integrand(z, u_int, t) * du;
    int_sum += int_value;
  }
  double second_term = (int_sum / delta); 

  cout << "DEBUG: Flux concentration for z = " << z << ", t = " << t
       << ": " << (first_term * second_term)
       << " ( = " << first_term << " * " << second_term
       << " = " << first_term << " * (" << int_sum << " / " << delta << ") )"
       << endl;

  return (first_term * second_term);
}



double ConvectionDispersion::integrand(double z, double u, double t) {
  double numerator = exp((u * (this->k_2 + this->k_e + this->k_1)) - (((z * z) + pow(u * this->v, 2) )/(4 * this->D * u * z * this->v)));
  double denominator = sqrt(this->D * PI * pow(u, 3));
  double bessel_substrate = 2 * sqrt(this->k_2 * this->k_2 * ((u*u) - (t * u)));
  double bessel_value = j0(bessel_substrate);

  cout << "DEBUG: Integrand for z = " << z << ", u = " << u << ", t = " << t
       << ": " << (numerator * bessel_value / denominator)
       << " ( = " << numerator << " * " << bessel_value
                  << " / " << denominator
       << ", where bessel = j0(" << bessel_substrate << ")"
       << endl;

  return (numerator * bessel_value / denominator);
}

