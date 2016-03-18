#!/usr/bin/python
# this makes the assumption that the file we're using fits in memory
# if it doesn't you can make this a stream and do things like that but
# that would be significantly more complex

import pandas as pd
import sys
import random

# from http://stackoverflow.com/questions/15389768/standard-deviation-of-a-list
def mean(data):
    """Return the sample arithmetic mean of data."""
    n = len(data)
    if n < 1:
        raise ValueError('mean requires at least one data point')
    return sum(data)/float(n)

def _ss(data):
    """Return sum of square deviations of sequence data."""
    c = mean(data)
    ss = sum((x-c)**2 for x in data)
    return ss

def stddev(data):
    """Calculates the sample standard deviation."""
    n = len(data)
    if n < 2:
        raise ValueError('variance requires at least two data points')
    ss = _ss(data)
    pvar = ss/(n-1) # the population variance

    return pvar**0.5
def normalize(l, normalizationFile):
	print "normalizing..."
	m = mean(l)
	dev = stddev(l)

	normalizationFile.write(str(m) + "\t" + str(dev) + "\n")

	return [(i-m)/dev for i in l]

def normalize_input():
	f = open("normalizationData", "w")

	df.LdSpd = normalize(df.LdSpd, f)
	df.Angle = normalize(df.Angle, f)
	df.CtrlPwr = normalize(df.CtrlPwr, f)

	f.close()

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
		outputs = get_future_speeds(index, [5,10,15,20])
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
	if index + offset >= len(df.LdSpd):
		raise ValueError('Invalid index')		
	return df.LdSpd[index + offset]

def make_row(l):
	return '\t'.join([str(x) for x in l]) + '\n'

# output format:
# LdPsd	TrqArm	DPow	T1	T5	T16	T20

if __name__ == '__main__':
	if len(sys.argv) != 2:
		print "arg format: input_data"
		sys.exit(0)
	outFile = open('training-set.csv', 'w')
	#outFile.write(make_row(['LdSpd', 'Angle', 'CtrlPwr', 'T5', 'T10', 'T20', 'T30']))
	df = pd.read_csv(sys.argv[1], sep='\t');

	normalize_input()
	
	collect_torque_changes()
	collect_drive_changes()	
	collect_rand()
	
