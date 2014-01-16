/* -*-	Mode:C++; c-basic-offset:8; tab-width:8; indent-tabs-mode:t -*- *t
/*
 * Copyright (c) Xerox Corporation 1997. All rights reserved.
 *  
 * License is granted to copy, to use, and to make and to use derivative
 * works for research and evaluation purposes, provided that Xerox is
 * acknowledged in all documentation pertaining to any such copy or derivative
 * work. Xerox grants no other licenses expressed or implied. The Xerox trade
 * name should not be used in any advertising without its written permission.
 *  
 * XEROX CORPORATION MAKES NO REPRESENTATIONS CONCERNING EITHER THE
 * MERCHANTABILITY OF THIS SOFTWARE OR THE SUITABILITY OF THIS SOFTWARE
 * FOR ANY PARTICULAR PURPOSE.  The software is provided "as is" without
 * express or implied warranty of any kind.
 *  
 * These notices must be retained in any copies of any part of this software.
 */

/*
 * A source for MPEG4 traffic. 
 * Procduces traffic that has the same marginal distribution and
 * autocorrelation function of a specified mpeg4 trace.
 * A TES model is developed first to get the statistics and then 
 * the statistics are used to generate the traffic
 */

#include <stdlib.h>
#include "random.h"
#include "trafgen.h"
#include "ranvar.h"

class Frame {
 public:
   Frame(const char*, const char*);
   void Get_Frame(double &,int &);   // get the next frame size	
 protected:
   RNG* rng_;	  
   int numEntry_inv_;              // number of entries in the innovation CDF table
   int numEntry_hist_;             // number of entries in the frame size CDF table
   int maxEntry_;                  // size of the CDF table (mem allocation)
   CDFentry* table_hist_;          // CDF table of frame sizes (val_, cdf_)
   CDFentry* table_inv_;           // CDF table of innovation (val_, cdf_)
   double value(double, CDFentry*, int );   
   double interpolate(double x, double x1, double y1, double x2, double y2);
   int lookup(double u,CDFentry*,int);
   void loadCDF(const char* filename, CDFentry*& , int &);
};

Frame::Frame(const char* file_1, const char* file_2)  :  maxEntry_(120), table_inv_(0), table_hist_(0)
{  
   rng_ = RNG::defaultrng();  
   loadCDF (file_1,table_hist_,numEntry_hist_);
   loadCDF (file_2,table_inv_,numEntry_inv_);
}

void Frame::Get_Frame (double &U, int &size)
{
  size  = int(value(U, table_hist_,numEntry_hist_));
  double rnd = rng_->uniform(0,1);
  double w = value (rnd,table_inv_,numEntry_inv_);
  U = U + w;
  U = U - floor(U);
  //smoothing using eata = 0.5
  if (U<0.5) U = U/0.5;
  else U = (1-U)/0.5; 
}
	
double Frame::value(double u, CDFentry *table_, int numEntry_)
{
   if (numEntry_ <= 0) return 0;
	int mid = lookup(u,table_,numEntry_);
		return interpolate(u, table_[mid-1].cdf_, table_[mid-1].val_,
				   table_[mid].cdf_, table_[mid].val_);
}

double Frame::interpolate(double x, double x1, double y1, double x2, double y2)
{
	return (y1 + (x - x1) * (y2 - y1) / (x2 - x1));
}

int Frame::lookup(double u, CDFentry* table_, int numEntry_)
{
	// always return an index whose value is >= u
	int lo, hi, mid;
	if (u <= table_[0].cdf_)
		return 0;
	for (lo=1, hi=numEntry_-1;  lo < hi; ) {
		mid = (lo + hi) / 2;
		if (u > table_[mid].cdf_)
			lo = mid + 1;
		else hi = mid;
	}
	return lo;
}

