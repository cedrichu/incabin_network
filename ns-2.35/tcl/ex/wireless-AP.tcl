# globals and flags
set ns [new Simulator]

$ns color 1 Blue
$ns color 2 Red

#number of nodes
set num_wired_nodes  1
set num_mobile_nodes 1
set num_bs_nodes     1 ;# number of base stations
set num_nodes [expr $num_wired_nodes + $num_mobile_nodes + $num_bs_nodes]
set bs_id $num_wired_nodes

# Parameter for wireless nodes
set opt(chan)           Channel/WirelessChannel    ;# channel type
set opt(prop)           Propagation/TwoRayGround   ;# radio-propagation model
set opt(netif)          Phy/WirelessPhy        ;# network interface type
set opt(mac)            Mac/802_11Ext               ;# MAC type
set opt(ifq)            Queue/DropTail/PriQueue       ;# interface queue type
set opt(ifqlen)         50
set opt(ll)             LL                         ;# link layer type
set opt(ant)            Antenna/OmniAntenna        ;# antenna model
set opt(ifqlen)         50                        ;# max packet in ifq
set opt(adhocRouting)   DSDV                  ;# routing protocol
set opt(x) 670 ;# X dimension of the topography
set opt(y) 670 ;# Y dimension of the topography

set opt(stop) 10

#By ArvinthanG for 802.11b

Antenna/OmniAntenna set Gt_ 1   ;#Transmit antenna gain
Antenna/OmniAntenna set Gr_ 1   ;#Receive antenna gain
#Phy/WirelessPhy set L_ 1.0     ;#system Loss Factor
#Phy/WirelessPhy set freq_ 2.472e9     ;#channel-13. 2.472GHz


#Lek: change to use 802.11a parameters
#802.11a default parameters

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



#set up for hierarchical routing
#(needed for routing over a basestation)
$ns node-config -addressType hierarchical
AddrParams set domain_num_ 2          ;# domain number
lappend cluster_num 1 1               ;# cluster number for each domain
AddrParams set cluster_num_ $cluster_num
#lappend eilastlevel $num_wired_nodes [expr $num_mobile_nodes + $num_bs_nodes] ;# number of nodes for each cluster          
lappend eilastlevel 1 2
AddrParams set nodes_num_ $eilastlevel

#Open the nam trace file
set nf [open wireless-AP.nam w]
$ns namtrace-all-wireless $nf $opt(x) $opt(y)
set ntr [open wireless-AP.tr w]
$ns trace-all $ntr



set chan [new $opt(chan)]
set topo [new Topography]
$topo load_flatgrid $opt(x) $opt(y)

# Create God
create-god [expr $num_mobile_nodes + $num_bs_nodes]

# creating wired nodes
for {set i 0} {$i < $num_wired_nodes} {incr i} {
    set W($i) [$ns node 0.0.$i]
    puts "wired node $i created"
}



# creating base station
$ns node-config -adhocRouting $opt(adhocRouting) \
                  -llType $opt(ll) \
                 -macType $opt(mac) \
                 -ifqType $opt(ifq) \
                 -ifqLen $opt(ifqlen) \
                 -antType $opt(ant) \
                 -propType $opt(prop)    \
                 -phyType $opt(netif) \
                 -channel $chan      \
                 -topoInstance $topo \
                 -wiredRouting ON \
                 -agentTrace ON \
                 -routerTrace OFF \
                 -macTrace ON  \
                 -movementTrace OFF\


set BS(0) [$ns node 1.0.0]
$BS(0) random-motion 0
puts "Base-Station node $bs_id created"
#provide some co-ord (fixed) to base station node
$BS(0) set X_ 200.0
$BS(0) set Y_ 300.0
$BS(0) set Z_ 0.0

# creating mobile nodes
$ns node-config -wiredRouting OFF
for {set i 0} {$i < $num_mobile_nodes} {incr i} {
    set wl_node_($i) [$ns node 1.0.[expr $i + 1]]
    $wl_node_($i) random-motion 0 ;# disable random motion
    puts "wireless node $i created ..."
    $wl_node_($i) base-station [AddrParams addr2id [$BS(0) node-addr]]
}

$wl_node_(0) set X_ 160
$wl_node_(0) set Y_ 310.0
$wl_node_(0) set Z_ 0.0


# linking of root to base-station node
$ns duplex-link $W(0) $BS(0) 100Mb 1ms DropTail


#WL-Node -0
set mobile_tcp [new Agent/TCP]
$mobile_tcp set class_ 2
set mobile_sink [new Agent/TCPSink]
$ns attach-agent $wl_node_(0) $mobile_tcp
$ns attach-agent $W(0) $mobile_sink
$ns connect $mobile_tcp $mobile_sink
set mobile_ftp [new Application/FTP]
$mobile_ftp attach-agent $mobile_tcp
$ns at 5.0 "$mobile_ftp start"


set src_udp0 [new Agent/UDP]
$src_udp0 set class_ 2
set dst_udp0 [new Agent/Null]
$ns attach-agent $wl_node_(0) $src_udp0
$ns attach-agent $W(0) $dst_udp0
set app0 [new Application/Traffic/CBR]
$app0 set packetSize_ 400
$app0 set rate_ 240Kb

$app0 attach-agent $src_udp0
$ns connect $src_udp0 $dst_udp0
#$ns at 0.0 "$app0 start"

set src_udp1 [new Agent/UDP]
$src_udp1 set class_ 1
set dst_udp1 [new Agent/Null]
$ns attach-agent $wl_node_(0) $src_udp1
$ns attach-agent $W(0) $dst_udp1
set app1 [new Application/Traffic/Exponential]
$app1 set packetSize_ 200
$app1 set rate_ 64kb
$app1 set burst_time_ 500ms
$app1 set idle_time_ 500ms
$app1 attach-agent $src_udp1

$ns connect $src_udp1 $dst_udp1
#$ns at 0.0 "$app1 start"


set src_udp2 [new Agent/UDP]
$src_udp2 set class_ 1
set dst_udp2 [new Agent/Null]
$ns attach-agent $wl_node_(0) $src_udp2
$ns attach-agent $W(0) $dst_udp2
set app2 [new Application/Traffic/Exponential]
$app2 set packetSize_ 200
$app2 set rate_ 64kb
$app2 set burst_time_ 500ms
$app2 set idle_time_ 500ms
$app2 attach-agent $src_udp2

$ns connect $src_udp2 $dst_udp2
#$ns at 0.0 "$app2 start"


# arvind code


# Define node initial position in nam
#for {set i 0} {$i < $num_mobile_nodes} {incr i} {
 #   $ns initial_node_pos $wl_node_($i) 20
 #  }

# Tell nodes when the simulation ends
for {set i 0} {$i < $num_mobile_nodes } {incr i} {
    $ns at 2.0 "$wl_node_($i) reset";
}

$ns at $opt(stop).01 "$BS(0) reset";
#$ns at $opt(stop).01 "$app0 stop"
#$ns at 2.0 "$ap stop"
#$ns at 2.0 "$app2 stop"
$ns at $opt(stop) "$mobile_ftp stop"

$ns at $opt(stop).1 "puts \"NS EXITING...\" ; $ns halt"

proc stop {} {
    global ns ntr nf
    close $ntr
    close $nf
}

# run the simulation
$ns run
