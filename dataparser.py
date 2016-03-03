# this makes the assumption that the file we're using fits in memory
# if it doesn't you can make this a stream and do things like that but
# that would be significantly more complex

import pandas as pd
import sys
import random

def collect_rand():
	for index, item in enumerate(df.Time):
		if 1 > random.randrange(0,10):
			collect_index(index)

def collect_drive_changes():
	prevFlag = 0

	for index, flag in enumerate(df.Input):
		if index == 0:
			prevFlag = flag
	
		if flag == 2 and flag != prevFlag:
			collect_index(index)
		prevFlag = flag

def collect_torque_changes():
	prevAng = 0
	
	for index, ang in enumerate(df.Angle):
		if index == 0:
			prevAng = ang
		if ang != prevAng:
			collect_index(index)
		prevAng = ang

def collect_index(index):
	try:
		inputs = [df.LdSpd[index], df.Angle[index], df.CtrlPwr[index]]
		outputs = get_future_speeds(index, [5,10,20,30])
		# join on tab, convert everything to string, add newline
		row = make_row(inputs + outputs)
		outFile.write(row)
	except ValueError as err:
		pass
		# this happens when a sample would go out of bounds
		# when trying to look into the future
		# don't need to do anything, just skip the case

def get_future_speeds(index, offsets):
	return [get_future_speed(index, offset) for offset in offsets]

def get_future_speed(index, offset):
	if index + offset > len(df.LdSpd):
		raise ValueError('Invalid index')		
	return df.LdSpd[index + offset]

def make_row(l):
	return '\t'.join([str(x) for x in l]) + '\n'

if __name__ == '__main__':
	outFile = open('output', 'w')
	outFile.write(make_row(['LdSpd', 'Angle', 'CtrlPwr', 'T5', 'T10', 'T20', 'T30']))
	df = pd.read_csv(sys.argv[1], sep='\t');
	
	collect_torque_changes()
	collect_drive_changes()	
	collect_rand()

# output format:
# LdPsd	TrqArm	DPow	T1	T5	T16	T20

