/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- 
 *
 * Copyright (c) 1996 Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the Computer Systems
 *	Engineering Group at Lawrence Berkeley Laboratory and the Daedalus
 *	research group at UC Berkeley.
 * 4. Neither the name of the University nor of the Laboratory may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * $Header: /nfs/jade/vint/CVSROOT/ns-2/mac/wireless-phy.cc,v 1.21 2004/12/10 22:07:13 johnh Exp $
 *
 * Ported from CMU/Monarch's code, nov'98 -Padma Haldar.
 * wireless-phy.cc
 */

#include <math.h>

#include <packet.h>

#include <mobilenode.h>
#include <phy.h>
#include <propagation.h>
#include <modulation.h>
#include <omni-antenna.h>
#include <wireless-phy.h>
#include <packet.h>
#include <ip.h>
#include <agent.h>
#include <trace.h>

#include "diffusion/diff_header.h"

#define MAX(a,b) (((a)<(b))?(b):(a))

void Idle_Timer::expire(Event *) {
	a_->UpdateIdleEnergy();
}


/* ======================================================================
   WirelessPhy Interface
   ====================================================================== */
static class WirelessPhyClass: public TclClass {
public:
        WirelessPhyClass() : TclClass("Phy/WirelessPhy") {}
        TclObject* create(int, const char*const*) {
                return (new WirelessPhy);
        }
} class_WirelessPhy;


WirelessPhy::WirelessPhy() : Phy(), idle_timer_(this), status_(IDLE)
{
	/*
	 *  It sounds like 10db should be the capture threshold.
	 *
	 *  If a node is presently receiving a packet a a power level
	 *  Pa, and a packet at power level Pb arrives, the following
	 *  comparion must be made to determine whether or not capture
	 *  occurs:
	 *
	 *    10 * log(Pa) - 10 * log(Pb) > 10db
	 *
	 *  OR equivalently
	 *
	 *    Pa/Pb > 10.
	 *
	 */
	bind("CPThresh_", &CPThresh_);
	bind("CSThresh_", &CSThresh_);
	bind("RXThresh_", &RXThresh_);
	bind("ambient_noise_", &ambient_noise_);
	//bind("bandwidth_", &bandwidth_);
	bind("Pt_", &Pt_);
	bind("freq_", &freq_);
	bind("L_", &L_);
	
	lambda_ = SPEED_OF_LIGHT / freq_;

	node_ = 0;
	ant_ = 0;
	propagation_ = 0;
	modulation_ = 0;

	// Assume AT&T's Wavelan PCMCIA card -- Chalermek
        //	Pt_ = 8.5872e-4; // For 40m transmission range.
	//      Pt_ = 7.214e-3;  // For 100m transmission range.
	//      Pt_ = 0.2818; // For 250m transmission range.
	//	Pt_ = pow(10, 2.45) * 1e-3;         // 24.5 dbm, ~ 281.8mw
	
	Pt_consume_ = 0.660;  // 1.6 W drained power for transmission
	Pr_consume_ = 0.395;  // 1.2 W drained power for reception

	//	P_idle_ = 0.035; // 1.15 W drained power for idle

	P_idle_ = 0.0;

	channel_idle_time_ = NOW;
	update_energy_time_ = NOW;
	last_send_time_ = NOW;
	noise_list = NULL; //Added Kevin
	noise = 0.0; //Added Kevin	
	idle_timer_.resched(1.0);
}

