# flowerpower

A very short guide (will be more detailed hopefully later):

1. Create a main folder for your dataset

2. Every subfolder in that folder is assumed to contain text files from a particular category

3. Every line in the text file is a document

4. The flower creator class: FlowerPower/src/main/java/de/l3s/analysis/topicflower/TopicFlowerCreator.java
    Start it with --help, it will tell which options are required

5. If your flower is garbage - there could be several reasons:

  pa) not enough text / size of the corpora too small (topics make no sense)
  
  pb) Too much overlapping information between the categories (all topics look the same)
  
  pc) Too many or to little k (topic number) is selected (all topics look the same, or are too general)
  
  pd) Text need cleaning (Strange, or meaningless terms in topic lables)
  
  
6. The package can support by some, but not all proplems:

  pa) It is possible to give additional folder with text as background information
  
  pb,pd) There is a class, which helps to identify the corpora specific stopwords: https://github.com/sergejzr/flowerpower/blob/master/FlowerPower/src/main/java/de/l3s/analysis/topicflower/StatisticsAnalysis.java
  run with -h, it will tell you the details
  
  pd) There is a class for text cleaning, it will create a copy of the folder structure and copy there all files from the main folder cleaned: https://github.com/sergejzr/flowerpower/blob/master/FlowerPower/src/main/java/de/l3s/analysis/topicflower/TextFileCleaner.java
  
  For pc) the user is responsible. Try different models, see which one makes sense. We have a tool for estimation of optimal k in the research pipeline, but it is not expected soon.
