#include "dlairqueue.h"

//int max_buff=51200;
extern int flow[100];

static class DLAirQueueClass : public TclClass {
public:
	DLAirQueueClass() : TclClass("Queue/LTEQueue/DLAirQueue") {}
	TclObject* create (int, const char*const*) {
		return (new DLAirQueue);
	}
} class_dlairqueue;

void DLAirTimer::expire(Event*)
{
	q_->update();
}

void DLAirQueue::update()
{
	if(!qos_) {
		flow[0] = q0->limit()*q0->meanPacketSize() - q0->byteLength();
	} else {	
		flow[0] = q0->limit()*q0->meanPacketSize() - q0->byteLength();
		flow[1] = q1->limit()*q1->meanPacketSize() - q1->byteLength();
		flow[2] = q2->limit()*q2->meanPacketSize() - q2->byteLength();
		flow[3] = q3->limit()*q3->meanPacketSize() - q3->byteLength();
	}

	dlairtimer.resched(1.0);
}

void DLAirQueue::enque(Packet* p)
{
	hdr_ip *iph=HDR_IP(p);
	int classid=iph->flowid();

	if(qos_) {
		//classfication
		switch(classid){
			case 0: q0->enqueue(p);break;
			case 1: q1->enqueue(p);break;
			case 2: q2->enqueue(p);break;
			case 3: q3->enqueue(p);break;
			default: 
				{
					printf("invalid classid %d\n",classid);
					exit(0);
				}
		}
	} else {//no qos_, no classification
		q0->enqueue(p);
	}	
}	

Packet* DLAirQueue::deque()
{
	if(!qos_)
	{
		return q0->dequeue();
	}	
	//scheduling: strict priority
	if(q0->length()>0)
	{
		return q0->dequeue();
	}
	if(q1->length()>0)
	{
		return q1->dequeue();
	}
	if(q2->length()>0)
	{
		//return q2->deque();
		return q2->dequeue();
	}
	if(q3->length()>0)
	{
		return q3->dequeue();
	}

	return NULL;
}	
