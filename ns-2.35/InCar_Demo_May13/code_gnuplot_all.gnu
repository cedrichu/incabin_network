set terminal postscript portrait enhanced dashed lw 1 "Helvetica" 14
set output "all.ps"
set multiplot layout 3, 1
set tmargin 2

set title "Amount of data transferred"
#unset key
set xlabel 'Time (s)'
set ylabel 'MB'
plot 'l' u 2:($8==1 ? $7/1000000 : 1/0) t "flow1" lc 1 lt 1, 'l' u 2:($8==2 ? $7/1000000 : 1/0) t "flow2" lc 3 lt 2

set title "Cumulative throughput"
#unset key
set xlabel 'Time (s)'
set ylabel 'Mbps'
plot 'l' u 2:($8==1 ? $7/($2-2)*8/1000000 : 1/0) t "flow1" lc 1, 'l' u 2:($8==2 ? $7/($2-2)*8/1000000 : 1/0) t "flow2" lc 3

set title "Per-packet end-to-end delay"
#unset key
set xlabel 'Time (s)'
set ylabel 'second'
plot 'l' u 2:($8==1 ? $3-$2 : 1/0) t "flow1" lc 1, 'l' u 2:($8==2 ? $3-$2 : 1/0) t "flow2" lc 3
unset multiplot
set output

