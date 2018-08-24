# Are All People Married? Determining Obligatory Attributes in Knowledge Bases

&copy; 2018 Jonathan Lajus &amp; Fabian M. Suchanek

> An attribute is obligatory for a class in a Knowledge Base (KB), if all instances of the class have the attribute in the real world. For example, hasBirthDate is an obligatory attribute for the class Person, while hasSpouse is not. In this paper, we propose a new way to model incompleteness in KBs. From this model, we derive a method to automatically determine obligatory attributes – using only the data from the KB. Our algorithm can detect such attributes with a precision of up to 90%.

## Publication

- [Jonathan Lajus](https://lajus.github.io), [Fabian M. Suchanek](https://suchanek.name):
  “Are All People Married? Determining Obligatory Attributes in Knowledge Bases” ([pdf](Are%20All%20People%20Married.pdf)
  Full paper at the [Web Conference](http://www2018.thewebconf.org/) (WWW), 2018

## Datasets
We ran our system on two datasets
- [YAGO](http://yago-knowledge.org) (version [3](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/archive/))
- [Wikidata](http://wikidata.org) ([Truthy 2017-06-27](https://dumps.wikimedia.org/wikidatawiki/entities/), see [this](data/Datasets/wikidata/README))

## Experimental Data

For each experiment, we report:
- The results given by the different algorithms ([here](data/Results/results/);
- The gold standard used ([here](data/Gold%20Standard/));
- The per-instances evaluation for each methods and relations ([here](data/Results/evaluation-results/).

### Raw results

In "data/Results/results" we provide tab-separated-value files of any class returned by any method, parameter and predicate.

IMPORTANT NOTE: here we only output upper-classes in the taxonomy

"predicate \t class" stands for "class(x) => \exists y. predicate(x, y)"
and "predicate-1 \t class" stands for "class(y) => \exists x. predicate(x, y)"

### Evaluation

We provide tab-separated-value files with the results of the experiments.
They contain for every method, parameter, and predicate:
- The number of true positives
- The number of predicted instances
- The number of instances derived from the gold standard
- The precision, recall, and F1 per predicate
- There is also their "NF" (for "New Facts") counterpart where we do not consider any instance already having the predicate in the KB (not used in the paper).

## Source Code

The source code in the form of a Maven project can be found in the directory [src](src/)

Dependencies not available in Maven are in [src/lib](src/lib/)

## Runnables

All the jar files used for the experiments can be found in the [run](run/) directory.

Refer to [this documentation](run/USAGE) for usage information.

