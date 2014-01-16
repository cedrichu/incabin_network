BEGIN {
    #x = unique pkt id, y = flow
    for ( x = 1; x <= 1000; x++ ) {
	    timeTX[x] = -1;
	    timeRX[x] = -1;
    }
    for ( x = 1; x <= 2000; x++) {
	cum_pkt[x]=0;
	pktID[x]="";
    }
    nflow = 0;
}


{
    if($1 == "+") {
	ip_src_num=4*substr($9,1,1)+substr($9,3,1)+substr($9,5,1);
	src = $3+0;
	if($src == $ip_src_num)  {
	    timeTX[$12%1000+1] = $2;
	    #printf("%s\n",$0);
	}
    } 
    if($1 == "s") {
	if(substr($3,1,1) == "_") {
	    #printf("%s %s\n",substr($3,2,length($3)-2),substr($14,2,index($14,":")-2));
	    srcc = substr($3,2,length($3)-2);
	    #src = $srcc+;
	    xxc = substr($14,2,index($14,":")-2)-4194300;
	    #printf("%s %s %d\n",srcc,xxc,srcc==xxc);
	    #xx = $xxc+0;
	    #printf("%s %d %d\n",$0,$src,$xx);
	    #printf("\n%s\n%d %d\n",$0,$src,$xx);
	    #printf("%s %s %s %d\n",src,xx,$6,$src==$xx);
	    if(srcc == xxc) {
		#printf("%s %s\n",$6,$2);
		timeTX[$6%1000+1] = $2;
	    }
	}

    }
    if($1 == "r") {
	if(substr($3,1,1) == "_") {
	    #printf("%s\n",$0);
	    timeRX[$6%1000+1] = $2;
	    if($7 == "tcp") y = 1;
	    else if ($7 == "ack") y = 0;
	    else if ($7 == "cbr") y = 2;
	    if(substr($14,2,4) == "6144" && substr($15,1,6)=="419430") {
		#printf("%s\n",$0);
		#printf("%d %d %d %d = %d%d\n",3,substr($14,7),substr($15,1,7)-4194300,substr($15,9),300+substr($14,7),100*(substr($15,1,7)-4194300)+substr($15,9));;
	    
		xx = 300+substr($14,7) "" 100*(substr($15,1,7)-4194300)+substr($15,9);
	    }
	    if(substr($14,2,6) == "419430" && substr($15,1,6)=="419430") {
		xx = 100*(substr($14,2,7)-4194300)+substr($14,10) "" 100*(substr($15,1,7)-4194300)+substr($15,9);
	    }
		#printf("%s %s\n",xx,substr($14,2,7));
		found = 0;
		for ( x = 1; x <= 2000; x++) {
		    if(pktID[x] == xx) {
			found = 1;
			break;
		    }
		}
		if(found == 0) {
		    nflow = nflow + 1;
		    pktID[nflow]=xx;
		    cum_pkt[nflow] = $8;
		    x = nflow;
		  #  printf("not found %s\n",xx);
		} else {
		    cum_pkt[x] = cum_pkt[x] + $8;
		 #   printf("found %s %s\n",xx,pktID[x]);
		}
		#printf("%d found %d nflow %d pktID %s cum_pkt %d\n",xx,found,nflow,pktID[x],cum_pkt[x]);
		printf("%d %f %f %d %d %s %d %d\n",$6,timeTX[$6%1000+1],timeRX[$6%1000+1],$8,y,pktID[x],cum_pkt[x],x);
	    }
	
    }

}

END {


}