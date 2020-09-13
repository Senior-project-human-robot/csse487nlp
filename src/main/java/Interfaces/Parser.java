package Interfaces;

import Models.ParseResultModel;

public interface Parser {
    ParseResultModel parse(String sentenceText);
}
