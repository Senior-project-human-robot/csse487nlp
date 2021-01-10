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
import utils.Constants;

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
    private static final Set<String> directionSet = Constants.getDirectionSet();
    private final static Set<String> gestureSet = new HashSet<>(Arrays.asList("this", "that"));
    private final static Set<String> nameSet = new HashSet<>(Arrays.asList("name", "call", "define"));
    private final static Set<String> oblSpecificRelationships = new HashSet<>(Arrays.asList("obl:to", "obl:under", "obl:below", "obl:on","obl:from","obl:onto"));
    private final static String NOTFOUND = "???";

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser() {
        this.refList = new ArrayList<>();
        this.pipeline = this.setup();
        this.seqNum = 0;
    }

    /**
     * This method will setup the pipeline for parsing the paragraph
     *
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
     * needed for output
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
        IndexedWord targetIndexedWord;
        String receiver;
        String directionString = NOTFOUND;
        String naming = NOTFOUND;

        IndexedWord sentenceMain = findSentenceMain(dependencies);

        receiver = findReceiver(dependencies, sentenceMain);
        String command = findCommand(dependencies, sentenceMain);
        if (!isVBFound && !hasRetriedParsing) {
            // If sentence main is not found, we will try to add "Please" to the front of the sentence and try again.
            hasRetriedParsing = true;
            return retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
        } else if (nameSet.contains(command) && !hasRetriedParsing) {
            // If the sentence is using naming, such as "name" and "define", try it separately with retryWhenCommandIsName method.
            hasRetriedParsing = true;
            return retryWhenCommandIsName(sentence.text(), dependencies, sentenceMain, previousSentence);
        } else {
            // The case that sentence main is found.
            // Searching for the target object and the verb compound part of the command
            if (!isVBFound) {
                command = NOTFOUND;
                targetIndexedWord = findTargetWOMain(dependencies);
            } else {
                targetIndexedWord = findTarget(dependencies, sentenceMain);
                if (targetIndexedWord == null && !this.hasRetriedParsing) {
                    this.hasRetriedParsing = true;
                    return retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
                }
            }

            // Using relationships to find the reference objects, their modifiers, and whether Gesture is used on them
            Set<IndexedWord> oblSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl"));
            Set<IndexedWord> oblToSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:to"));

            Set<IndexedWord> oblSpecificRelationSet = new HashSet<>();
            String oblRelationship = "";
            for (String oblSpecificRelationship : oblSpecificRelationships){
                oblRelationship = oblSpecificRelationship.replaceAll("obl:", "");
                oblSpecificRelationSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf(oblSpecificRelationship));
                if (!oblSpecificRelationSet.isEmpty()){
                    break;
                }
            }

            if (!oblSet.isEmpty()) { // Reference clarification
                directionString = ((IndexedWord) oblSet.toArray()[0]).word();
            } else if (!oblToSet.isEmpty()) {
                for (IndexedWord indexedWord : oblToSet) {
                    if (isDirectional(indexedWord.word())) {
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
                            for (IndexedWord word : nmodsWords) {
                                if (dependencies.reln(indexedWord, word) != null) {
                                    if ("between".equals(dependencies.reln(indexedWord, word).getSpecific())) {
                                        directionString = "between";
                                        refObj = word;
                                        refList.add(generateJSONObj(refObj, dependencies, false));
                                    } else {
                                        System.err.println("New cases for to the place ...");
                                        throw new UnsupportedOperationException();
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!oblSpecificRelationSet.isEmpty()){
                directionString = oblRelationship;
                refObj = (IndexedWord) oblSpecificRelationSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies, false));
            } else if (targetIndexedWord != null) {
                Set<IndexedWord> nmodsWords = generateNmodSet(targetIndexedWord, dependencies);
                if (nmodsWords.size() >= 1) {
                    IndexedWord dep = (IndexedWord) nmodsWords.toArray()[0];
                    // self reference
                    Set<IndexedWord> possessions = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:poss"));
                    if (!possessions.isEmpty()) {
                        IndexedWord possessor = (IndexedWord) possessions.toArray()[0];
                        if (possessor.word().equals("your")) {
                            directionString = dep.word();
                            if (isDirectional(directionString)) {
                                // self with direction - "on your right"
                                refList.add(generateSelfObj(null));
                            } else {
                                // self with possession - "in your basket"
                                Set<IndexedWord> posDir = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("case"));
                                IndexedWord relation = (IndexedWord) posDir.toArray()[0];
                                directionString = relation.word();
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
                                    if (dep.word().equals("it")) {
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
            }
            //If the command verb is naming, find the naming part in the sentence
            if (nameSet.contains(command)) {
                naming = findNaming(sentence.text(), dependencies, sentenceMain);
            }

        }
        System.out.println("-----------------------");
        System.out.println("Full Command: " + command.toLowerCase());
        System.out.println("-----------------------");


        System.out.println(command);
        SentenceParseResult result = new SentenceParseResult();
        result.command = command.toLowerCase();
        result.target = isTargetFromIt ? targetFromIt : generateJSONObj(targetIndexedWord, dependencies, true);
        previousTarget = result.target;
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
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @param sentenceMain the main part of the sentence starting with the verb phrase
     * @return the naming (xcomp/obl:as) part
     */
    private String findNaming(String sentenceString, SemanticGraph dependencies, IndexedWord sentenceMain) {
        // Naming
        String naming = NOTFOUND;

        Set<IndexedWord> xcompSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("xcomp"));
        Set<IndexedWord> oblAsSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:as"));
        if (!xcompSet.isEmpty()) {
            naming = ((IndexedWord) xcompSet.toArray()[0]).word();
        } else if (!oblAsSet.isEmpty()) {
            IndexedWord partname = (IndexedWord) oblAsSet.toArray()[0];
            ArrayList<String> mods = getModStrings(partname, dependencies);
            naming = "";
            for(String st : mods) {
                naming += st + " ";
            }
            naming += partname.word();
        } else if (sentenceString.contains("as")) { //TODO: cannot use obl:as relationship, use the last word
            String[] words = sentenceString.split(" ");
            IndexedWord lastChild = dependencies.getNodeByIndex(words.length);
            if(lastChild.tag().equals("NNP") || lastChild.tag().equals("NN")) {
                ArrayList<String> mods = getModStrings(lastChild, dependencies);
                naming = "";
                for(String st : mods) {
                    naming += st + " ";
                }
                naming += lastChild.word();
            }
        }
        return naming;
    }


    /**
     * Find target based on the dependencies of sentenceMain, using "obj" relationship
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @param sentenceMain the main part of the sentence starting with the verb phrase
     * @return the target IndexedWord in the sentence
     */
    private IndexedWord findTarget(SemanticGraph dependencies, IndexedWord sentenceMain) {
        IndexedWord targetIndexedWord = null;
        Set<IndexedWord> targetObjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));
        for (IndexedWord indexedWord : targetObjSet) {
            targetIndexedWord = indexedWord;
            if (targetIndexedWord.word().equalsIgnoreCase("it")) {
                isTargetFromIt = true;
                targetFromIt = this.previousTarget;
            }
        }
        return targetIndexedWord;
    }

    /**
     * Build up the command to be the word of the sentenceMain; find the compound part of the command through "compound:prt"
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @param sentenceMain the main part of the sentence starting with the verb phrase
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
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @return the indexed word for the command first verb part
     */
    private IndexedWord findSentenceMain(SemanticGraph dependencies) {
        // Searching for the command verb part
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        if (!dependencies.getFirstRoot().tag().equals("VB")) {
            isVBFound = false;
            for (IndexedWord rootChild : dependencies.getChildren(sentenceMain)) {
                if (rootChild.tag().equals("VB")) {
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
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @param sentenceMain the main part of the sentence starting with the verb phrase
     * @return the String of the receiver's name, else return NOTFOUND
     */
    private String findReceiver(SemanticGraph dependencies, IndexedWord sentenceMain) {
        String receiver = NOTFOUND;
        Set<IndexedWord> iobjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("iobj"));
        Set<IndexedWord> objSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));

        if (!iobjSet.isEmpty()) {
            for (IndexedWord word : iobjSet) {
                if (word.word().equals("me")) receiver = "caller";
                else receiver = word.word();
            }
        } else if (!objSet.isEmpty()) {
            for (IndexedWord word : objSet) {
                if (word.tag().equals("PRP") && !word.word().equals("it")) {
                    callerObj = true;
                    if (word.word().equals("me")) receiver = "caller";
                    else receiver = word.word();
                }
            }
        }

        return receiver;
    }

    /**
     * Reset every parameter
     *
     * @param seqNum the sequence number of the sentence
     */
    private void resetData(int seqNum) {
        this.seqNum = seqNum;
        this.isTargetFromIt = false;
        this.refList = new ArrayList<>();
        this.isVBFound = true;
        this.callerObj = false;
    }

    /**
     * When there is no sentence main found in the sentence.
     * Add a Please at the beginning of the sentence and retry the parsing.
     *
     * @param sentenceString   the sentence to be parsed in String format
     * @param previousSentence the previously parsed sentence in String format
     * @return recursion on parsing
     */
    private SentenceParseResult retryWhenSentenceMainNotFound(String sentenceString, CoreSentence previousSentence) {
        // add please at the begin of the sentence and change the first char of the original string to lower case.
        CoreDocument doc = new CoreDocument("Please " + sentenceString.substring(0, 1).toLowerCase() + sentenceString.substring(1));
        // annotate
        pipeline.annotate(doc);
        return parse(seqNum, doc.sentences().get(0), previousSentence);
    }

    /**
     * When there is the command verb being name/call
     * Replace name/call to define and retry the parsing.
     *
     * @param sentenceString   the sentence to be parsed in String format
     * @param dependencies     the SemanticGraph input that records the dependency parse results
     * @param previousSentence the previously parsed sentence in String format
     * @return the parse result after replacing the call and name with define, as well as inserting the missing as
     */
    private SentenceParseResult retryWhenCommandIsName(String sentenceString, SemanticGraph dependencies, IndexedWord sentenceMain, CoreSentence previousSentence) {
        sentenceString = sentenceString.replaceFirst("Call", "define");
        sentenceString = sentenceString.replaceFirst("call", "define");
        sentenceString = sentenceString.replaceFirst("Name", "define");
        sentenceString = sentenceString.replaceFirst("name", "define");

        if (!sentenceString.contains(" as ")) { // not inclusive, as the sentence may already contain an "as" for other purposes && before the last word might not be the proper position
            String[] words = sentenceString.split(" ");
            // IndexedWord xcompChild = dependencies.getChildWithReln(sentenceMain, GrammaticalRelation.valueOf("xcomp"));
            IndexedWord lastChild = dependencies.getNodeByIndex(words.length);
            Set<IndexedWord> compounds = dependencies.getChildrenWithReln(lastChild, GrammaticalRelation.valueOf("compound"));
            Set<IndexedWord> amod = dependencies.getChildrenWithReln(lastChild, GrammaticalRelation.valueOf("amod"));
            ArrayList<String> newSentenceLst = new ArrayList<String>(Arrays.asList(words));
            System.out.println("LastChild: " + lastChild.word() + " AmodSize: " + amod.size() + " CompoundSize: " + compounds.size());

            if(lastChild.tag().equals("NNP") || lastChild.tag().equals("NN")) {
                if (amod.isEmpty() && !compounds.isEmpty()) { // define the red block Bob
                    newSentenceLst.add(words.length-1, "as");
                } else if (!amod.isEmpty() && !compounds.isEmpty()) {
                    newSentenceLst.add(words.length-amod.size()-compounds.size()-1, "as");
                } else if (!amod.isEmpty() && compounds.isEmpty()) {
                    newSentenceLst.add(words.length-amod.size()-1, "as");
                } else {
                    newSentenceLst.add(words.length-1, "as");
                }
            }
            sentenceString = String.join(" ", newSentenceLst);

        }

        // System.out.println("+++++++++++++++++++++++++++++" + sentenceString + "+++++++++++++++++++++++++++++");

        System.out.println("New Sentence String: " + sentenceString);
        CoreDocument doc = new CoreDocument(sentenceString);
        // annotate
        pipeline.annotate(doc);
        return parse(seqNum, doc.sentences().get(0), previousSentence);
    }

    /**
     * Extract all the amod-obl:npmod and compound-amod parts from the sentence.
     *
     * @param indexedWord  the indexed word that we want to find modifiers for
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @return the ArrayList of modifiers in String Format
     */
    private ArrayList<String> getModStrings(IndexedWord indexedWord, SemanticGraph dependencies) {
        ArrayList<String> mods = new ArrayList<>();
        for (IndexedWord mod : dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("amod"))) {
            mods.add(mod.word().toLowerCase());
            for (IndexedWord npmod : dependencies.getChildrenWithReln(mod, GrammaticalRelation.valueOf("obl:npmod"))) {
                mods.add(npmod.word().toLowerCase());
            }
        }
        for (IndexedWord mod : dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("compound"))) {
            mods.add(mod.word().toLowerCase());
            for (IndexedWord amod : dependencies.getChildrenWithReln(mod, GrammaticalRelation.valueOf("amod"))) {
                mods.add(amod.word().toLowerCase());
            }
        }
        return mods;
    }

    /**
     * Extract all the amod-obl:npmod and compound-amod parts from the sentence.
     *
     * @param indexedWord  the indexed word that we want to find modifiers for
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @return the ArrayList of modifiers in IndexedWord format
     */
    private ArrayList<IndexedWord> getModIndexWords(IndexedWord indexedWord, SemanticGraph dependencies) {
        ArrayList<IndexedWord> mods = new ArrayList<>();
        for (IndexedWord mod : dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("amod"))) {
            mods.add(mod);
            mods.addAll(dependencies.getChildrenWithReln(mod, GrammaticalRelation.valueOf("obl:npmod")));
        }
        for (IndexedWord mod : dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("compound"))) {
            mods.add(mod);
            mods.addAll(dependencies.getChildrenWithReln(mod, GrammaticalRelation.valueOf("amod")));
        }
        return mods;
    }

    /**
     * Check if there is grammatical relation "det" or "mark"
     *
     * @param word         the indexed word that we want to check whether the gesture is used
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @param isTarget     the boolean input that indicates whether the indexed word is the target of the sentence
     * @return if there is gesture used on the given indexed word
     */
    private Boolean isGestureUsed(IndexedWord word, SemanticGraph dependencies, Boolean isTarget) {
        if (isTarget && callerObj) return true;
        for (IndexedWord indexedWord : dependencies.getChildrenWithReln(word, GrammaticalRelation.valueOf("det"))) {
            if (gestureSet.contains(indexedWord.word().toLowerCase())) {
                return true;
            }
        }
        for (IndexedWord indexedWord : dependencies.getChildrenWithReln(word, GrammaticalRelation.valueOf("compound"))) {
            for (IndexedWord inner : dependencies.getChildrenWithReln(indexedWord, GrammaticalRelation.valueOf("mark"))) {
                if (gestureSet.contains(inner.word().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will generate a JSON Object that having the information about
     * the provided object, its modifiers, and whether gesture is used
     *
     * @param word         the Item that the JSON object will be generated upon
     * @param dependencies the entire sentence main part in dependency parsing format stored as Semantic Graph
     * @param isTarget     a Boolean target that indicates whether it is the target. if the object is our target, it will be true, otherwise false
     * @return a JSONObject object that containing the information about the provided object, its modifiers, and whether gesture is used
     */
    private JSONObject generateJSONObj(IndexedWord word, SemanticGraph dependencies, Boolean isTarget) {
        JSONObject refMods = new JSONObject();
        if (word == null) {
            refMods.put("Item", NOTFOUND);
        } else if (word.word().equalsIgnoreCase("you")) {
            return generateSelfObj(null);
        } else {
            refMods.put("Item", word.word().toLowerCase());
            refMods.put("Mods", getModStrings(word, dependencies));
            refMods.put("Gesture", isGestureUsed(word, dependencies, isTarget));
        }

        return refMods;
    }

    /**
     * Generate self-referencing object. Add possession when needed.
     *
     * @param possession the indexed word that possessed by the robot
     * @return the json object that record the information about what the robot possesses
     */
    private JSONObject generateSelfObj(IndexedWord possession) {
        JSONObject refMods = new JSONObject();
        refMods.put("Item", "self");
        if (possession != null) {
            refMods.put("Belonging", possession.word());
        }
        return refMods;
    }

    /**
     * Generate a set of IndexedWord that has the nmods tag of the first parameter indexedWord
     *
     * @param indexedWord  the indexed word that we want to check whether the gesture is used
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @return a set containing IndexedWord of all words that hold the specified GrammaticalRelation
     */
    private Set<IndexedWord> generateNmodSet(IndexedWord indexedWord, SemanticGraph dependencies) {
        return dependencies.getChildrenWithRelns(indexedWord, new ArrayList<GrammaticalRelation>() {
            {
                add(GrammaticalRelation.valueOf("nmod:in_front_of"));
                add(GrammaticalRelation.valueOf("nmod:behind"));
                add(GrammaticalRelation.valueOf("nmod:between"));
                add(GrammaticalRelation.valueOf("nmod:on_top_of"));
                add(GrammaticalRelation.valueOf("nmod:on"));
                add(GrammaticalRelation.valueOf("nmod:in"));
                add(GrammaticalRelation.valueOf("nmod:to"));
                add(GrammaticalRelation.valueOf("nmod:from"));
                add(GrammaticalRelation.valueOf("nmod:under"));
            }
        });
    }

    /**
     * Helper function; check whether an string is a direction word
     *
     * @param arg the argument to be checked to see whether it is a supported directional word
     * @return boolean telling whether arg is within directionSet
     */
    private boolean isDirectional(String arg) {
        return directionSet.contains(arg);
    }

    /**
     * When sentenceMain is not found, find the target based on the rest of the sentences
     *
     * @param dependencies the SemanticGraph input that records the dependency parse results
     * @return null if target not found, else return the target
     */
    private IndexedWord findTargetWOMain(SemanticGraph dependencies) {
        IndexedWord current = dependencies.getFirstRoot();
        IndexedWord target = null;
        Queue<IndexedWord> children = new LinkedList<>();
        children.add(current);
        while (!children.isEmpty()) {
            current = children.poll();
            if (current.tag().equals("NN")) {
                target = current;
                break;
            } else {
                children.addAll(dependencies.getChildren(current));
            }
        }
        return target;
    }
}
