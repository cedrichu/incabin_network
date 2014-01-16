#proc UniformErr {} {
#    set err [new ErrorModel/CL]
#    $err unit packet
#    return $err
#}

Mac/802_11 set Arf_ 1
Node/MobileNode set default_error_ .00
Node/MobileNode set max_channels_ 12
Node/MobileNode set switch_time_ .001
#ErrorModel/CL set BitRate 1000000
#power and noise values taken from "Efficient Geographic Routing in Multihop Wireless Networks", S. Lee, B. Bhatacharjee, S. Banerjee
Phy/WirelessPhy set ambient_noise_ .0000000000012
Phy/WirelessPhy set Pt_ 0.020

# 24 bytes sent at 1Mbps
Mac/802_11 set PreambleLength_ 144


set val(chan)           Channel/WirelessChannel    ;# Channel Type
set val(prop)           Propagation/TwoRayGround   ;# radio-propagation model
set val(netif)          Phy/WirelessPhy            ;# network interface type
set val(mac)            Mac/802_11                 ;# MAC type
set val(ifq)            Queue/DropTail/PriQueue    ;# interface queue type
set val(ll)             LL                         ;# link layer type
set val(ant)            Antenna/OmniAntenna        ;# antenna model
set val(ifqlen)         50                         ;# max packet in ifq
set val(nn)             6                          ;# number of mobilenodes
set val(rp)             AODV                  ;# routing protocol
set val(x)		800
set val(y)		800
set val(interfaces)     2                    ;#number of interfaces each node has
set val(length)         800                  ;#length of simulation


# Initialize Global Variables
set ns_		[new Simulator]
set tracefd     [open ./example.tr w]
#$ns_ use-newtrace
$ns_ trace-all $tracefd


# set up topography object
set topo       [new Topography]

$topo load_flatgrid $val(x) $val(y)

# Create God
create-god [expr $val(nn) * ($val(interfaces) + 1)]

# Create channels
#notice how channels are created ahead of time
for {set i 0} {$i < 15 } {incr i} {
    set chan($i) [new $val(chan)] 
}


#=====================================	
#must set ErrorModel/CL to get gray model
#set inerrProc_ "UniformErr"
set outerrProc_ ""
set FECProc_ ""
set propInstance_ [new $val(prop)]
set port [Node set rtagent_port_]


# configure node, please note the change below.
$ns_ node-config -adhocRouting $val(rp) \
		-llType $val(ll) \
		-macType $val(mac) \
		-ifqType $val(ifq) \
		-ifqLen $val(ifqlen) \
		-antType $val(ant) \
		-propType $val(prop) \
		-phyType $val(netif) \
		-topoInstance $topo \
		-agentTrace ON \
		-routerTrace OFF \
		-macTrace OFF \
		-movementTrace OFF \
		-channel $chan(0) 
#\
#                -IncomingErrProc UniformErr


for {set i 0} {$i < $val(nn) } {incr i} {
    # create the node
    set node_($i) [$ns_ node]  
    # get the routing agent
    set agent [$node_($i) set ragent_] 
    $node_($i) random-motion 0
}


set tcp1 [new Agent/TCP]
set sink1 [new Agent/TCPSink]
$node_(4) attach $tcp1 30
$node_(5) attach $sink1 30
$ns_ connect $tcp1 $sink1
set ftp1 [new Application/FTP]
$ftp1 attach-agent $tcp1

#command to switch channel of a node 
# note this is also exposed through the C interface
# while switching nodes will queue any packets to send and will drop any incomming pacets
#node switch-channel (interface to change) (destination channel)
$ns_ at 8.0 "$ftp1 produce 700"
$ns_ at 8.0 "puts Sending"
#$ns_ at 10.0 "$node_(3) switch-channel 1 11"
$node switch-channel 1 11

$ns_ at $val(length) "stop"
$ns_ at [expr {$val(length) +.0001}] "puts \"NS EXITING...\" ; $ns_ halt"

proc stop {} {
    global ns_ tracefd
    $ns_ flush-trace
    close $tracefd
}

puts "Starting Simulation..."
$ns_ run
