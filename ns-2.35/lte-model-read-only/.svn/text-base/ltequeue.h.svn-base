#ifndef ns_ltequeue_h
#define ns_ltequeue_h

#include "drop-tail.h"
//#include "drr.h"
#include "red.h"

//extern int max_buf;
extern int flow[100];

class LTEQueue : public Queue {
public:
	LTEQueue() {
		bind_bool("qos_", &qos_);
		bind_bool("flow_control_", &flow_control_);
	}
	~LTEQueue() {
	}
	void enque(Packet* p);
	Packet *deque();
protected:
	int qos_;
	int flow_control_;
};



#endif
