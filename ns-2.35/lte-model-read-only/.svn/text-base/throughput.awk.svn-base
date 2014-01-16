#!/usr/bin/awk -f
# calculate each class throughput

BEGIN{
}

{
#r 0.241408 1 0 tcp 1040 ------- 1   4.0   0.0   3   6
#$1 $2     $3 $4 $5   $6   $7    $8    $9   $10  $11  $12
      event = $1;
      time = $2;
      node_s = $3;
      node_d = $4;
      trace_type = $5;
      pkt_size = $6;
      classid = $8;

#eNB node id is 0
#aGW node id is 1
#server node id is 2
#UE node id >2
     if(event == "r" && node_d >2 ) 
       {
             pkt_byte_sum[classid] = pkt_byte_sum[classid] + pkt_size;
       }
}

END {      
	for(i=0;i<4;i++)
	{
		th[i]=pkt_byte_sum[i]*8/1000;
		total=total+th[i];
	}
	printf("0 		 1		 2		 3		 total\n");
	print th[0], "	", th[1],"	", th[2],"	", th[3],"	", total;
}
