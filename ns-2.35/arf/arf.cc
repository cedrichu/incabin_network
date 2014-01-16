
#include "arf.h"
#include <stdio.h>
#include "wireless-phyExt.h"

Arf::Arf(int Rate, int b, int i)
{
   currentRate = 0;
  consecutiveSuccess = 0;
  consecutiveFailures = 0;
  probationNumber = 0;
  mode = Rate; //0-constant, 1-ARF, 2-RBAR, 3-ONOE
  fixedRate = b;
  index = i;
  if(mode == 3) {
    m_shortRetry = 0;
    m_longRetry = 0;
    m_tx_ok = 0;
    m_tx_err = 0;
    m_tx_retr = 0;
    m_tx_upper = 0;
    m_txrate = 0;
    m_nextModeUpdate = Scheduler::instance().clock() + m_updatePeriod;
  }
  //  fprintf(stderr,"mode = %d, fixedRate %d\n",mode,fixedRate);
}
/* Start ONOE code */
void 
Arf::DoReportRtsFailed() {
  if(mode == 3) {
    m_shortRetry++;
  }
}

void 
Arf::DoReportDataFailed() {
  if(mode == 3) {
    m_longRetry++;
  }
}

void 
Arf::DoReportFinalRtsFailed() {
  if(mode == 3) {
    UpdateRetry();
    m_tx_err++;
  }
}

void 
Arf::DoReportFinalDataFailed() {
  if(mode == 3) {
    UpdateRetry();
    m_tx_err++;
  }
}

void 
Arf::DoReportDataOk() {
  if(mode == 3) {
    UpdateRetry();
    m_tx_ok++;
  }
}

void 
Arf::UpdateRetry() {
  if(mode == 3) {
    m_tx_retr += m_shortRetry + m_longRetry;
    m_shortRetry = 0;
    m_longRetry = 0;
  }
}
int
Arf::DoGetDataMode() {
  int nrate = m_txrate;
  UpdateMode();
  if(m_longRetry < 4)
    nrate = m_txrate;
  else if(m_longRetry < 6)
    nrate = (m_txrate >= 1) ? m_txrate-1 : 0;
  else if(m_longRetry < 8)
    nrate = (m_txrate >= 2) ? m_txrate-2 : 0;
  else
    nrate = (m_txrate >= 3) ? m_txrate-3 : 0;
  //  fprintf(stderr,"%d update mode m_longRetry %d nrate %d->%d\n",index,m_longRetry,m_txrate,nrate);
  return nrate;
}

void
Arf::UpdateMode() {
  if(Scheduler::instance().clock() <= m_nextModeUpdate) 
    return;
  
  m_nextModeUpdate = Scheduler::instance().clock() + m_updatePeriod;
  int dir = 0, nrate = m_txrate;
  int enough = (m_tx_ok + m_tx_err >= 10);

  if(m_tx_err > 0 && m_tx_ok == 0) dir = -1;
  if(enough && m_tx_ok < m_tx_retr) dir = -1;
  if(enough && m_tx_err == 0 && m_tx_retr < (m_tx_ok * addCreditThreshold)/100) dir = 1;

  //  fprintf(stderr,"%d Ok %d err %d retr %d upper %d dir %d\n",&m_tx_ok,m_tx_ok,m_tx_err,m_tx_retr,m_tx_upper,dir);
  //  fprintf(stderr,"%f %d -9876 %d %d %d %d %d ",Scheduler::instance().clock(),&m_tx_ok,m_tx_ok,m_tx_err,m_tx_retr,m_tx_upper,dir);
  

  if(dir == 0) {
    if(enough && m_tx_upper > 0) m_tx_upper--;
  } else if(dir == -1) {
    nrate = (nrate > 0) ? nrate-1: 0;
    m_tx_upper = 0;
  } else if(dir == 1) {
    if(++m_tx_upper < raiseThreshold) {}
    else {
      int length = sizeof(modulation_table)/sizeof(ModulationParam);
      nrate = (nrate < length-2) ? nrate+1:(length-1);
      m_tx_upper = 0;
    }
  }
  //  fprintf(stderr,"%d\n",nrate);
  if(nrate != m_txrate) {
    m_tx_ok = 0;
    m_tx_err = 0;
    m_tx_retr = 0;
    m_tx_upper = 0;
    m_txrate = nrate;
  } else {
    m_tx_ok = 0;
    m_tx_err = 0;
    m_tx_retr = 0;
  }
}
/* end ONOE code */
void
Arf::successfulTransmission ()
{
  // if still in probation
  if (probationNumber > 0){
    // Correctly tx, so leave probation
    probationNumber = 0;
  }
  else {
    consecutiveSuccess++;
    consecutiveFailures = 0;
    
    if (consecutiveSuccess >= STEPUP_THRESHOLD){
      // don't increase beyond the Max
      if (currentRate < 3)
	currentRate++;
      // reset counters
      consecutiveSuccess = 0;
      consecutiveFailures = 0;
      // enter probation
      probationNumber = PROBATION_SIZE;
    }
    if(mode == 1)
      fprintf(stderr,"successful transmission: currentRate %d\n",currentRate);
  }
}

void
Arf::failedTransmission ()
{
  // Check for failed last chance in probation
  if (probationNumber == 1){
    currentRate--;
    consecutiveSuccess = 0;
    consecutiveFailures = 0;
    probationNumber = 0;    
  }
  else if (probationNumber > 1){
    // failed a chance to get out of probation
    probationNumber--;
  }
  else{
    consecutiveSuccess = 0;
    consecutiveFailures++;
    // Too many failures, fallback
    if ((consecutiveFailures >= FALLBACK_THRESHOLD) && (currentRate > 0)){
      currentRate--;
      consecutiveSuccess = 0;
      consecutiveFailures = 0;
      probationNumber = 0;
    }
  }
  if(mode == 1)
    fprintf(stderr,"failed transmission: currentRate %d\n",currentRate);
}


double
Arf::getTransmissionRate ()
{
  double rate;
  switch (currentRate){
  case 0:
    rate = 1000000.0;
    break;
  case 1:
    rate = 2000000.0;
    break;
  case 2:
    rate = 5500000.0;
    break;
  case 3:
    rate = 11000000.0;
    break;
  default:
    printf("Unknown rate %d\n", currentRate);
    rate = 0;
  }

  return rate;
}

int
Arf::getModScheme (double SINR)
{

  //SINR ratio
  //return 0
  int length = sizeof(modulation_table)/sizeof(ModulationParam);
  //  fprintf(stderr,"%f SINR %f %f %d\n",Scheduler::instance().clock(),SINR,modulation_table[7].SINR_ratio,length);
  if(mode == 0) {
    //default, always use basic
    if(fixedRate > length) {
      fprintf(stderr,"Unsupported rate %d\n",fixedRate);
      exit(0);
    }
    return fixedRate;
  }
  else if(mode == 1) {
    //ARF
    return currentRate; 
  } else if(mode == 2) {
    for(int i=length-1; i > 0; i--) {
      if(SINR > modulation_table[i].SINR_ratio) {
	//	fprintf(stderr,"%f use rate SINR %.2f %d\n",Scheduler::instance().clock(),modulation_table[i].SINR_ratio,i);
	return i;
      }
    }
    fprintf(stderr,"%f use basic rate SINR %.2f\n",Scheduler::instance().clock(),SINR);
    return 0;
  } else if(mode == 3){
    //ONOE
    return DoGetDataMode();
  }
  return 0;
}
