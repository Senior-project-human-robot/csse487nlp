# CSSE 487 Senior Research Project - NLP
![Build Status of Master Branch](https://github.com/liux6/csse487nlp/workflows/build/badge.svg)

Instructor: Dr. Wollowski

Team Members: Lilin Chen, Xiangnan Chen, Yifan "Augustine" Cui, Xusheng "Fred" Liu

# Input for the NLP module
The input for the NLP module will be a string containing one or more sentences. The NLP module will parse the given string into multiple annotated sentences, and later output the parse result of each individual sentence as a JSON file.

# How we structure the output files?
The output files are in JSON format. The file name contains 2 parts: the user provided output file name and the sequence of the given sentence appearing in the given string.
```json
{
    "NLPProcessor":
        [
            {
                "Command":"pick up",
                "Info":{
                    "Reference_Mods":
                        [
                            {"Item":"cube","Mods":["blue"], "Gesture": "this"}, 
                            {"Item":"bottle","Mods":["orange"], "Gesture": "that"}
                        ],
                    "Target_Mods":{"Item":"block","Mods":["red"], "Gesture": ""},
                    "Direction":"left"
                }
            }
        ]
}
```
The sample JSON object above presents the typical structure of our output JSON files. The JSON object will contain the following information:
- **"Command"**: the action send to the robot, such as "pick up", "drop", and "throw away".
- **"Info"**: Information related to the target object, reference objects, and spatial relationship between the target object and reference objects.
For each object stored, either target object or reference objects, the following information is stored:
- The name of the item (**"Item"**), such as "cube", "bottle", "block", "tube", and etc.
- The modifiers for that item (**"Mods"**), including texture, color, material, and etc.
- What kind of pointing terms are used (**"Gesture"**), including "this" and "that". If none of them are used, we will store a empty string.

# How to extract the "Command Verb Phrase"?
For a sentence that contains requests, it could start in multiple forms, such as "I want to pick up...", "Pick up ...", "Please pick up ...", and etc. One thing we noticed that it will not change is it will always contain the main verb phrase. In the examples we provided above, the main verb phrase is "pick up". The sentence structure tends to remain constant after the verb phrase. So, our first task is to find out what is the actual root of our sentence, the phrase start with the command verb phrase.
## Steps we take to find the command verb phrase:
- First, we use the dependence parse of Stanford CoreNLP library to annotate the sentence with both relationships and tags for words in the provided sentence. Those information are stored in data structure called [`Semantic Graph`](https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/semgraph/SemanticGraph.html).
- Then, we use the first root of the sentence stored in the semantic graph and iteratively find the children of the root and check whether it has tag ["VB"](https://sites.google.com/site/partofspeechhelp/#TOC-VB), which stands for Verb. This verb will be stored as a indexed word for future uses. It is also the root for the main structure of the sentence.
- After finding the verb part of the command phrase, we also need to find the preposition part of the command. We will start with the verb we acquired in the previous step. The relation between the preposition part of the command verb phrase and the verb we find previously is always called ["compound:prt"](https://universaldependencies.org/docs/en/dep/compound-prt.html). So, we use the [`.getRelation()`](https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/semgraph/SemanticGraphEdge.html#getRelation--) method to find out the relationship between each edge (the data structure for this is [`SemanticGraphEdge`](https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/semgraph/SemanticGraphEdge.html)) stored in the Semantic Graph and searching for the relationship "compound:prt" to find the preposition part of the command.
- We will use space to connect both the verb part and the preposition part to form the complete Command Verb Phrase for output.

## Special cases for finding the command verb phrase
Since the CoreNLP library has some issue in parsing the upper case "drop", which it will always parse as a noun, we will add the word "Please" in front of the sentence whenever we are not able to find the verb phrase and make everything else into lower case. If we still have trouble in parsing the sentence, we will take the steps described [here](#what-if-we-cannot-parse-the-sentence-with-corenlp-library)

# How to find the "Target Object"?
We will use our result from section [How to extract the command verb phrase](#how-to-extract-the-command-verb-phrase) in this section. Notice that we have stored the verb part of the command verb phrase as a [`IndexedWord`](https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/ling/IndexedWord.html). We will then iterate through all the edges of the semantic graph and find all the words with ["governor"](https://en.wikipedia.org/wiki/Government_(linguistics)) that equals to the command verb part. In other words, we are searching for "dependent" words of the verb part of the command.

After finding all the words that match our condition, we will search for relation ["obj"](https://universaldependencies.org/u/dep/obj.html). This relationship will help us find the object the verb is acting on. Thus, the object we find will be our target object. We will store it as a string for output and an `IndexedWord` for future use.

# How to find the "Modifiers" for the Target Object?
We will use the target object we stored as `IndexedWord` from section [How to find the "Target Object"](#how-to-find-the-target-object) in this section. To find all the modifiers for the target object, we will search for all the dependents of that word using [`.getDependent()`](https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/semgraph/SemanticGraphEdge.html#getDependent--) and store them as modifiers in a list.

# How to find the "Direction" reference?
<h1 style="color:red;">TODO</h1>

# How to find the "Reference Objects"?
<h1 style="color:red;">TODO</h1>

# How to find the "Modifiers" for the reference objects?
<h1 style="color:red;">TODO</h1>

# How to find "Gestures"?
<h1 style="color:red;">TODO</h1>

# What if we cannot parse the sentence with CoreNLP library
<h1 style="color:red;">TODO</h1>
