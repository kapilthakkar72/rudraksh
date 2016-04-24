import json
import sys
from pprint import pprint

args = sys.argv
file_name = args[1]

with open(file_name) as data_file:    
    data = json.load(data_file)

for i in range(0,len(data)):
	op_array = data[i]["output"]
	op = ""

	for o in op_array:
		op = op + o + " "

	print op.strip() + "\n"
