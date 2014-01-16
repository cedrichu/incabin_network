/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- */

#ifndef tworayclean_h
#define tworayclean_h

#include <packet-stamp.h>
#include <wireless-phy.h>
#include <propagation.h>
#include <rng.h>
#include <float.h>

class TwoRayClean : public Propagation {
public:
	TwoRayClean();
	~TwoRayClean();
	virtual double Pr(PacketStamp *tx, PacketStamp *rx, WirelessPhy *ifp);
	virtual double getDist(double Pr, double Pt, double Gt, double Gr,
			       double hr, double ht, double L, double lambda);
	virtual int command(int argc, const char*const* argv);
	void PrTest(double);
protected:
	RNG *ranVar;	// random number generator for normal distribution
	
	double std_db_;		// shadowing deviation (dB),
	double dist0_;	// close-in reference distance
	int seed_;	// seed for random number generator
};

#endif

