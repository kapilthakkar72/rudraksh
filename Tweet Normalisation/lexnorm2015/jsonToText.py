import json
import sys
from pprint import pprint

args = sys.argv
file_name = args[1]

with open(file_name) as data_file:    
    data = json.load(data_file)

for i in range(0,len(data)):
	ip_array = data[i]["input"]
	op_array = data[i]["output"]

	ip = ""
	op = ""

	for i in ip_array:
		ip = ip + i + " "

	for o in op_array:
		op = op + o + " "

	print ip.strip()
	print op.strip() + "\n"