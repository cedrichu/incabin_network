Phy/WirelessPhyExt set CSThresh_                6.31e-12    ;#-82 dBm Wireless interface sensitivity (sensitivity defined in the standard)
Phy/WirelessPhyExt set Pt_                      0.001  
Phy/WirelessPhyExt set freq_                    5.18e+9
Phy/WirelessPhyExt set noise_floor_             2.512e-13   ;#-96 dBm for 10MHz bandwidth
Phy/WirelessPhyExt set L_                       1.0         ;#default radio circuit gain/loss
Phy/WirelessPhyExt set PowerMonitorThresh_      1.259e-13   ;#-99dBm power monitor  sensitivity
Phy/WirelessPhyExt set HeaderDuration_          0.000020    ;#20 us
Phy/WirelessPhyExt set BasicModulationScheme_   0  
Phy/WirelessPhyExt set PreambleCaptureSwitch_   1
Phy/WirelessPhyExt set DataCaptureSwitch_       0
Phy/WirelessPhyExt set SINR_PreambleCapture_    2.5118;     ;# 4 dB
Phy/WirelessPhyExt set SINR_DataCapture_        100.0;      ;# 10 dB
Phy/WirelessPhyExt set trace_dist_              1e6         ;# PHY trace until distance of 1 Mio. km ("infinty")
Phy/WirelessPhyExt set PHY_DBG_                 0
Mac/802_11Ext set CWMin_                        15
Mac/802_11Ext set CWMax_                        1023
Mac/802_11Ext set SlotTime_                     0.000009
Mac/802_11Ext set SIFS_                         0.000016
Mac/802_11Ext set ShortRetryLimit_              7
Mac/802_11Ext set LongRetryLimit_               4
Mac/802_11Ext set HeaderDuration_               0.000020
Mac/802_11Ext set SymbolDuration_               0.000004
Mac/802_11Ext set BasicModulationScheme_        0
Mac/802_11Ext set use_802_11a_flag_             true
Mac/802_11Ext set RTSThreshold_                 2346
Mac/802_11Ext set MAC_DBG                       0
Mac/802_11Ext set Noise_floor_             2.512e-13   ;#-96 dBm for 10MHz bandwidth
Mac/802_11Ext set Rate_Mode_ 3;
Mac/802_11Ext set Fixed_rate_ 0;
Propagation/InCar set std_db_ 4.0
Propagation/InCar set m_ 1.0
Propagation/InCar seed_random 1
set opt(nn) 2
set opt(stop) 1.0
global opt
set opt(chan)       Channel/WirelessChannel
set opt(prop)       Propagation/InCar
set opt(netif)      Phy/WirelessPhyExt
set opt(mac)        Mac/802_11Ext
set opt(ifq)        Queue/DropTail/PriQueue
set opt(ll)         LL
set opt(ant)        Antenna/OmniAntenna
set opt(x)             670   
set opt(y)              670   
set opt(ifqlen)         50   
set opt(tr)          output.tr
set opt(namtr)       output.trnam
set opt(adhocRouting)   DSDV     
set num_wired_nodes      1
set num_bs_nodes         1
set ns_   [new Simulator]
# set up for hierarchical routing
$ns_ node-config -addressType hierarchical
AddrParams set domain_num_ 2
lappend cluster_num 4 1 
AddrParams set cluster_num_ $cluster_num
lappend eilastlevel 1 1 1 1 4     
AddrParams set nodes_num_ $eilastlevel 
set tracefd  [open $opt(tr) w]
$ns_ trace-all $tracefd
set topo   [new Topography]
$topo load_flatgrid $opt(x) $opt(y)
create-god [expr $opt(nn) + $num_bs_nodes]
Queue/LTEQueue set qos_ true
Queue/LTEQueue set flow_control_ false
#create wired nodes
set UE(0) [$ns_ node '0.0.0']
set eNB [$ns_ node '0.1.0'];
set aGW [$ns_ node '0.2.0']
set server [$ns_ node '0.3.0']
$ns_ node-config -adhocRouting $opt(adhocRouting) \
-llType $opt(ll) \
-macType $opt(mac) \
-ifqType $opt(ifq) \
-ifqLen $opt(ifqlen) \
-antType $opt(ant) \
-propInstance [new $opt(prop)] \
-phyType $opt(netif) \
-channel [new $opt(chan)] \
-topoInstance $topo \
-wiredRouting ON \
-agentTrace ON \
-routerTrace OFF \
-macTrace OFF
set temp {1.0.0 1.0.1 1.0.2 1.0.3 1.0.4}
Phy/WirelessPhyExt set Pt_ 0.001
$ns_ node-config -phyType $opt(netif)
set BS(0) [$ns_ node [lindex $temp 0]]
$BS(0) random-motion 0
$BS(0) set X_ 11.0
$BS(0) set Y_ 11.5
$BS(0) set Z_ 0.0
$ns_ node-config -wiredRouting OFF
Phy/WirelessPhyExt set Pt_ 0.001
$ns_ node-config -phyType $opt(netif)
set node_(0) [$ns_ node [lindex $temp [expr 1]]]
$node_(0) base-station [AddrParams addr2id [$BS(0) node-addr]]
$node_(0) random-motion 0
$node_(0) set X_ 10.5
$node_(0) set Y_ 12.0
$node_(0) set Z_ 0.0
Phy/WirelessPhyExt set Pt_ 0.01
$ns_ node-config -phyType $opt(netif)
set node_(1) [$ns_ node [lindex $temp [expr 2]]]
$node_(1) base-station [AddrParams addr2id [$BS(0) node-addr]]
$node_(1) random-motion 0
$node_(1) set X_ 11.2
$node_(1) set Y_ 10.5
$node_(1) set Z_ 0.0
$ns_ duplex-link $BS(0) $UE(0) 100.0Mb 0.1ms DropTail
$ns_ simplex-link $UE(0) $eNB 1.8Mb 140ms DropTail
$ns_ at 5.0 "[[$ns_ link $UE(0) $eNB] link] set bandwidth_ 0.2Mb"
$ns_ at 5.0 "[[$ns_ link $UE(0) $eNB] link] set delay_ 140ms"
$ns_ at 10.0 "[[$ns_ link $UE(0) $eNB] link] set bandwidth_ 0.05Mb"
$ns_ at 10.0 "[[$ns_ link $UE(0) $eNB] link] set delay_ 140ms"
$ns_ at 20.0 "[[$ns_ link $UE(0) $eNB] link] set bandwidth_ 0.1Mb"
$ns_ at 20.0 "[[$ns_ link $UE(0) $eNB] link] set delay_ 140ms"
$ns_ at 30.0 "[[$ns_ link $UE(0) $eNB] link] set bandwidth_ 0.09Mb"
$ns_ at 30.0 "[[$ns_ link $UE(0) $eNB] link] set delay_ 200ms"
$ns_ at 40.0 "[[$ns_ link $UE(0) $eNB] link] set bandwidth_ 0.15Mb"
$ns_ at 40.0 "[[$ns_ link $UE(0) $eNB] link] set delay_ 100ms"
$ns_ simplex-link $eNB $UE(0) 3.2Mb 140ms DropTail
$ns_ at 5.0 "[[$ns_ link $eNB $UE(0)] link] set bandwidth_ 0.3Mb"
$ns_ at 5.0 "[[$ns_ link $eNB $UE(0)] link] set delay_ 140ms"
$ns_ at 10.0 "[[$ns_ link $eNB $UE(0)] link] set bandwidth_ 0.15Mb"
$ns_ at 10.0 "[[$ns_ link $eNB $UE(0)] link] set delay_ 140ms"
$ns_ at 20.0 "[[$ns_ link $eNB $UE(0)] link] set bandwidth_ 0.2Mb"
$ns_ at 20.0 "[[$ns_ link $eNB $UE(0)] link] set delay_ 140ms"
$ns_ at 30.0 "[[$ns_ link $eNB $UE(0)] link] set bandwidth_ 0.04Mb"
$ns_ at 30.0 "[[$ns_ link $eNB $UE(0)] link] set delay_ 200ms"
$ns_ at 40.0 "[[$ns_ link $eNB $UE(0)] link] set bandwidth_ 0.2Mb"
$ns_ at 40.0 "[[$ns_ link $eNB $UE(0)] link] set delay_ 100ms"
$ns_ duplex-link $eNB $server 960.0Mb 40.0ms DropTail
Agent/TCP set timestamps_ true
set delack 0.4
Agent/TCP set interval_ $delack
Agent/TCP/FullTcp set timestamps_ true
Agent/TCP/FullTcp set interval_ $delack

