package Workers;

import Models.SentenceParseResult;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import org.json.JSONObject;

import java.util.*;

public class SentenceParser {

    private ArrayList<JSONObject> refList;
    private Boolean hasRetriedParsing = false;
    private Boolean isTargetFromIt = false;
    private Boolean isVBFound = true;
    private Boolean callerObj = false;
    private final StanfordCoreNLP pipeline;
    private int seqNum;
    private JSONObject targetFromIt = new JSONObject();
    private JSONObject previousTarget = new JSONObject();
    private static HashSet<String> directionSet = new HashSet<>(Arrays.asList("top", "bottom", "left", "right", "in_front_of", "behind"));
    private final static HashSet<String> gestureSet = new HashSet<>(Arrays.asList("this", "that"));
    private final static HashSet<String> nameSet = new HashSet<>(Arrays.asList("name", "call", "define"));
    private final static String NOTFOUND = "???";

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.refList = new ArrayList<>();
        this.pipeline = this.setup();
        this.seqNum = 0;
    }

    /**
     * This method will setup the pipeline for parsing the paragraph
     * @return new StanfordCoreNLP
     */
    private StanfordCoreNLP setup() {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // build and return pipeline
        return new StanfordCoreNLP(props);
    }

    /**
     * This method will parse the sentence provided and return a Parse Result
     * IParseResultModel Object containing all the information needed for output
     * 
     * @param sentence the sentence to be parsed
     * @return a SentenceIParseResult object that containing all the information
     *         needed for output
     */
    public SentenceParseResult parse(int seqNum, CoreSentence sentence, CoreSentence previousSentence) {
        resetData(seqNum);
        System.out.println("--------------------------------");
        System.out.println(sentence.text());
        Tree tree =
                sentence.coreMap().get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependencies = sentence.dependencyParse();
        System.out.println(dependencies);
        System.out.println(tree);

        // get the command
        IndexedWord direction;
        IndexedWord refObj;
        IndexedWord targetIndexedWord = null;
        String receiver = NOTFOUND;
        String directionString = NOTFOUND;
        String naming = NOTFOUND;

        IndexedWord sentenceMain = findSentenceMain(dependencies);

        receiver = findReceiver(dependencies, sentenceMain);
        String command = findCommand(dependencies, sentenceMain);
        if (!isVBFound && !hasRetriedParsing){
            // If sentence main is not found, we will try to add "Please" to the front of the sentence and try again.
            hasRetriedParsing = true;
            return retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
        } else if (nameSet.contains(command) && !hasRetriedParsing) {
            // If the sentence is using naming, such as "name" and "define", try it separately with retryWhenCommandIsName method.
            hasRetriedParsing = true;
            return retryWhenCommandIsName(sentence.text(), previousSentence);
        } else {
            // The case that sentence main is found.

            // Searching for the target object and the verb compound part of the command
            if(!isVBFound && hasRetriedParsing) {
                command = NOTFOUND;
                targetIndexedWord = findTargetWOMain(dependencies);
            } else {
                targetIndexedWord = findTarget(dependencies, sentenceMain);
                if(targetIndexedWord == null && !this.hasRetriedParsing){
                    this.hasRetriedParsing = true;
                    return retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
                }
            } 

            // Using relationships to find the reference objects, their modifiers, and whether Gesture is used on them
            Set<IndexedWord> oblSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl"));
            Set<IndexedWord> oblToSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:to"));
            Set<IndexedWord> oblUnderSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:under"));
            Set<IndexedWord> oblBelowSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:below"));
            Set<IndexedWord> oblOnSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:on"));

            if (!oblSet.isEmpty()) { // Reference clarification
                directionString = ((IndexedWord) oblSet.toArray()[0]).word();
                // refList.add(generateJSONObj(null, dependencies, false));
            } else if (!oblToSet.isEmpty()) {
                for (IndexedWord indexedWord : oblToSet) {
                    if(isDirectional(indexedWord.word())) {
                        direction = indexedWord;
                        directionString = direction.word().toLowerCase();
                        Set<IndexedWord> refSet = dependencies.getChildrenWithReln(direction, GrammaticalRelation.valueOf("nmod:of"));
                        if (!refSet.isEmpty()) {
                            refObj = (IndexedWord) refSet.toArray()[0];
                            refList.add(generateJSONObj(refObj, dependencies, false));
                        }
                    } else { // "to the place between..." condition
                        Set<IndexedWord> nmodsWords = generateNmodSet(indexedWord, dependencies);
                        if (!nmodsWords.isEmpty()) {
                            for(IndexedWord word : nmodsWords) {
                                if(dependencies.reln(indexedWord, word) != null) {
                                    switch (dependencies.reln(indexedWord, word).getSpecific()) {
                                        case "between":
                                            directionString = "between";
                                            refObj = (IndexedWord) word;
                                            refList.add(generateJSONObj(refObj, dependencies, false));
                                            break;
                                        default:
                                            System.err.println("New cases for to the place ...");
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!oblUnderSet.isEmpty()) { //relation under
                directionString = "under";
                refObj = (IndexedWord) oblUnderSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies, false));
            } else if (!oblBelowSet.isEmpty()) { //relation below
                directionString = "below";
                refObj = (IndexedWord) oblBelowSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies, false));
            } else if (!oblOnSet.isEmpty()) { // relation on
                directionString = "on";
                refObj = (IndexedWord) oblOnSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies, false));
            } else if (targetIndexedWord != null){
                Set<IndexedWord> nmodsWords = generateNmodSet(targetIndexedWord, dependencies);
                // add(GrammaticalRelation.valueOf("nmod:of"));
                // System.out.println("~~~~~~~~~~~~~~~~~~~~~ " + nmodsWords.size());
                if (nmodsWords.size() >= 1) {
                    IndexedWord dep = (IndexedWord) nmodsWords.toArray()[0];
                    // self reference
                    Set<IndexedWord> possesion = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:poss"));
                    if(!possesion.isEmpty()) {
                        IndexedWord possesor = (IndexedWord) possesion.toArray()[0];
                        if(possesor.word().toString().equals("your")) {
                            directionString = dep.word().toString();
                            if(isDirectional(directionString)) {
                                // self with direction - "on your right"
                                refList.add(generateSelfObj(null));
                            } else {
                                // self with possession - "in your basket"
                                Set<IndexedWord> posDir = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("case"));
                                IndexedWord relation = (IndexedWord) posDir.toArray()[0];
                                directionString = relation.word().toString();
                                refList.add(generateSelfObj(dep));
                            }
                        }
                    } else {
                        Set<IndexedWord> refSet = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:of"));
                        switch (dependencies.reln(targetIndexedWord, dep).getSpecific()) {
                            case "on": // on the top of
                                directionString = "on";
                                if (!refSet.isEmpty()) {
                                    refObj = (IndexedWord) refSet.toArray()[0];
                                    refList.add(generateJSONObj(refObj, dependencies, false));
                                } else { // pick up the block on it
                                    if(dep.word().equals("it")) {
                                        refList.add(previousTarget);
                                    }
                                }
                                break;
                            case "to": // to the left/right of
                                directionString = dep.word();
                                if (!refSet.isEmpty()) {
                                    refObj = (IndexedWord) refSet.toArray()[0];
                                    refList.add(generateJSONObj(refObj, dependencies, false));
                                }
                                break;
                            default:
                                directionString = dependencies.reln(targetIndexedWord, dep).getSpecific();
                                for (IndexedWord d : nmodsWords) {
                                    refList.add(generateJSONObj(d, dependencies, false));
                                }
                        }
                    }
                }

                //If the command verb is naming, find the naming part in the sentence
                if(nameSet.contains(command)) {
                    naming = findNaming(dependencies, sentenceMain, command);
                }
                
            }
           
        }
        System.out.println("-----------------------");
        System.out.println("Full Command: " + command.toLowerCase());
        System.out.println("-----------------------");

        
        System.out.println(command);
        SentenceParseResult result = new SentenceParseResult();
        result.command = command.toLowerCase();
        result.target = isTargetFromIt? targetFromIt : generateJSONObj(targetIndexedWord, dependencies, true);
        previousTarget = (JSONObject) result.target;
        result.refList = refList;
        result.direction = directionString;
        result.naming = naming;
        result.receiver = receiver;
        result.seqNum = seqNum;
        result.originalCoreSentence = sentence;
        hasRetriedParsing = false;
        return result;
    }

    /**
     * Extract all the xcomp/obl:as parts in the sentence
     * @param dependencies 
     * @param sentenceMain
     * @param command
     * @return the naming (xcomp/obl:as) part
     */
    private String findNaming(SemanticGraph dependencies, IndexedWord sentenceMain, String command) {
        // Naming
        String naming = NOTFOUND;

        Set<IndexedWord> xcompSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("xcomp"));
        Set<IndexedWord> oblAsSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:as"));
        if(!xcompSet.isEmpty()){
            naming = ((IndexedWord)xcompSet.toArray()[0]).word().toLowerCase();
        } else if(!oblAsSet.isEmpty()){
            naming = ((IndexedWord)oblAsSet.toArray()[0]).word().toLowerCase();
        }
        
        return naming;
    }


    /**
     * Find target based on the dependencies of sentenceMain, using "obj" relationship
     * @param dependencies
     * @param sentenceMain
     * @return the target IndexedWord in the sentece
     */
    private IndexedWord findTarget(SemanticGraph dependencies, IndexedWord sentenceMain) {
        IndexedWord targetIndexedWord = null;
        Set<IndexedWord> targetObjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));
        // Set<IndexedWord> targetDepSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("dep"));
        // Set<IndexedWord> tempDepSet = null;
        // if(callerObj) {
        //     for (IndexedWord indexedWord : targetDepSet) {
        //         if(indexedWord.tag().equals("IN")) 
        //             tempDepSet = dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("dep"));
        //             for (IndexedWord tempWord : tempDepSet) {
        //                 targetIndexedWord = tempWord;
        //             }
        //     }
        // } else {
        for (IndexedWord indexedWord : targetObjSet) {
            targetIndexedWord = indexedWord;
            if(targetIndexedWord.word().toLowerCase().equals("it")){
                isTargetFromIt = true;
                targetFromIt = this.previousTarget;
            }
            if(!indexedWord.tag().equals("NN")){
                continue;
            }
        }
        return targetIndexedWord;
    }

    /**
     * Build up the command to be the word of the sentenceMain; find the compound part of the command through "compound:prt"
     * @param dependencies
     * @param sentenceMain
     * @return the String of the command and its compound if it exists
     */
    private String findCommand(SemanticGraph dependencies, IndexedWord sentenceMain) {
        StringBuilder command = new StringBuilder(sentenceMain.word().toLowerCase());
        Set<IndexedWord> compoundPrtSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("compound:prt"));
        if (!compoundPrtSet.isEmpty()) {
            for (IndexedWord prtWord : compoundPrtSet) {
                // Found the verb compound part of the command
                command.append(" ").append(prtWord.word().toLowerCase());
            }
        }
        return command.toString();
    }

    /**
     * Find the first word that has a tag "VB"
     * @param dependencies
     * @return the indexword for the command first verb part
     */
    private IndexedWord findSentenceMain(SemanticGraph dependencies) {
        // Searching for the command verb part
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        if (!dependencies.getFirstRoot().tag().equals("VB")){
            isVBFound = false;
            for(IndexedWord rootChild : dependencies.getChildren(sentenceMain)){
                if(rootChild.tag().equals("VB")){
                    sentenceMain = rootChild;
                    isVBFound = true;
                    break;
                }
            }
        }
        return sentenceMain;
    }

    /**
     * Find the receiver of the action if the receiver exists, e.g. "Hand me the red block," "Hand Alice that blue block."
     * @param dependencies
     * @param sentenceMain
     * @return the String of the receiver's name, else return NOTFOUND
     */
    private String findReceiver(SemanticGraph dependencies, IndexedWord sentenceMain) {
        String receiver = NOTFOUND;
        Set<IndexedWord> iobjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("iobj"));
        Set<IndexedWord> objSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));

        if(!iobjSet.isEmpty()) {
            for (IndexedWord word : iobjSet) {
                if(word.word().equals("me")) receiver = "caller";
                else receiver = word.word();
            }
        } else if(!objSet.isEmpty()) {
            for (IndexedWord word : objSet) {
                if(word.tag().equals("PRP") && !word.word().equals("it")) {
                    callerObj = true;
                    if(word.word().equals("me")) receiver = "caller";
                    else receiver = word.word();
                }
            }
        }

        return receiver;
    }
    
    /**
     * Reset every parameter
     * @param seqNum
     */
    private void resetData(int seqNum){
        this.seqNum = seqNum;
        this.isTargetFromIt = false;
        this.refList = new ArrayList<>();
        this.isVBFound = true;
        this.callerObj = false;
    }

    /**
     * When there is no sentence main found in the sentence.
     * Add a Please at the beginning of the sentence and retry the parsing.
     * @param sentenceString
     * @param previousSentence
     * @return recursion on parsing
     */
    private SentenceParseResult retryWhenSentenceMainNotFound(String sentenceString, CoreSentence previousSentence){
        // add please at the begin of the sentence and change the first char of the original string to lower case.
        CoreDocument doc = new CoreDocument("Please " + sentenceString.substring(0, 1).toLowerCase() + sentenceString.substring(1));
        // annotate
        pipeline.annotate(doc);
        return parse(seqNum,doc.sentences().get(0),previousSentence);
    }
    
    /**
     * When there is the command verb is name/call
     * Replace name/call to define and retry the parsing.
     * @param sentenceString
     * @param previousSentence
     * @return recusion of the parsing
     */
    private SentenceParseResult retryWhenCommandIsName(String sentenceString, CoreSentence previousSentence){
        sentenceString = sentenceString.replaceFirst("Call", "define");
        sentenceString = sentenceString.replaceFirst("call", "define");
        sentenceString = sentenceString.replaceFirst("Name", "define");
        sentenceString = sentenceString.replaceFirst("name", "define");

        if (!sentenceString.contains(" as ")) { // not inclusive, as the sentence may already contain an "as" for other purposes && before the last word might not be the proper position
            String[] words = sentenceString.split(" ");
            words[words.length-1] = "as " + words[words.length-1];
            sentenceString = String.join(" ", words);
        }

        // System.out.println("+++++++++++++++++++++++++++++" + sentenceString + "+++++++++++++++++++++++++++++");

        CoreDocument doc = new CoreDocument(sentenceString);
        // annotate
        pipeline.annotate(doc);
        return parse(seqNum,doc.sentences().get(0),previousSentence);
    }

    /**
     * Extract all the amod-obl:npmod and compound-amod parts from the sentence.
     * @param word
     * @param graph
     * @return the ArrayList of modifiers
     */
    private ArrayList<String> getMods(IndexedWord word, SemanticGraph graph){
        ArrayList<String> mods = new ArrayList<>();
        for(IndexedWord mod :graph.getChildrenWithReln(word,GrammaticalRelation.valueOf("amod"))){
            mods.add(mod.word().toLowerCase());
            for (IndexedWord npmod : graph.getChildrenWithReln(mod, GrammaticalRelation.valueOf("obl:npmod"))){
                mods.add(npmod.word().toLowerCase());
            }
        }
        for(IndexedWord mod :graph.getChildrenWithReln(word,GrammaticalRelation.valueOf("compound"))){
            mods.add(mod.word().toLowerCase());
            for (IndexedWord amod : graph.getChildrenWithReln(mod, GrammaticalRelation.valueOf("amod"))){
                mods.add(amod.word().toLowerCase());
            }
        }
        return mods;
    }

    /**
     * Check if there is grammatic relation "det" or "mark"
     * @param word
     * @param dependencies
     * @param isTarget
     * @return if there is gesture Used in the sentence
     */
    private Boolean isGestureUsed(IndexedWord word, SemanticGraph dependencies, Boolean isTarget){
        if(isTarget && callerObj) return true;
        for(IndexedWord indexedWord : dependencies.getChildrenWithReln(word,GrammaticalRelation.valueOf("det"))){
            if(gestureSet.contains(indexedWord.word().toLowerCase())) {
                return true;
            }
        }
        for(IndexedWord indexedWord : dependencies.getChildrenWithReln(word,GrammaticalRelation.valueOf("compound"))){
            for(IndexedWord inner : dependencies.getChildrenWithReln(indexedWord,GrammaticalRelation.valueOf("mark"))){
                if(gestureSet.contains(inner.word().toLowerCase())) {
                    // System.out.println("+++++++++++++++++++" + "here" + "+++++++++++++++++");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will generate a JSON Object that having the information about 
     * the provided object, its modifiers, and whether gesture is used
     * @param obj the Item that the JSON object will be generated upon
     * @param dependencies the entire sentence main part in dependency parsing format stored as Semantic Graph
     * @param isTarget a Boolean target that indicates whether it is the target. if the object is our target, it will be true, otherwise false
     * @return a JSONObject object that containing the information about the provided object, its modifiers, and whether gesture is used
     */
    private JSONObject generateJSONObj(IndexedWord obj, SemanticGraph dependencies, Boolean isTarget){
        JSONObject refMods = new JSONObject();
        if(obj == null){
            refMods.put("Item", NOTFOUND);
        }
        else if(obj.word().toLowerCase().equals("you")) {
            return generateSelfObj(null);
        } else {
            refMods.put("Item", obj.word().toLowerCase());
            refMods.put("Mods", getMods(obj, dependencies));
            refMods.put("Gesture", isGestureUsed(obj, dependencies, isTarget));
        }
    
        return refMods;
    }

    /**
     * Generate self-referencing object. Add possession when needed.
     * @param possesion
     * @return self JSON object 
     */
    private JSONObject generateSelfObj(IndexedWord possesion) {
        JSONObject refMods = new JSONObject();
        refMods.put("Item", "self");
        if(possesion != null) {
            refMods.put("Belonging", possesion.word());
        }
        return refMods;
    }

    /**
     * Generate a set of IndexedWord that has the nmods tag of the first parameter indexedWord
     * @param indexedWord
     * @param dependencies
     * @return a set containing IndexedWord of all words that hold the specified GrammaticalRelation 
     */
    private Set<IndexedWord> generateNmodSet(IndexedWord indexedWord, SemanticGraph dependencies) {
        Set<IndexedWord> nmodsWords = dependencies.getChildrenWithRelns(indexedWord, new ArrayList<GrammaticalRelation>() {
            {
                add(GrammaticalRelation.valueOf("nmod:in_front_of"));
                add(GrammaticalRelation.valueOf("nmod:behind"));
                add(GrammaticalRelation.valueOf("nmod:between"));
                add(GrammaticalRelation.valueOf("nmod:on_top_of"));
                add(GrammaticalRelation.valueOf("nmod:on"));
                add(GrammaticalRelation.valueOf("nmod:in"));
                add(GrammaticalRelation.valueOf("nmod:to"));
            }
        });
        return nmodsWords;
    }
    
    /**
     * Helper function; check whether an string is a direction word
     * @param arg
     * @return boolean telling whether arg is within directionSet
     */
    private boolean isDirectional(String arg) {
        return directionSet.contains(arg);
    }

    /**
     * When sentenceMain is not found, find the target based on the rest of the sentences
     * @param dependencies
     * @return null if target not found, else return the target
     */
    private IndexedWord findTargetWOMain(SemanticGraph dependencies){
        IndexedWord current = dependencies.getFirstRoot();
        IndexedWord target = null;
        Queue<IndexedWord> children = new LinkedList<>();
        children.add(current);
        while(!children.isEmpty()){
            current = children.poll();
            if(current.tag().equals("NN")){
                target = current;
                break;
            } else{
                children.addAll(dependencies.getChildren(current));
            }
        }
        return target;
    }
}