int
WirelessPhy::command(int argc, const char*const* argv)
{
	TclObject *obj; 

	if (argc==2) {
		if (strcasecmp(argv[1], "NodeOn") == 0) {
			if (em() == NULL) 
				return TCL_OK;
			if (NOW > update_energy_time_) {
				update_energy_time_ = NOW;
			}
			return TCL_OK;
		} else if (strcasecmp(argv[1], "NodeOff") == 0) {
			if (em() == NULL) 
				return TCL_OK;
			if (NOW > update_energy_time_) {
				em()->DecrIdleEnergy(NOW-update_energy_time_,
						     P_idle_);
				update_energy_time_ = NOW;
			}
			return TCL_OK;
		}
	} else if(argc == 3) {
		if (strcasecmp(argv[1], "setTxPower") == 0) {
			Pt_consume_ = atof(argv[2]);
			return TCL_OK;
		} else if (strcasecmp(argv[1], "setRxPower") == 0) {
			Pr_consume_ = atof(argv[2]);
			return TCL_OK;
		} else if (strcasecmp(argv[1], "setIdlePower") == 0) {
			P_idle_ = atof(argv[2]);
			return TCL_OK;
		} else if( (obj = TclObject::lookup(argv[2])) == 0) {
			fprintf(stderr,"WirelessPhy: %s lookup of %s failed\n", 
				argv[1], argv[2]);
			return TCL_ERROR;
		} else if (strcmp(argv[1], "propagation") == 0) {
			assert(propagation_ == 0);
			propagation_ = (Propagation*) obj;
			return TCL_OK;
		} else if (strcasecmp(argv[1], "antenna") == 0) {
			ant_ = (Antenna*) obj;
			return TCL_OK;
		} else if (strcasecmp(argv[1], "node") == 0) {
			assert(node_ == 0);
			node_ = (Node *)obj;
			return TCL_OK;
		}
	}
	return Phy::command(argc,argv);
}
 
void 
WirelessPhy::sendDown(Packet *p)
{
	/*
	 * Sanity Check
	 */
	assert(initialized());
	
	if (em()) 
		if ((em()->node_on() != true) || (em()->sleep())) {
			Packet::free(p);
			return;
		}

	/*
	 * Decrease node's energy
	 */
	if(em()) {
		if (em()->energy() > 0) {
			//double txtime = (8.*hdr_cmn::access(p)->size())/bandwidth_;
		    double txtime = hdr_cmn::access(p)->txtime();
		    double start_time = MAX(channel_idle_time_, NOW);
		    double end_time = MAX(channel_idle_time_, NOW+txtime);
		    double actual_txtime = end_time-start_time;

		    if (start_time > update_energy_time_) {
			    em()->DecrIdleEnergy(start_time - 
						 update_energy_time_, P_idle_);
			    update_energy_time_ = start_time;
		    }

		    /* It turns out that MAC sends packet even though, it's
		       receiving some packets.
		    
		    if (txtime-actual_txtime > 0.000001) {
			    fprintf(stderr,"Something may be wrong at MAC\n");
			    fprintf(stderr,"act_tx = %lf, tx = %lf\n", actual_txtime, txtime);
		    }
		    */

		   // Sanity check
		   double temp = MAX(NOW,last_send_time_);

		   /*
		   if (NOW < last_send_time_) {
			   fprintf(stderr,"Argggg !! Overlapping transmission. NOW %lf last %lf temp %lf\n", NOW, last_send_time_, temp);
		   }
		   */
		   
		   double begin_adjust_time = MIN(channel_idle_time_, temp);
		   double finish_adjust_time = MIN(channel_idle_time_, NOW+txtime);
		   double gap_adjust_time = finish_adjust_time - begin_adjust_time;
		   if (gap_adjust_time < 0.0) {
			   fprintf(stderr,"What the heck ! negative gap time.\n");
		   }

		   if ((gap_adjust_time > 0.0) && (status_ == RECV)) {
			   em()->DecrTxEnergy(gap_adjust_time,
					      Pt_consume_-Pr_consume_);
		   }

		   em()->DecrTxEnergy(actual_txtime,Pt_consume_);
		   if (end_time > channel_idle_time_) {
			   status_ = SEND;
		   }
							
		   last_send_time_ = NOW+txtime;
		   channel_idle_time_ = end_time;
		   update_energy_time_ = end_time;

		   if (em()->energy() <= 0) {
			   em()->setenergy(0);
			   ((MobileNode*)node())->log_energy(0);
		   }

		} else {
			Packet::free(p);
			return;
		}
	}

	/*
	 *  Stamp the packet with the interface arguments
	 */
	p->txinfo_.stamp((MobileNode*)node(), ant_->copy(), Pt_, lambda_);

	// Send the packet
	channel_->recv(p, this);
}