void  Frame::loadCDF(const char* filename, CDFentry*& table_, int & numEntry_)
{
	FILE* fp;
	char line[256];
	CDFentry* e;
	fp = fopen(filename, "r");
	if (fp == 0) 
		return ;

	if (table_ == 0)
		table_ = new CDFentry[maxEntry_];
	for (numEntry_=0;  fgets(line, 256, fp);  numEntry_++) {
		e = &table_[numEntry_];
		sscanf(line, "%lf %lf", &e->val_, &e->cdf_);
	}
	return ;
}

//////////////////////////////////////////////////////////////////////////////////////////////////


class VIDEO_Traffic : public TrafficGenerator {
 public:
	VIDEO_Traffic();
	virtual double next_interval(int&);
        double u0; 
        double rateFactor_;
protected:
        RNG* rng_;	  
        const char* I_File_1; 
        const char* I_File_2; 
        const char* P_File_1; 
        const char* P_File_2; 
        const char* B_File_1; 
        const char* B_File_2;
        char prev_frame_type, next_frame_type;
        double inter_frame_interval_ ;
	int GOP_count;
	virtual void start();
	virtual void timeout();
        double Ui, Up, Ub;              // random seeds for the three frame types      
        Frame *i;
	Frame *p;
	Frame *b;
};


static class VIDEOTrafficClass : public TclClass {
 public:
	VIDEOTrafficClass() : TclClass("Application/Traffic/MPEG4") {}
	TclObject* create(int, const char*const*) {
		return (new VIDEO_Traffic());
	}
} class_video_traffic;

VIDEO_Traffic::VIDEO_Traffic()
{
        bind("initialSeed_",&u0);
        bind("rateFactor_",&rateFactor_);
	I_File_1 = "./video_model/Imodel_hist_1"; 
	I_File_2 = "./video_model/Imodel_inv_1"; 
	P_File_1 = "./video_model/Pmodel_hist_1";
	P_File_2 = "./video_model/Pmodel_inv_1"; 
	B_File_1 = "./video_model/Bmodel_hist_1"; 
	B_File_2 = "./video_model/Bmodel_inv_1"; 
        rng_ = RNG::defaultrng();  
}

void VIDEO_Traffic::start()
{
        Ui = u0;
        size_ = 0;
	next_frame_type = 'I';
	prev_frame_type = ' ';
	GOP_count = 3;                      //first GOP is 10 frames only
	inter_frame_interval_ = 1.0/30.0;   // 30 frame/sec
	i = new Frame (I_File_1,I_File_2);
	p = new Frame (P_File_1,P_File_2);
	b = new Frame (B_File_1,B_File_2);
	if (agent_) agent_->set_pkttype(PT_VIDEO);     
        running_ = 1;
        timeout();
}

void VIDEO_Traffic::timeout()
{
        double frame_in_bytes; 
        if (! running_)
                return;
        /* figure out when to send the next one */
          nextPkttime_ = next_interval(size_);
        /* send a packet */
        frame_in_bytes = rateFactor_*size_/8 ; 
        agent_->sendmsg(frame_in_bytes);     
	/* schedule it */
        if (nextPkttime_ > 0)
               timer_.resched(nextPkttime_);
        else
               running_ = 0;
}

double VIDEO_Traffic::next_interval(int& size)
{
	if (next_frame_type == 'I'){
              i->Get_Frame(Ui, size);
	      Up = Ui;
	      Ub = Ui;
	      if (prev_frame_type == 'B')  
                    next_frame_type = 'B'; 
              else  next_frame_type = 'P';  
              prev_frame_type = 'I';
	} // end if == I
	else if (next_frame_type == 'P'){
	      p->Get_Frame (Up, size);
	      next_frame_type = 'B';  
              prev_frame_type = 'P';
	} // end if == P
	else if (next_frame_type == 'B'){
	      b->Get_Frame (Ub, size);
	      if (prev_frame_type != 'B') next_frame_type = 'B';
				     else if (GOP_count == 12)
					     {next_frame_type = 'I'; GOP_count = 0;}
	                                     else next_frame_type = 'P';  
              prev_frame_type = 'B';
	} // end if == B
      GOP_count++ ;
      return(inter_frame_interval_);
}