Agent/TCP/Linux instproc done {} {
global ns_ filesize
#this doesn't seem to work, had to hack tcp-linux.cc to do repeat ftps
$self set closed_ 0
#needs to be delayed by at least .3sec to slow start
puts "[$ns_ now] TCP/Linux proc done called"
$ns_ at [expr [$ns_ now] + 0.3] "$self send $filesize"
}

# problem is that idle() in tcp.cc never seems to get called...
Application/FTP instproc resume {} {
puts "called resume"
 global filesize
$self send $filesize
#	$ns_ at [expr [$ns_ now]  0.5] "[$self agent] reset"
$ns_ at [expr [$ns_ now] + 0.5] "[$self agent] send $filesize"
}

Application/FTP instproc fire {} {
global filesize
$self instvar maxpkts_
set maxpkts_ $filesize
[$self agent] set maxpkts_ $filesize
$self send $maxpkts_
puts "fire() FTP"
}

proc build_cbr {cnd snd startTime timeToStop Flow_id rate pktSize} {
global ns_
set udp [$ns_ create-connection UDP $snd LossMonitor $cnd $Flow_id]
set cbr [new Application/Traffic/CBR]
$cbr attach-agent $udp
# change these for different types of CBRs
$cbr set packetSize_ $pktSize
$cbr set rate_ $rate
$ns_ at $startTime "$cbr start"
$ns_ at $timeToStop "$cbr stop"
}

