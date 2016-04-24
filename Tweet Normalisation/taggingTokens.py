import sys
import string

if(len(sys.argv) != 3):
	print "usage: python taggingTokens.py <ip_file> <op_file>\nInput File should be of format: \nOriginal Tweet\nCorrected Tweet\n<Blank Line>"
	print "Output File will be:\nOriginal Tweet\nTag of <NO,IV,OOV>\nCorrected Tweet\n<Blank Line>"
	sys.exit(0)

###########################################################
###					SOME HELPER FUNCTIONS				###
###########################################################

# Load Words from Dictionary to get In Vocabulary (IV) words.
dictionary_file = open('my.dict')

dictionary = set()
for i,line in enumerate(dictionary_file):
	dictionary.add(line.strip().lower())

dictionary_file.close()


def arrayToString(array):
	string = ""
	for element in array:
		string = string + str(element) + " "
	return string


def getTags(tokens):
	# Punctuations
	punc = set(string.punctuation)
	tags = []
	for token in tokens:
		# Check for Hashtag
		if(token[0] == '@' || token[0] == '#'):
			tags.append("NO")
		elif(token in punc):
			tags.append("NO")
		elif(token in dictionary):
			tags.append("IV")
		elif(token.isdigit()):
			tags.append("NO")
		else:
			tags.append("OOV")
	return tags


###########################################################
###########################################################




ip_file = open(sys.argv[1])
op_file = open(sys.argv[2],'w')

# Read Tweets one by one and Tage it
for i,line in enumerate(ip_file):
	if(i%3 == 0):
		original = line
		tokens = line.split()
	elif(i%3 == 1):
		correctedTweet = line
	elif(i%3 == 2):
		tags = getTags(tokens)
		# Print
		op_file.write(original+"\n")
		op_file.write(arrayToString(tags)+"\n")
		op_file.write(correctedTweet+"\n")
		op_file.write("\n")



# Closing opened file
ip_file.close()
op_file.close()