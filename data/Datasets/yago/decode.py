import sys
from os.path import basename, dirname, join

for path in sys.argv[1:]:
    with open(path, 'r') as f:
        with open(join(dirname(path),'uni_'+basename(path)), 'w') as f2:
            for l in f:
                f2.write('\t'.join([e.encode('unicode_escape') for e in l.decode('utf8').strip('\r\n').split('\t')]) + '\n')
