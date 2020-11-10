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

import org.json.simple.JSONObject;

import java.util.*;


public class SentenceParser {

    private ArrayList<JSONObject> refList;
    private Boolean hasRetriedParsing = false;
    private Boolean isTargetFromIt = false;
    private Boolean isVBFound = true;
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
     * @return
     */
    private StanfordCoreNLP setup() {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // build and return pipeline
        return new StanfordCoreNLP(props);
    }

    /**
     * This method will parse the sentence provided and return a Parse Result IParseResultModel Object
     * containing all the information needed for output
     * @param sentence the sentence to be parsed
     * @return a SentenceIParseResult object that containing all the information needed for output
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
        String receiver = NOTFOUND;
        String directionString = NOTFOUND;
        String naming = NOTFOUND;

        IndexedWord sentenceMain = findSentenceMain(dependencies);

        receiver = findReceiver(dependencies, sentenceMain);
        String command = findCommand(dependencies, sentenceMain);
        if (!isVBFound){
            // If sentence main is not found, we will try to add "Please" to the front of the sentence and try again.
            if (!hasRetriedParsing){
                hasRetriedParsing = true;
                return retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
            } else {
                // check if there is a target
                command = NOTFOUND;
                targetIndexedWord = findTargetWOMain(dependencies);
            }
        } else if (nameSet.contains(command) && !hasRetriedParsing) {
            hasRetriedParsing = true;
            return retryWhenCommandIsName(sentence.text(), previousSentence);
        } else {
            // The case that sentence main is found.

            // Searching for the target object and the verb compound part of the command
            // receiver.equals(NOTFOUND)? findDepTarget(dependencies, sentenceMain) : 
            targetIndexedWord = findTarget(dependencies, sentenceMain);

            // Using relationships to find the reference objects, their modifiers, and whether Gesture is used on them
            Set<IndexedWord> oblToSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:to"));
            Set<IndexedWord> oblUnderSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:under"));
            Set<IndexedWord> oblBelowSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:below"));
            Set<IndexedWord> oblOnSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:on"));

            if (!oblToSet.isEmpty()) {
                for (IndexedWord indexedWord : oblToSet) {
                    direction = indexedWord;
                    directionString = direction.word().toLowerCase();
                    Set<IndexedWord> refSet = dependencies.getChildrenWithReln(direction, GrammaticalRelation.valueOf("nmod:of"));
                    if (!refSet.isEmpty()) {
                        refObj = (IndexedWord) refSet.toArray()[0];
                        refList.add(generateJSONObj(refObj, dependencies));
                    }
                }
            } else if (!oblUnderSet.isEmpty()) {
                directionString = "under";
                refObj = (IndexedWord) oblUnderSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies));
            } else if (!oblBelowSet.isEmpty()) {
                directionString = "below";
                refObj = (IndexedWord) oblBelowSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies));
            } else if (!oblOnSet.isEmpty()) {
                directionString = "on";
                refObj = (IndexedWord) oblOnSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies));
            } else if (targetIndexedWord != null){
                Set<IndexedWord> nmodsWords = dependencies.getChildrenWithRelns(targetIndexedWord, new ArrayList<GrammaticalRelation>() {
                    {
                        add(GrammaticalRelation.valueOf("nmod:in_front_of"));
                        add(GrammaticalRelation.valueOf("nmod:behind"));
                        add(GrammaticalRelation.valueOf("nmod:between"));
                        add(GrammaticalRelation.valueOf("nmod:on_top_of"));
                        add(GrammaticalRelation.valueOf("nmod:on"));
                        add(GrammaticalRelation.valueOf("nmod:in"));
                    }
                });
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
                        switch (dependencies.reln(targetIndexedWord, dep).getSpecific()) {
                            case "on":
                                directionString = "on";
                                Set<IndexedWord> refSet = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:of"));
                                if (!refSet.isEmpty()) {
                                    refObj = (IndexedWord) refSet.toArray()[0];
                                    String refObjString = refObj.word();
                                    JSONObject refMods = new JSONObject();
                                    refMods.put("Item", refObjString);
                                    ArrayList<String> rmods = new ArrayList<>();
                                    rmods.add(dep.word());
                                    refMods.put("Mods", rmods);
                                    refMods.put("Gesture", isGestureUsed(refObj, dependencies));
                                    refList.add(refMods);
                                }
                                break;
                            default:
                                directionString = dependencies.reln(targetIndexedWord, dep).getSpecific();
                                for (IndexedWord d : nmodsWords) {
                                    refList.add(generateJSONObj(d, dependencies));
                                }
                        }
                    }
                }

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
        result.target = isTargetFromIt? targetFromIt : generateJSONObj(targetIndexedWord, dependencies);
        previousTarget = (JSONObject) result.target.clone();
        result.refList = refList;
        result.direction = directionString;
        result.naming = naming;
        result.receiver = receiver;
        result.seqNum = seqNum;
        result.originalCoreSentence = sentence;
        hasRetriedParsing = false;
        return result;
    }

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

    private IndexedWord findTarget(SemanticGraph dependencies, IndexedWord sentenceMain) {
        IndexedWord targetIndexedWord = null;
        Set<IndexedWord> targetObjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));
        for (IndexedWord indexedWord : targetObjSet) {
            targetIndexedWord = indexedWord;
            if(targetIndexedWord.word().toLowerCase().equals("it")){
                isTargetFromIt = true;
                targetFromIt = this.previousTarget;
            }
        }
        return targetIndexedWord;
    }

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

    private String findReceiver(SemanticGraph dependencies, IndexedWord sentenceMain) {
        String receiver = NOTFOUND;
        Set<IndexedWord> iobjSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("iobj"));
        Set<IndexedWord> objSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obj"));

        if(!iobjSet.isEmpty()) {
            for (IndexedWord word : iobjSet) {
                if(word.word().equals("me")) receiver = "caller";
                else receiver = word.word();
            }
        }

        return receiver;
    }

    private void resetData(int seqNum){
        this.seqNum = seqNum;
        this.isTargetFromIt = false;
        this.refList = new ArrayList<>();
        this.isVBFound = true;
    }

    private SentenceParseResult retryWhenSentenceMainNotFound(String sentenceString, CoreSentence previousSentence){
        CoreDocument doc = new CoreDocument("Please " + sentenceString.toLowerCase());
        // annotate
        pipeline.annotate(doc);
        return parse(seqNum,doc.sentences().get(0),previousSentence);
    }
    
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
        }
        return mods;
    }

    private Boolean isGestureUsed(IndexedWord word, SemanticGraph dependencies){
        for(IndexedWord indexedWord : dependencies.getChildrenWithReln(word,GrammaticalRelation.valueOf("det"))){
            if(gestureSet.contains(indexedWord.word().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private JSONObject generateJSONObj(IndexedWord obj, SemanticGraph dependencies){
        JSONObject refMods = new JSONObject();
        if(obj == null){
            refMods.put("Item", NOTFOUND);
        }
        else if(obj.word().toLowerCase().equals("you")) {
            return generateSelfObj(null);
        } else {
            refMods.put("Item", obj.word().toLowerCase());
            refMods.put("Mods", getMods(obj, dependencies));
            refMods.put("Gesture", isGestureUsed(obj, dependencies));
        }
    
        return refMods;
    }

    private JSONObject generateSelfObj(IndexedWord possesion) {
        JSONObject refMods = new JSONObject();
        refMods.put("Item", "self");
        if(possesion != null) {
            refMods.put("Belonging", possesion.word());
        }
        return refMods;
    }

    private boolean isDirectional(String arg) {
        return directionSet.contains(arg);
    }

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
