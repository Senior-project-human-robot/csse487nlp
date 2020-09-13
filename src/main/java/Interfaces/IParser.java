package Interfaces;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public interface IParser {
    StanfordCoreNLP setup();
    IParseResultModel parse(String sentenceText);
}
