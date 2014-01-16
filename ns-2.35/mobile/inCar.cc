/*
 * Copyright (C) 2007 
 * Mercedes-Benz Research & Development North America, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,
 * version 2, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 *
 * The copyright of this module includes the following
 * linking-with-specific-other-licenses addition:
 *
 * In addition, as a special exception, the copyright holders of
 * this module give you permission to combine (via static or
 * dynamic linking) this module with free software programs or
 * libraries that are released under the GNU LGPL and with code
 * included in the standard release of ns-2 under the Apache 2.0
 * license or under otherwise-compatible licenses with advertising
 * requirements (or modified versions of such code, with unchanged
 * license).  You may copy and distribute such a system following the
 * terms of the GNU GPL for this module and the licenses of the
 * other code concerned, provided that you include the source code of
 * that other code when and as the GNU GPL requires distribution of
 * source code.
 *
 * Note that people who make modified versions of this module
 * are not obligated to grant this special exception for their
 * modified versions; it is their choice whether to do so.  The GNU
 * General Public License gives permission to release a modified
 * version without this exception; this exception also makes it
 * possible to release a modified version which carries forward this
 * exception.
 *
 */

/*
 * This code was developed by:
 * 
 * Qi Chen                 : qi.chen@daimler.com
 * Heiko Mangold            
 * Daniel Jiang            : daniel.jiang@daimler.com
 * 
 * For further information see: 
 * http://dsn.tm.uni-karlsruhe.de/english/Overhaul_NS-2.php
 */

#include <math.h>
#include <delay.h>
#include <packet.h>

#include <iostream>
#include <float.h>
#include <packet-stamp.h>
#include <antenna.h>
#include <mobilenode.h>
#include <propagation.h>
#include <wireless-phy.h>
#include <ranvar.h>
#include <inCar.h>

static class InCarClass: public TclClass {
public: 
	InCarClass() : TclClass("Propagation/InCar") {}
	TclObject* create(int, const char*const*) {
		return (new InCar);	
}
} class_incar;


InCar::InCar()
{
  bind("std_db_", &std_db);
  bind("m_", &m);
  bind("seed_", &seed);
  ranVar = new RNG;
  ranVar->set_seed(RNG::PREDEF_SEED_SOURCE, seed);
}



InCar::~InCar()
{
  delete ranVar;
	//
}


double InCar::Pr(PacketStamp *t, PacketStamp *r, WirelessPhy *ifp)
{
	double L = ifp->getL();	 	    // system loss
	double lambda = ifp->getLambda();   // wavelength

	double Xt, Yt, Zt;	    	    // loc of transmitter
	double Xr, Yr, Zr;	 	    // loc of receiver

	t->getNode()->getLoc(&Xt, &Yt, &Zt);
	r->getNode()->getLoc(&Xr, &Yr, &Zr);

	// Is antenna position relative to node position?
	Xr += r->getAntenna()->getX();
	Yr += r->getAntenna()->getY();
	Zr += r->getAntenna()->getZ();
	Xt += t->getAntenna()->getX();
	Yt += t->getAntenna()->getY();
	Zt += t->getAntenna()->getZ();

	double dX = Xr - Xt;
	double dY = Yr - Yt;
	double dZ = Zr - Zt;
	double dist = sqrt(dX * dX + dY * dY + dZ * dZ);
 
	// get antenna gain
 	double Gt = t->getAntenna()->getTxGain(dX, dY, dZ, lambda);
 	double Gr = r->getAntenna()->getRxGain(dX, dY, dZ, lambda);
	
	double d_ref = 1.0;

	// calculate receiving power at reference distance
	double Pr0 = Friis(t->getTxPr(), Gt, Gr, lambda, L, d_ref);

	// calculate average power loss predicted by empirical loss model in dB
	// according to measurements, 
	// the default settings of gamma, m and d are stored in tcl/lib/ns-default.tcl
  	double path_loss_dB = ranVar->normal(0.0,std_db);;
	
    // calculate the receiving power at distance dist
 	double Pr = Pr0 * pow(10.0, -path_loss_dB/10.0);
	
	unsigned int int_m = (unsigned int)(floor (m));
 	
	double resultPower;
 	
        if (int_m == m) {
	  resultPower = ErlangRandomVariable(Pr/m, int_m).value();
	} else {
	  resultPower = GammaRandomVariable(m, Pr/m).value();
	}
	return resultPower;
}	

int InCar::command(int argc, const char* const* argv)
{
  if(argc == 3) {
    if(strcmp(argv[1], "seed_random") == 0) {
      if(atoi(argv[2]) == 0)
	return TCL_OK;
      ranVar->set_seed(RNG::HEURISTIC_SEED_SOURCE, 0);
      return TCL_OK;
    } else if(strcmp(argv[1], "std_db_") == 0) {
      std_db = atof(argv[2]);
      return TCL_OK;
    } else if(strcmp(argv[1], "m_") == 0) {
      m = atof(argv[2]);
      return TCL_OK;
    }
   
  }
  return Propagation::command(argc, argv);
}


double InCar::getDist(double , double , double , double , double , double , double , double ) {
	return DBL_MAX;
}
