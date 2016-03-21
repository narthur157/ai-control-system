#!/bin/python

# opens a given file and makes sure that the relation between
# control power and load speed is monotonic
# So we check that when control power changes, load speed does not decrease

import pandas as pd
import sys

def check_match(spdIncreasing, pwrIncreasing):
	if spdIncreasing is not None and pwrIncreasing is not None:
		if spdIncreasing != pwrIncreasing:
			if spdIncreasing:
				print "ERR: Speed increased while power decreased"
				sys.exit(1)
			else:
				print "ERR: Speed decreased while power increased"
				sys.exit(1)

if __name__ == '__main__':
	if len(sys.argv) != 2:
		print "arg format: input_data"
		sys.exit(1)
	
	df = pd.read_csv(sys.argv[1], sep='\t')

	pwrIncreasing = None
	spdIncreasing = None
	
	for i in enumerate(df.Time):
		if curPwr is None:
			curPwr = df.CtrlPwr[i]
			curSpd = df.LdSpd[i]
		else: 
			prevPwr = curPwr
			prevSpd = curSpd
			
			if curPwr > prevPwr:
				pwrIncreasing = True
			if curPwr < prevPwr:
				pwrIncreasing = False

			if curSpd > prevSpd:
				spdIncreasing = True
			if curSpd < prevSpd:
				spdIncreasing = False
			
			check_match(spdIncresaing, pwrIncreasing)