int 
WirelessPhy::sendUp(Packet *p)
{
	/*
	 * Sanity Check
	 */
	assert(initialized());

	double Pr;
	int pkt_recvd = 0;
	
	// if the node is in sleeping mode, drop the packet simply
	if (em()) 
		if (em()->sleep() || (em()->node_on() != true)) {
			pkt_recvd = 0;
			goto DONE;
		}
	
	// if the energy goes to ZERO, drop the packet simply
	if (em()) {
		if (em()->energy() <= 0) {
			pkt_recvd = 0;
			goto DONE;
		}
	}

	if(propagation_) {
		PacketStamp* s = new PacketStamp();
		s->stamp((MobileNode*)node(), ant_, 0, lambda_);
		//if (node_->nodeid() == 1){
		//	printf("node1\n");
		//	}
			
		Pr = propagation_->Pr(&p->txinfo_, s, this);
		delete s;
		//Added Kevin Begin
		int node_id = node_->nodeid();
		p->txinfo_.noise_value = getNoise();
		//if (node_->nodeid() == 1){
			//printf("[%f]getNoise() = %f \n", NOW, getNoise()*1000000000.0);
			//printf("[%f]Pr = %f \n", NOW, Pr*1000000000.0);
			//printf("[%f]Pr / getNoise() = %f \n", NOW, Pr / getNoise());
		//}
		//		if (Pr / getNoise() < CSThresh_) { //modified Kevin
		if (Pr < ambient_noise_){

			pkt_recvd = 0;
			//if (node_->nodeid() == 1)
			//printf("[%f]Packet below node #%d's CS\n", NOW, node_->nodeid());
			// have Pr, need to calculate finish transmitting time
			double rcvtime = hdr_cmn::access(p)->txtime();
			addNoise(Pr, NOW + rcvtime);
			goto DONE;
		}
		else{
			pkt_recvd = 1;
		}
		// have Pr, need to calculate finish transmitting time
		double rcvtime = hdr_cmn::access(p)->txtime();
		addNoise(Pr, NOW + rcvtime);

		//Added Kevin End
	}
	if(modulation_) {
		hdr_cmn *hdr = HDR_CMN(p);
		hdr->error() = modulation_->BitError(Pr);
	}
	
	/*
	 * The MAC layer must be notified of the packet reception
	 * now - ie; when the first bit has been detected - so that
	 * it can properly do Collision Avoidance / Detection.
	 */

DONE:
	p->txinfo_.getAntenna()->release();

	/* WILD HACK: The following two variables are a wild hack.
	   They will go away in the next release...
	   They're used by the mac-802_11 object to determine
	   capture.  This will be moved into the net-if family of 
	   objects in the future. */
	p->txinfo_.RxPr = Pr;
	p->txinfo_.CPThresh = CPThresh_;

	/*
	 * Decrease energy if packet successfully received
	 */
	if(pkt_recvd && em()) {
		//double rcvtime = (8. * hdr_cmn::access(p)->size())/bandwidth_;
		double rcvtime = hdr_cmn::access(p)->txtime();
		// no way to reach here if the energy level < 0
		
		/*
		  node()->add_rcvtime(rcvtime);	  
		  em()->DecrRcvEnergy(rcvtime,Pr_consume_);
		*/

		double start_time = MAX(channel_idle_time_, NOW);
		double end_time = MAX(channel_idle_time_, NOW+rcvtime);
		double actual_rcvtime = end_time-start_time;

		if (start_time > update_energy_time_) {
			em()->DecrIdleEnergy(start_time-update_energy_time_,
					     P_idle_);
			update_energy_time_ = start_time;
		}
		
		em()->DecrRcvEnergy(actual_rcvtime,Pr_consume_);
		if (end_time > channel_idle_time_) {
			status_ = RECV;
		}

		channel_idle_time_ = end_time;
		update_energy_time_ = end_time;

		/*
		  hdr_diff *dfh = HDR_DIFF(p);
		  printf("Node %d receives (%d, %d, %d) energy %lf.\n",
		  node()->address(), dfh->sender_id.addr_, 
		  dfh->sender_id.port_, dfh->pk_num, node()->energy());
		*/
		
		if (em()->energy() <= 0) {  
			// saying node died
			em()->setenergy(0);
			((MobileNode*)node())->log_energy(0);
		}
	}
	return pkt_recvd;
}

