#include "dls1queue.h"

extern int flow[100];

static class DLS1QueueClass : public TclClass {
public:
	DLS1QueueClass() : TclClass("Queue/LTEQueue/DLS1Queue") {}
	TclObject* create (int, const char*const*) {
		return (new DLS1Queue);
	}
} class_dls1queue;

void DLS1Queue::enque(Packet* p)
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

Packet* DLS1Queue::deque()
{
	if(!flow_control_) 
	{
		if(!qos_)
			return q0->dequeue();
	
		//if qos_ && !flow_control
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
	
		//all the queues are empty, no packet to be sent.
		//printf("LTEQueue::deque(), all the queues are empty, no packet to be sent.\n");
		return NULL;
	}

	//else flow control
	//flow control only valid to classo 2 & class 3
	if(!qos_)
	{
		for(int i=0;i < q0->length();i++)
		{
			Packet *p=q0->find(i);
			if(p==NULL) return NULL;
			hdr_ip *iph=HDR_IP(p);
			hdr_cmn *cmh=HDR_CMN(p);
			int size=cmh->size();
			int classid=iph->flowid();
			//int flowid=iph->daddr();

			if(classid==0 || classid==1)
			{
				p=q0->remove(i);
				return p;
			}
			//flow control only apply to class 2 and class 3
			if(size < flow[classid])
			{
				p=q0->remove(i);
				return p;
			}
			
			//else continue to find next packet
		}
		//no packet can be sent
		return NULL;
	}

	//with flow control and QoS
	if(qos_)
	{
		if(q0->length()>0) {
			return q0->dequeue();
		}
		if(q1->length()>0) {
			return q1->dequeue();
		}
		for(int i=0;i < q2->length();i++) {
			Packet *p=q2->find(i);
			if(p==NULL) {
				// no packet to send in q2
				break;
			}
			hdr_ip *iph=HDR_IP(p);
			hdr_cmn *cmh=HDR_CMN(p);
			int size=cmh->size();
			int classid=iph->flowid();
			int flowid=iph->daddr();

			if(size<flow[classid])
			{
				p=q2->remove(i);
				return p;
			}
			//else continue to find next packet
		}
		
		//no packet can be sent in q2, try q3
		for(int i=0;i < q3->length();i++) {
			Packet *p=q3->find(i);
			if(p==NULL) {
				// no packet to send in q3
				return NULL;
			}
			hdr_ip *iph=HDR_IP(p);
			hdr_cmn *cmh=HDR_CMN(p);
			int size=cmh->size();
			int flowid=iph->daddr();
			int classid=iph->flowid();
			
			if(size<flow[classid])
			{
				p=q3->remove(i);
				return p;
			}
			//else continue to find next packet
		}
		// no packet can be sent in q3
		return NULL;
	}
}
