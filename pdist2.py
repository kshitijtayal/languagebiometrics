#!/usr/bin/python

import sys,numpy,time

firstfile = sys.argv[1]
secondfile = sys.argv[2]
outputfile = sys.argv[3]

genp = 0.01;
impp = 0.001;
sdata = [];
#slabels = [];
start = time.time()
with open(secondfile) as s:
    for sline in s:
        sline = sline.strip()
        svals = [float(x) for x in sline.split(",")]
        #starget = svals[len(svals)-1]
        #svals = svals[:len(svals)-1]
        sdata.append(svals)
        #slabels.append(starget)
sdata1 = numpy.array(sdata)
nrows = sdata1.shape[0]
end = time.time()
print "Loaded "+sys.argv[2]+" in "+str(end - start)+" seconds"
n = 0
te = open(outputfile,'w')
with open(firstfile) as f:
    for line in f:
        start = time.time()
        line = line.strip()
        vals = [float(x) for x in line.split(",")]
        #target = vals[len(vals)-1]
        #vals = vals[:len(vals)-1]
        #repmat vals
        avals = numpy.array(vals)
        avals1 = numpy.tile(avals,(nrows,1))
        diff = abs(avals1 - sdata1)
        ncols = diff.shape[1] - 1
        diff[diff[:,ncols] != 0,ncols] = 1
        for row in diff:
            if(row[ncols] == 0):
                v = numpy.random.binomial(1,genp,1)[0]
            else:
                v = numpy.random.binomial(1,impp,1)[0]
            if(v == 1):
                te.write(','.join('%f' % i for i in row)+'\n')
        n = n + 1
        end = time.time()
        print "processed line "+str(n)+" in "+str(end-start)+" seconds."

te.close()
