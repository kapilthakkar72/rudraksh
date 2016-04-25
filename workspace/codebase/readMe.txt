./crf	model file for segmenting informal words using CRF++
./src	source code
./lib	libfile
./data	data file like IV dictionary, phonetic info for IV word and syllables

running instruction:
export LD_LIBRARY_PATH=./lib:$LD_LIBRARY_PATH
java -jar norm.jar <INPUT_FILE> <OUTPUT_FILE> (<weightWord> <weightSound> <weightSeg> <weightLM>)
#(<weightWord> ...) is used to adjust relative importance of every measure, and if not specified, the default value is (0.7,0.3,0.0,1.0)
# perharps because of the file paths, you cannot run the jar file , you can go directly to ./src/nomalizationWork/NormBatch