# cnd is client node, snd is server node
proc build_ftpclient {cnd snd startTime timeToStop Flow_id filesize} {

global ns_ 
set ctcp [$ns_ create-connection TCP/Linux $snd TCPSink/Sack1 $cnd $Flow_id]
$ctcp select_ca cubic
set ftp [$ctcp attach-app FTP]
$ftp set enableResume_ true
$ftp set type_ FTP 

#set up a single infinite ftp with smallest RTT
if {$filesize < 0} {
$ns_ at $startTime "$ftp start"
} else {
$ns_ at $startTime "$ftp send $filesize"
}
$ns_ at $timeToStop "$ftp stop"
}

proc build_webs {cnd snd rate startTime timeToStop} {
set CLIENT 0
set SERVER 1

# SETUP PACKMIME
set pm [new PackMimeHTTP]
$pm set-TCP Sack
$pm set-client $cnd
$pm set-server $snd
$pm set-rate $rate;                    # new connections per second
$pm set-http-1.1;                      # use HTTP/1.1

# create RandomVariables
set flow_arrive [new RandomVariable/PackMimeHTTPFlowArrive $rate]
set req_size [new RandomVariable/PackMimeHTTPFileSize $rate $CLIENT]
set rsp_size [new RandomVariable/PackMimeHTTPFileSize $rate $SERVER]

# assign RNGs to RandomVariables
$flow_arrive use-rng [new RNG]
$req_size use-rng [new RNG]
$rsp_size use-rng [new RNG]

# set PackMime variables
$pm set-flow_arrive $flow_arrive
$pm set-req_size $req_size
$pm set-rsp_size $rsp_size

global ns_
$ns_ at $startTime "$pm start"
$ns_ at $timeToStop "$pm stop"
}

proc finish {} {
global ns_
$ns_ halt
$ns_ flush-trace
exit 0
}

set filesize 10000000
build_ftpclient $node_(0) $server 10.0 20.0 0 10000000
set filesize 10000000
build_ftpclient $node_(1) $server 25.0 50.0 1 10000000
for {set i 0} {$i < $opt(nn)} {incr i} {
$ns_ initial_node_pos $node_($i) 10
}
set opt(stop) 50.0
for {set i 0} {$i < $opt(nn) } {incr i} {
$ns_ at $opt(stop).0000010 "$node_($i) reset";
}
$ns_ at $opt(stop).0000010 "$BS(0) reset";
$ns_ at $opt(stop).1 "puts \"NS EXITING...\" ; $ns_ halt; $ns_ flush-trace"
puts "Starting Simulation..."
$ns_ run
#$ns_ at [expr $opt(stop)+.2 ] "finish"
exit 0
