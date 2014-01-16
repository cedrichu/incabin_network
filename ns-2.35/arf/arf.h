#ifndef arf_h_
#define arf_h_


class Arf {
 private:
  static const int STEPUP_THRESHOLD = 4; // 4 consecutive tx before increasing
  static const int FALLBACK_THRESHOLD = 2; // 2 consecutive tx failures before decreasing
  static const int PROBATION_SIZE = 1;

  int currentRate;
  int consecutiveSuccess;
  int consecutiveFailures;
  int probationNumber; // Number of tries left to get a packet through at the new rate
  int mode;
  int fixedRate;

  /*for ONOE */
  void UpdateMode();
  void UpdateRetry();
  int DoGetDataMode();
  static const double m_updatePeriod = 1.0;
  static const int addCreditThreshold = 20;
  static const int raiseThreshold = 10;

  double m_nextModeUpdate;
  int m_shortRetry, m_longRetry;
  int m_tx_ok, m_tx_err, m_tx_retr, m_tx_upper;
  int m_txrate;
  int index;

 public:
  Arf(int, int, int);
  void successfulTransmission();
  void failedTransmission();
  double getTransmissionRate();
  int getModScheme(double);

  /* for ONOE */
  void DoReportRtsFailed();
  void DoReportDataFailed();
  void DoReportDataOk();
  void DoReportFinalRtsFailed();
  void DoReportFinalDataFailed();
};

#endif
