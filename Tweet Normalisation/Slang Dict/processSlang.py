import sys
import string

seperator = "###"

if(len(sys.argv) != 3):
	print "usage: python processSlang.py <ip_file> <op_file>"
	sys.exit(0)

ip_file = open(sys.argv[1])
op_file = open(sys.argv[2],'w')

word = ""
meaning = ""

for i,line in enumerate(ip_file):
	if(i%4 == 0):
		word = line.strip()
	elif(i%4 == 1):
		continue
	elif(i%4 == 2):
		meaning = line.strip()
	elif(i%4 == 3):
		op_file.write(word + seperator + meaning + "\n")

# Closing opened file
ip_file.close()
op_file.close()