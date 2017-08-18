cd Tool\ark-tweet-nlp-0.3.2
java -Xmx1000m -jar ark-tweet-nlp-0.3.2.jar --output-format conll ..\..\Data\Input\Courpus(no_id).txt > ..\..\Data\Preprocessing\\POS_corpus(conll).txt
java -Xmx1000m -jar ark-tweet-nlp-0.3.2.jar ..\..\Data\Input\Courpus(no_id).txt > ..\..\Data\Preprocessing\POS_corpus.txt
