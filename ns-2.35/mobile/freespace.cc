/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- */

#include <math.h>

#include <delay.h>
#include <packet.h>

#include <packet-stamp.h>
#include <antenna.h>
#include <mobilenode.h>
#include <propagation.h>
#include <wireless-phy.h>
#include <freespace.h>


static class FreeSpaceCleanClass: public TclClass {
public:
	FreeSpaceCleanClass() : TclClass("Propagation/FreeSpaceClean") {}
	TclObject* create(int, const char*const*) {
		return (new FreeSpaceClean);
	}
} class_freespaceclean;

FreeSpaceClean::FreeSpaceClean() 
{
	bind("std_db_", &std_db_);
	bind("dist0_", &dist0_);
	bind("seed_", &seed_);
	
	ranVar = new RNG;
	ranVar->set_seed(RNG::PREDEF_SEED_SOURCE, seed_);

	/*	for(int i=2; i <= 400; i+=2) {
		PrTest((double)i);
		} */
}


FreeSpaceClean::~FreeSpaceClean()
{
	delete ranVar;
}


double FreeSpaceClean::Pr(PacketStamp *t, PacketStamp *r, WirelessPhy *ifp)
{
	bool forcedParam = true;

	double L = ifp->getL();		// system loss
	double lambda = ifp->getLambda();   // wavelength
	
	double Xt, Yt, Zt;		// loc of transmitter
	double Xr, Yr, Zr;		// loc of receiver

	t->getNode()->getLoc(&Xt, &Yt, &Zt);
	r->getNode()->getLoc(&Xr, &Yr, &Zr);

	// Is antenna position relative to node position?
	Xr += r->getAntenna()->getX();
	Yr += r->getAntenna()->getY();
	Zr += r->getAntenna()->getZ();
	Xt += t->getAntenna()->getX();
	Yt += t->getAntenna()->getY();
	Zt += t->getAntenna()->getZ();

	if(forcedParam) {
		Zt = 1.55;
		Zr = 1.5;
	}

	double dX = Xr - Xt;
	double dY = Yr - Yt;
	double dZ = Zr - Zt;
	double dist = sqrt(dX * dX + dY * dY + dZ * dZ);

	// get antenna gain
	double Gt = t->getAntenna()->getTxGain(dX, dY, dZ, lambda);
	double Gr = r->getAntenna()->getRxGain(dX, dY, dZ, lambda);
	if(forcedParam) {
		Gt = pow(10.0,0.4);
		Gr = pow(10.0,0.4);
	}
	

	double Pt = t->getTxPr();
	if(forcedParam) 
		Pt = pow(10.0,0.4)/1000.0; //4 dBm

	double Pr0 = Pt * Gt * Gr * lambda * lambda/ (16 * PI * PI * dist *dist);

	double powerLoss_db = ranVar->normal(0.0, std_db_);
	// calculate the receiving power at dist
	double Pr = Pr0 * pow(10.0, powerLoss_db/10.0);
	//dXY ht hr Gt Gr dRefl Pr
	//	fprintf(stderr,"%f %f %f %f %f %f %f %f %f\n",distXY,Zt,Zr,Gt,Gr,reflCoeff,Pr,10.0*log10(Pr0)+30,Pt);
	return Pr;
}


int FreeSpaceClean::command(int argc, const char* const* argv)
{
	if (argc == 4) {
		if (strcmp(argv[1], "seed") == 0) {
			int s = atoi(argv[3]);
			if (strcmp(argv[2], "raw") == 0) {
				ranVar->set_seed(RNG::RAW_SEED_SOURCE, s);
			} else if (strcmp(argv[2], "predef") == 0) {
				ranVar->set_seed(RNG::PREDEF_SEED_SOURCE, s);
				// s is the index in predefined seed array
				// 0 <= s < 64
			} else if (strcmp(argv[2], "heuristic") == 0) {
				ranVar->set_seed(RNG::HEURISTIC_SEED_SOURCE, 0);
			}
			return(TCL_OK);
		}
	}
	
	return Propagation::command(argc, argv);
}


double FreeSpaceClean::getDist(double , double , double , double, double , double , double , double )
{
	return DBL_MAX;
}

