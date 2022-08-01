extern int ecd_init(double st, double te, double tint, 
		    double k1, double k2, double ke, double D, 
		    double T, double M, double Q, double a, 
		    double b);
extern int ecd_destroy();
extern int ecd_step();
extern double get_current_cout();
extern double get_current_extracted_cout();
extern double get_current_time();