void
WirelessPhy::node_on()
{
        if (em() == NULL)
 	    return;	
   	if (NOW > update_energy_time_) {
      	    update_energy_time_ = NOW;
   	}
}

void 
WirelessPhy::node_off()
{
	if (em() == NULL)
            return;
        if (NOW > update_energy_time_) {
            em()->DecrIdleEnergy(NOW-update_energy_time_,
                                P_idle_);
            update_energy_time_ = NOW;
	}
}

void
WirelessPhy::dump(void) const
{
	Phy::dump();
	fprintf(stdout,
		"\tPt: %f, Gt: %f, Gr: %f, lambda: %f, L: %f\n",
		Pt_, ant_->getTxGain(0,0,0,lambda_), ant_->getRxGain(0,0,0,lambda_), lambda_, L_);
	//fprintf(stdout, "\tbandwidth: %f\n", bandwidth_);
	fprintf(stdout, "--------------------------------------------------\n");
}


void WirelessPhy::UpdateIdleEnergy()
{
	if (em() == NULL) {
		return;
	}
	if (NOW > update_energy_time_ && em()->node_on()) {
		  em()-> DecrIdleEnergy(NOW-update_energy_time_,
					P_idle_);
		  update_energy_time_ = NOW;
	}

	// log node energy
	if (em()->energy() > 0) {
		((MobileNode *)node_)->log_energy(1);
        } else {
		((MobileNode *)node_)->log_energy(0);   
        }

	idle_timer_.resched(10.0);
}

double WirelessPhy::getDist(double Pr, double Pt, double Gt, double Gr,
			    double hr, double ht, double L, double lambda)
{
	if (propagation_) {
		return propagation_->getDist(Pr, Pt, Gt, Gr, hr, ht, L,
					     lambda);
	}
	return 0;
}

// Added Kevin Begin

void WirelessPhy::addNoise(double value, double end)
{

	updateNoise();
	noise = noise + value;
	// insert noise element into sorted list
	NoiseElement* ne = noise_list;
	NoiseElement* previous = NULL;
	while (ne != NULL){
		if (ne->finish_time > end){
			break;
		}
		previous = ne;
		ne = ne->next;
	}
	
	NoiseElement* temp = new NoiseElement();
	temp->value = value;
	temp->finish_time = end;
	temp->next = ne;

	// replacing the head of the list
	if (previous == NULL){
		noise_list = temp;
	}
	else {
		previous->next = temp;
	}

}

double WirelessPhy::getNoise()
{
	updateNoise();
	return noise + ambient_noise_;
}

void WirelessPhy::updateNoise()
{
	Scheduler &s = Scheduler::instance();
	
	for (NoiseElement* ne = noise_list; ne != NULL; ){		
		if (ne->finish_time < s.clock() + .00000001){
			noise = noise - ne->value;
			noise_list = ne->next;
			delete ne;
			ne = noise_list;
		}
		else {
			ne = NULL;
		}
	}
}

void WirelessPhy::dumpNoiseList()
{
	updateNoise();
	printf("----------------------------------\nBegin Dump\n");
	printf("noise = %.12f\ttime = %.12f\n", noise, NOW);
	Scheduler &s = Scheduler::instance();
	for (NoiseElement* ne = noise_list; ne != NULL; ){		
		printf("Element\tValue:%.12f\tfinish:%.12f\n", ne->value, ne->finish_time);
		ne = ne->next;
	}
	printf("Finish Dump\n----------------------------------\n");
}


// Added Kevin End