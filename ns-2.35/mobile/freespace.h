/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- */

#ifndef freespaceclean_h
#define freespaceclean_h

#include <packet-stamp.h>
#include <wireless-phy.h>
#include <propagation.h>
#include <rng.h>
#include <float.h>

class FreeSpaceClean : public Propagation {
public:
	FreeSpaceClean();
	~FreeSpaceClean();
	virtual double Pr(PacketStamp *tx, PacketStamp *rx, WirelessPhy *ifp);
	virtual double getDist(double Pr, double Pt, double Gt, double Gr,
			       double hr, double ht, double L, double lambda);
	virtual int command(int argc, const char*const* argv);

protected:
	RNG *ranVar;	// random number generator for normal distribution
	
	double std_db_;		// shadowing deviation (dB),
	double dist0_;	// close-in reference distance
	int seed_;	// seed for random number generator
};

#endif

