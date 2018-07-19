#/usr/bin/python3

import csv
import collections
import sys

#PREDROOT = "<bornBefore_"
PREDROOT = "<b_"
PREDTYPE = "rdf:type"
PREDSUBC = "rdfs:subClassOf"
TOPC = "owl:Thing"

def load(fn, classSizeThreshold, intervals, birthDateRelation):
    born = {}
    with open(fn, 'r', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter="\t")
        for row in reader:
            if row[2] == birthDateRelation:
                try:
                    birthYear = int(float(row[4]))
                    if birthYear > intervals[0] and birthYear <= intervals[-1]:
                        i = 1
                        while birthYear > intervals[i]: i += 1 
                        born.setdefault(intervals[i], []).append(row[1])
                except ValueError:
                    pass
    y = list(born.keys())[:]
    y.sort()
    for i in range(len(y)-1):
        if len(born[y[i]]) < classSizeThreshold:
            born[y[i+1]].extend(born[y[i]])
            del(born[y[i]])
    y = list(born.keys())[:]
    y.sort()
    if len(born[y[-1]]) < classSizeThreshold:
        born[y[-1]].extend(born[y[-2]])
        del(born[y[-2]])
    return born

def printType(born, out=sys.stdout):
    for y in born:
        for e in born[y]:
            print(e, PREDTYPE, PREDROOT+str(y)+">", sep="\t", file=out)

def printTransitiveType(born, out=sys.stdout):
    ys = list(born.keys())
    ys.sort()
    for i in range(len(ys)):
        for e in born[ys[i]]:
            for j in range(i, len(ys)):
                print(e, PREDTYPE, PREDROOT+str(ys[j])+">", sep="\t", file=out)
            if TOPC: print(e, PREDTYPE, TOPC, sep="\t", file=out)
            
def printTaxo(born, out=sys.stdout):
    ys = list(born.keys())
    ys.sort()
    for i in range(len(ys)-1):
        print(PREDROOT+str(ys[i])+">", PREDSUBC, PREDROOT+str(ys[i+1])+">", sep="\t", file=out)
    if TOPC: print(PREDROOT+str(ys[-1])+">", PREDSUBC, TOPC, sep="\t", file=out)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("usage: generateClasses.py [yagoDateFacts.tsv]", file=sys.stderr)
        exit(1)
    born = load(sys.argv[1], 50, range(1700, 2021, 10), "<wasBornOnDate>")
    printType(born, open("bornType.tsv", "w", encoding="utf-8"))
    printTaxo(born, open("bornTaxonomy.tsv", "w", encoding="utf-8"))
    printTransitiveType(born, open("bornTransitiveType.tsv", "w", encoding="utf-8"))
