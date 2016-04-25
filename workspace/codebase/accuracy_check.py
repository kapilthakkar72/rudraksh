import sys

args = sys.argv
op_file_name = args[2]
ip_file_name = args[1]

ip_file_object = open(ip_file_name)
op_file_object = open(op_file_name)

ip_file = ip_file_object.readlines()
op_file = op_file_object.readlines()

n = min(len(ip_file),len(op_file))

total = 0.0
correct_predicted = 0.0

i=0
while((i+2) < n):
	tag = ip_file[i+1]
	gold = ip_file[i+2]
	predicted = op_file[i+1]
	# Split
	gold_tokens = gold.split()
	predicted_tokens = predicted.split()
	tag_tokens = tag.split()
	# Compare
	for j in range(0,len(tag_tokens)):
		if(tag_tokens[j] == "OOV"):
			total = total +1
			if(gold_tokens[j] == predicted_tokens[j]):
				correct_predicted = correct_predicted +1
	i=i+4

ip_file_object.close()
op_file_object.close()


print "Total:" + str(total)
print "Correct Predicted:" + str(correct_predicted)
print "F-Score:" + str(correct_predicted/total)