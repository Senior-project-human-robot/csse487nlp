package Models;

import Interfaces.IParseResultModel;

import java.util.List;

public class ParagraphParseResult implements IParseResultModel {
    private List<String> sentences;

    public ParagraphParseResult(List<String> sentences){
        this.sentences = sentences;
    }

    public List<String> getSentences(){
        return sentences;
    }
    public Integer getSize(){ return sentences.size(); }
}
