/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- */

#include <math.h>

#include <delay.h>
#include <packet.h>

#include <packet-stamp.h>
#include <antenna.h>
#include <mobilenode.h>
#include <propagation.h>
#include <wireless-phy.h>
#include <tworayclean.h>


static class TwoRayCleanClass: public TclClass {
public:
	TwoRayCleanClass() : TclClass("Propagation/TwoRayClean") {}
	TclObject* create(int, const char*const*) {
		return (new TwoRayClean);
	}
} class_tworayclean;

TwoRayClean::TwoRayClean() 
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


TwoRayClean::~TwoRayClean()
{
	delete ranVar;
}


double TwoRayClean::Pr(PacketStamp *t, PacketStamp *r, WirelessPhy *ifp)
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
	double dist2 = sqrt(dX * dX + dY * dY + ((Zr+Zt) * (Zr+Zt)));
	double distXY = sqrt(dX * dX + dY * dY);

	// get antenna gain
	double Gt = t->getAntenna()->getTxGain(dX, dY, dZ, lambda);
	double Gr = r->getAntenna()->getRxGain(dX, dY, dZ, lambda);
	if(forcedParam) {
		Gt = pow(10.0,0.4);
		Gr = pow(10.0,0.4);
	}
	
	double er = 1.003;
	double sinTheta = (Zr + Zt) / dist2;
	double cosTheta = distXY/dist2;
	double secTerm = sqrt(er - cosTheta*cosTheta);
	double reflCoeff = (sinTheta - secTerm)/(sinTheta + secTerm);


	double Pt = t->getTxPr();
	if(forcedParam) 
		Pt = pow(10.0,0.4)/1000.0; //4 dBm

	double Pd0 = Pt * Gt / (4 * PI * dist0_ * dist0_);
	double E0 = sqrt(Pd0 * 120 * PI);

	double Etot = E0 * dist0_/dist + reflCoeff * E0 *dist0_/dist2*cos(2*PI/lambda*(dist-dist2));
	double Pr0 = Etot * Etot * Gt * lambda * lambda / (480 * PI *PI);

	double powerLoss_db = ranVar->normal(0.0, std_db_);
	// calculate the receiving power at dist
	double Pr = Pr0 * pow(10.0, powerLoss_db/10.0);
	//dXY ht hr Gt Gr dRefl Pr
	//	fprintf(stderr,"%f %f %f %f %f %f %f %f %f\n",distXY,Zt,Zr,Gt,Gr,reflCoeff,Pr,10.0*log10(Pr0)+30,Pt);
	return Pr;
}

void TwoRayClean::PrTest(double d)
{
	double lambda = 299792458.0/5.9/pow(10.0,9.0);   // wavelength

	double Zt = 1.55, Zr = 1.5;

	double dist = sqrt(d * d + ((Zr-Zt) * (Zr-Zt)));
	double dist2 = sqrt(d * d + ((Zr+Zt) * (Zr+Zt)));
	double distXY = d;

	// get antenna gain
	double Gt = pow(10.0,0.4), Gr = pow(10.0,0.4);
	
	double er = 1.003;
	double sinTheta = (Zr + Zt) / dist2;
	double cosTheta = distXY/dist2;
	double secTerm = sqrt(er - cosTheta*cosTheta);
	double reflCoeff = (sinTheta - secTerm)/(sinTheta + secTerm);

	//	fprintf(stderr,"sin %f cos %f sec %f reflCoeff %f\n",sinTheta,cosTheta,secTerm,reflCoeff);
	double Pt = pow(10.0,0.4)/1000.0;
	double Pd0 = Pt * Gt / (4 * PI * dist0_ * dist0_);
	double E0 = sqrt(Pd0 * 120 * PI);

	double Etot = E0 * dist0_/dist + reflCoeff * E0 *dist0_/dist2*cos(2*PI/lambda*(dist-dist2));
	double Pr0 = Etot * Etot * Gt * lambda * lambda / (480 * PI *PI);
	//fprintf(stderr,"Pt %f Pd0 %f E0 %f Etot %f\n",Pt,Pd0,E0,Etot);
	/*	// calculate receiving power at reference distance
	double Pr0 = Friis(t->getTxPr(), Gt, Gr, lambda, L, dist0_);

	// calculate average power loss predicted by path loss model
	double avg_db;
        if (dist > dist0_) {
            avg_db = -10.0 * pathlossExp_ * log10(dist/dist0_);
        } else {
            avg_db = 0.0;
        }
   
	// get power loss by adding a log-normal random variable (shadowing)
	// the power loss is relative to that at reference distance dist0_
	double powerLoss_db = avg_db + ranVar->normal(0.0, std_db_);
	*/
	double powerLoss_db = ranVar->normal(0.0, std_db_);
	// calculate the receiving power at dist
	double Pr = Pr0 * pow(10.0, powerLoss_db/10.0);
	//dXY ht hr Gt Gr dRefl Pr
	fprintf(stderr,"%f %f %f %f %f %f %f %f %f\n",distXY,Zt,Zr,Gt,Gr,reflCoeff,Pr0,Pr,10.0*log10(Pr0)+30.0);
}

int TwoRayClean::command(int argc, const char* const* argv)
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


double TwoRayClean::getDist(double , double , double , double, double , double , double , double )
{
	return DBL_MAX;
}
