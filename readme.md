# MagMC

Presenting MagMC, the simulation tool for performing Markov Chain Monte Carlo simulations on lihium orthophosphates.

Run the program with the following command:

java -jar MCMC_Mag.jar

Then the commands to run can be input. Every command argument starting with a # as the first character will be treated as a comment. (This makes it possible to input commands from a file.)


## List of commands:
		disableConfigFile, disableVis, enableConfigFile, enableVis, help, loadConfigFile, mkDir, modifyelement, printDir, printParam, resetDir, saveConfigFile, scanBx, scanBy, scanBz, scanT, setDir, setParam, setVars. scananglebyz, scantbx, repeat/setrepeat, printparam, printvar, printdir, resetdir, mkdir, loadconfigfile, saveconfigfile, savecurrentconfig, enableconfigfile, disableConfigFile, enableSaveAllConfigs, disableSaveAllConfigs, 
		disableVis, disablePeriodic, enablePeriodic, runsim, wait, exit
		
### Description:
- help 
    - Displays this text.
- setCommonParam MCS N\_x N\_y N\_z ELEM
	- Sets the configuration for the simulation, that is the number of monte carlo steps, MCS, the dimensions N\_x N\_y and N\_z, and the default element ELEM for the array. The element can be either Co, Ni, Fe or Mn (and case matters).
- printParam
	- Prints the parameters for the configuration and the atom array. 
- setCommonVars T B\_x B\_y B\_z
	- Sets the default values for the thermodynamic parameters, T, B\_x, B\_y, B\_z, 
	- where B is the external applied magnetic field.
- scanpoint
- scanT T\_start dT T\_end
	- Makes a scan ramping up or down the temp, starting at T\_start.
- scanB[x/y/z]
	- Makes a scan ramping up or down the x/y/z- component of the applied field. 
	- The scan starts at B\_start ends at B\_end, and increments by dB.
- scanTB[x/y/z] T\_start dT T\_end B\_start dB B\_end
	- Makes a scan over a square in T-B space, over the interval 
	- [T\_start, T\_end] x [B\_start, B\_end] with increments of dT and dB.
- scanAngleB[x/y/z][x/y/z] Theta\_start dTheta Theta\_end
    - Makes a scan over different angles for the applied magnetic field. The angle is varied from Theta\_start to Theta\_end in increments of dTheta.
- modifyelement ELEM PARAM VALUE
	- Modify the parameter given by PARAM of the element ELEM, to be the value VALUE.
    	The parameters are numbered as: 
    	0: Ja, 
    	1: Jb, 
    	2: Jc, 
    	3: Jbc, 
    	4: Jac, 
    	5: Jab, 
    	6: Da, 
    	7: Db, 
    	8: Dc
    	e.g. to change the Jbc of Co to be 0.4, do
    		modifyelement Co 3 0.4
- setRepeat NUM
    - Sets that each simulation should be repeated a number of times given by NUM. (In effect multiplies the points added to the queue.)
- enableVis/disableVis
	- Enables or disables the use of a visualiser. If run on a computer cluster, this should be disabled. Is disabled by default.
- enablePeriodic/disablePeriodic
	- Enables or disables periodic boundary conditions. Is enabled by default.
- loadConfigFile FILE
	- Loads in the configuration of the atom array from a config-file FILE. This loads both what element should be at each position and the direction of the spin. 
- saveCurrentConfig FILE
	- Saves the configuration of the atom array to a config-file FILE. This saves both what element are at each position and the direction of the spin.
- mkDir DIR
	- Attempts to create the directory given by the string DIR.
- resetDir
	- Resets the directory for where to save results, to the default location. 
	(This is a folder named Data, which is created if it does not exist)
- printDir
	- Prints the default directory for which to save the results,  
- printVars
	- Prints the current queue of [T,B]-points to simulate.
- enableConfigFile/disableConfigFile
	- Enable/disable the use of a file for saving the configuration. If not set
- enableSaveAllConfigs/disableSaveAllConfig
- runSim [FILE]
	- Starts the simulation, with a copy of the current queue of [T,B]-points to simulate. If the optional parameter FILE is given, the results are saved there, otherwise result will be printed to standard out/system.out
- wait
	- Halts the program for 10 seconds. Intended for inspecting visually the resulting magnetic ordering. Cannot be used while a simulation is underway.
- exit/quit
	- Quits the program.

## Example usage:
	    
	    setCommon 1000 6 6 6 Co
	    setVars 2 0 0 0
	    scanT 30 -0.1 2
	    runSim results.dat
	    exit