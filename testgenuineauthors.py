#!/usr/bin/python
import sys, numpy,os,subprocess
# load all test authors
testfeatures = sys.argv[1]
trainfilename = sys.argv[2]
modelfilename = sys.argv[3]
data = [];
with open(testfeatures) as s:
    for line in s:
        line = line.strip()
        vals = [float(x) for x in line.split(",")]
        data.append(vals)
adata = numpy.array(data)
authors = numpy.unique(adata[:,adata.shape[1]-1])
counts = numpy.zeros(authors.shape[0])
i = 0
for a in authors:
    counts[i] = adata[adata[:,adata.shape[1]-1]==a,:].shape[0]
    i = i + 1
numblogstosample = 10
# output this to an intermediate file
prefix = 'intermediate/inter.authors';
tmpfilename = prefix+'.txt'
o = open(tmpfilename,'w')
for author in authors:
    features = adata[adata[:,adata.shape[1]-1]==author,0:adata.shape[1]-1]
    bloginds = range(0,features.shape[0])
    rinds = numpy.random.permutation(bloginds)
    for j in range(0,numblogstosample):
        feature = features[rinds[j],:]
        otherfeatures = numpy.delete(features,rinds[j],0)
        featuretiled = numpy.tile(feature,(rinds.shape[0]-1,1))
        diff = abs(featuretiled - otherfeatures)
        for row in diff:
            o.write(','.join('%f' % i for i in row)+',0.000000\n')
o.close()
afilename = prefix+'.arff'
sfilename = prefix+'.normalized.arff'
accfilename = prefix+'.results'

# convert into arff
os.system('python csv2arff.py '+tmpfilename+' '+afilename+' 263 testauthor')
# normalize with respect to training data
traintmpfilename = prefix+'.train.arff'
os.system('java -cp /Users/neetipokhriyal/Downloads/personal/weka-3-6-8/weka.jar weka.filters.unsupervised.attribute.Standardize -b -i '+trainfilename+' -o '+traintmpfilename+' -r '+afilename+' -s '+sfilename)
# run trained classifier
os.system('java -cp ~/Downloads/personal/weka-3-6-8/weka.jar weka.classifiers.functions.Logistic -l '+modelfilename+' -T '+sfilename+' -p 0 | grep "000000" > '+accfilename)
# process results
currentauthorid = 0
numlines = 0
predictions = []
with open(accfilename) as rfile:
    for resline in rfile:
        resline = resline.strip()
        tokens = [x for x in resline.split(" ")]
        if(tokens[1] == tokens[2]):
            pred = 1
        else:
            pred = 0
        predictions.append(pred)
        numlines = numlines + 1
        c = int(counts[currentauthorid]-1)
        if(numlines == c*numblogstosample):
            for i in range(0,numblogstosample):
                s = i*c
                e = (i+1)*c
                p = predictions[s:e]
                print float(sum(p))/float(c)
            predictions = []
            numlines = 0
            currentauthorid = currentauthorid + 1
# for each author:
# randomly sample one blog
# test 1:
# compute pdist with all other blogs of same author
# test in weka using the classifier
# take majority predictions as the final prediction

# test 2:
# randomly choose another author
# compute pdist with all blogs of the other author
# test in weka using the classifier
# take majority predictions as the final prediction

# run clean up
