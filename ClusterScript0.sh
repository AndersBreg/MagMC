#!/bin/sh
# embedded options to qsub - start with #PBS
# -- Name of the job ---
#PBS -N Sim
# -- specify queue --
#PBS -q hpc
# -- estimated wall clock time (execution time): hh:mm:ss --
#PBS -l walltime=24:00:00
# -- number of processors/cores/nodes --
#PBS -l nodes=1:ppn=4
# -- user email address --
# please uncomment the following line and put in your e-mail address,
# if you want to receive e-mail notifications on a non-default address
#PBS -M s134272@student.dtu.dk
# -- mail notification --
#PBS -m ae
# -- run in the current working (submission) directory --
if test X$PBS_ENVIRONMENT = XPBS_BATCH; then cd $PBS_O_WORKDIR; fi
# here follow the commands you want to execute
java -jar MCMC_Mag.jar < ./Program_files/Program_name.txt > ./Data/Sim.txt
