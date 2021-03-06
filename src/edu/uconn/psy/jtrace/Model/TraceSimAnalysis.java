/*
 * TraceAnalysis.java
 *
 * Created on May 5, 2005, 10:33 AM
 */

package edu.uconn.psy.jtrace.Model;

import edu.uconn.psy.jtrace.Model.*;
import java.util.Vector;
import java.io.*;
import org.jfree.data.xy.*;

/**
 * A TraceAnalysis object analyzes the results of a TraceSim. It is a 
 * general-purpose LCR analysis tool that collects and processes
 * data for graphing or for output to files for more analysis. 
 *
 * @author Harlan Harris
 */
public class TraceSimAnalysis {
    
    // are we watching phonemes or words?
    public static final int PHONEMES = 1;
    public static final int WORDS = 2;
    
    // graph contents
    public static final int RESPONSE_PROBABILITIES = 1;
    public static final int ACTIVATIONS = 2;    
    public static final int COMPETITION_INDEX = 3;    
    
    // what items to plot
    public static final int WATCHTOPN = 1;
    public static final int WATCHSPECIFIED = 2;
    
    // how alignment works
    public static final int AVERAGE = 1;
    public static final int MAX_POSTHOC = 2;
    public static final int STATIC = 3;
    public static final int FRAUENFELDER = 4;
    public static final int MAX_ADHOC = 5;
    public static final int MAX_ADHOC_2 = 6;
    
    // how choice works
    public static final int NORMAL = 1;
    public static final int FORCED = 2;
    
    // type of calculation being done in the competition index content type (oy)
    public static final int RAW=2;
    public static final int FIRST_DERIVATIVE=1;
    public static final int SECOND_DERIVATIVE=0;
    
    // calculation constants
//    public static final int NUMERATOR = 1;
//    public static final int DENOMINATOR = 2;    
    
    private double [][][] phonemeD;
    private double [][][] wordD;
    
    private double [] globalPhonemeComp;
    private double [] globalLexicalComp;
    
    
    // how we do analysis
    private int domain;           // PHONEMES or WORDS
    private Vector itemsToWatch;    // vector of Strings
    private int watchTopN;          // 
    private int watchType;          // WATCHTOPN || WATCHSPECIFIED
    private int calculationType;          // AVERAGE, MAX_ADHOC, MAX_POSTHOC, STATIC, or FRAUNFELDER
    private int contentType;        // ACTIVATIONS || RESPONSE_PROBABILITIES
    private int alignment;    // only used if alignment == STATIC || FRAUNFELDER
    private int choice;             // NORMAL or FORCED
    private int kValue;             // LCR exponent
    private int competType;         // RAW || FIRST_DERIVATIVE || SECOND_DERIVATIVE
    private int competSlope;
    
    private int[] adhocAlignment;
    private int[][] adhocAlignment2;
    private int[] posthocAlignment;
    
    private XYSeriesCollection averagedAnalysis;
    private int simsInIterator;
    private boolean currentlyIterating;
    
    public int getDomain(){return domain;}
    public void setDomain(int _d){domain=_d;}
    public int getTopN(){return watchTopN;}
    public void setTopN(int _n){watchTopN=_n;}
    public int getAlignment(){return alignment;}
    public void setCalculationType(int _c){calculationType=_c;}
    public int getCalculationType(){return calculationType;}
    public void setAlignment(int _a){alignment=_a;} 
    public int getChoice(){return choice;}
    public void setChoice(int _c){choice=_c;}
    public int getKValue(){return kValue;}
    public void setKValue(int _k){
        kValue=_k;
    }
    public Vector getItemsToWatch(){return itemsToWatch;}
    public void setItemsToWatch(String [] items)
    {
        itemsToWatch.clear();
        for (int i = 0; i < items.length; i++)
            addItemToWatchList(items[i]);
    }
    public void setItemsToWatch(TraceLexicon lex){    
        itemsToWatch.clear();
        for(int i=0;i<lex.size();i++)
            itemsToWatch.add(lex.get(i).getPhon());
    }
    public void addItemToWatchList(String w){
        itemsToWatch.add(w);
    }    
    /** 
     * Creates a new instance of TraceAnalysis.
     *
     * @param watching  PHONEMES or WORDS
     * @param items     items to watch (Vector of chars, Vector of TraceWords, or null)
     * @param topN      0 to use items, or otherwise N
     * @param calc      AVERAGE, MAX_ADHOC, MAX_ADHOC2, MAX_POSTHOC, STATIC, or FRAUNFELDER
     * @param static    if alignment == STATIC
     * @param choice    NORMAL or FORCED
     * @param k         LCR exponent (if 0, use activations)
     */
    public TraceSimAnalysis(int _domain, int _watchType, Vector _items, int _topN,
            int _calc, int _static, int _choice, int _k) 
    {
        // load everything in
        domain = _domain;
        itemsToWatch = _items;
        watchTopN = _topN;
        watchType = _watchType;
        calculationType = _calc;
        alignment = _static;
        choice = _choice;
        kValue = _k;
        if (kValue==0)
        {   
            contentType=ACTIVATIONS;
            kValue = 1;
        }
        else 
            contentType=RESPONSE_PROBABILITIES;
    }
    public TraceSimAnalysis() 
    {
        domain = WORDS;
        watchTopN = 10;
        itemsToWatch = new Vector();
        watchType = WATCHTOPN;
        calculationType = STATIC;
        alignment = 4;
        choice = NORMAL;
        kValue = 4;
        contentType=ACTIVATIONS;        
        currentlyIterating = false;
    }
    public void reset() 
    {
        domain = WORDS;
        watchTopN = 10;
        itemsToWatch = new Vector();
        watchType = WATCHTOPN;
        calculationType = STATIC;
        alignment = 4;
        choice = NORMAL;
        kValue = 4;
        contentType=ACTIVATIONS;        
    }
    public void gc(){
        phonemeD=null;
        wordD=null;
    }
    
    
    /**
     * Copy constructor.
     */
    public TraceSimAnalysis(TraceSimAnalysis old)
    {
        domain = old.domain;
        watchType = old.watchType;
        itemsToWatch = (Vector)old.itemsToWatch.clone();
        watchTopN = old.watchTopN;
        calculationType = old.calculationType;
        alignment = old.alignment;
        choice = old.choice;
        kValue = old.kValue;
        contentType = old.contentType;
        
        averagedAnalysis = old.averagedAnalysis;
        simsInIterator = old.simsInIterator;
        currentlyIterating = old.currentlyIterating;
    
        competType = old.competType;
        competSlope = old.competSlope;
    }
    
    /**
     * Does the actual analysis.
     * 
     * @param sim      TraceSim to collect data from
     * @return         XYSeriesCollection
     */
    public XYSeriesCollection doAnalysis(TraceSim sim)
    {        
        int dataSetLength = sim.getStepsRun();
        
        // short-circuit for no data
        if(dataSetLength == 0) return new XYSeriesCollection();
        
        // data arrays
        double [][][] activationData;       // reference to wordD or phonemeD
                                            // activationData[cycle][item][slice]
        double [][][] responseStrengthData; // e^(k*activationData)
        double [][] plotData;               // will be converted to XYSeriesCollection at the end
                                            // plotData[item][cycle]
        
        // set of indices into the items
        int [] itemIndices;                 // activationData[c][itemIndices[i]][slice]
        //int ctItemIndices; 
        
        // alignment can either be a constant, a 1-D array (max_posthoc), or a 2-D array (max_adhoc)
        // constant is alignment field of this object
        int [] alignmentPostHoc;
        int [][] alignmentAdHoc;
        // initial processing, before we loop
        
        // get the parameters
        TraceParam param = sim.getParameters();
        TraceLexicon lexicon = param.getLexicon();        
        
        // get references to the data
        phonemeD = sim.getPhonemeD();
        wordD = sim.getWordD();
        
                        
        XYSeriesCollection ret;
        
        if (contentType == RESPONSE_PROBABILITIES || contentType == ACTIVATIONS){
        
            // figure out what we're analyzing
            Vector items;
            if (watchType == WATCHTOPN)
            {
                // we don't want to update the object's list -- that's bad form...
                items = discoverItemsToWatch(sim);
            }
            else{ //watchType == WATCHSPECIFIED
                items = itemsToWatch;            
            }
            // short-circuit if nothing to analyze!
            if(items.size() == 0) return new XYSeriesCollection();

            // set up data and indexes
            if (domain == WORDS)
            {
                itemIndices = itemsToArrayIndices(items,param.getLexicon().toStringArray());

                activationData = wordD;
            }
            else{ //if(domain==PHONEMES){
                itemIndices = itemsToArrayIndices(items,param.getPhonemeLabels());            
                activationData = phonemeD;
            }
            int numDataSets = activationData[0].length;
            int numSlices = sim.getParameters().getFSlices() / sim.getParameters().getSlicesPerPhon();

            // we only need responseStrength for plotting response probabilities
            responseStrengthData = new double[dataSetLength][numDataSets][numSlices];

            if (contentType == RESPONSE_PROBABILITIES)
            {
                // built the matrix
                for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                {
                    for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                    {
                        for (int iSlice = 0; iSlice < numSlices; iSlice++)
                        {
                            double d = activationData[iDSL][iNDS][iSlice];
                            // convert it to proportion possible activation
                            // d = (d - param.getMin() ) / (param.getMax() - param.getMin());                                                 
                            // do k-value
                            responseStrengthData[iDSL][iNDS][iSlice] = java.lang.Math.exp(d * kValue);

                            if(param.getFreqNode().RDL_post_c != 0 && domain == WORDS){
                                // From JSM modified TRACE code : S_i =  SWP_i =  e^(k*a_i) * [log 10( c +  f_i )]
                                responseStrengthData[iDSL][iNDS][iSlice] = param.getFreqNode().applyPostActivationFreqScaling(param.getLexicon().get(iNDS), responseStrengthData[iDSL][iNDS][iSlice]);                            
                            }
                        }
                    }
                }
            }

            // set up alignment matricies if needed
            // NB: responseStrength is a monotonic transformation of activationData, so
            // max operations are equivalent. So, we can calculate these alignment 
            // matricies regardless of whether we want activations or response strengths.
            alignmentAdHoc = new int[numDataSets][dataSetLength];
            alignmentPostHoc = new int[numDataSets];
            if (calculationType == MAX_ADHOC)
            {
                // foreach item
                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                {
                    for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                    {
                        // find the alignment that maximizes activation for a particular cycle
                        double bestActivation = -1000;

                        for (int iSlices = 0; iSlices < numSlices; iSlices++)
                        {
                            if (activationData[iDSL][iNDS][iSlices] > bestActivation)
                            {
                                bestActivation = activationData[iDSL][iNDS][iSlices];
                                alignmentAdHoc[iNDS][iDSL] = iSlices;
                            }
                        }
                    }
                }
            }
            //MAX_ADHOC_2 IS IDENTICAL TO MAX_ADHOC IN THIS PART
            else if (calculationType == MAX_ADHOC_2)
            {
                // foreach item
                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                {
                    for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                    {
                        // find the alignment that maximizes activation for a particular cycle
                        double bestActivation = -1000;

                        for (int iSlices = 0; iSlices < numSlices; iSlices++)
                        {
                            if (activationData[iDSL][iNDS][iSlices] > bestActivation)
                            {
                                bestActivation = activationData[iDSL][iNDS][iSlices];
                                alignmentAdHoc[iNDS][iDSL] = iSlices;
                            }
                        }
                    }
                }
            }        
            else if (calculationType == MAX_POSTHOC)
            {
                // foreach item
                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                {
                    // find the alignment that maximizes activation over all cycles
                    double bestActivation = -1000;

                    for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                        for (int iSlices = 0; iSlices < numSlices; iSlices++)
                        {
                            if (activationData[iDSL][iNDS][iSlices] > bestActivation)
                            {
                                bestActivation = activationData[iDSL][iNDS][iSlices];
                                alignmentPostHoc[iNDS] = iSlices;
                            }
                        }
                }
            }

            // now, calculate the denominators
            double [] denominator = new double [dataSetLength];
            // denominatorTwo is used if the alignment differs depending on the item;
            // so: denominatorTwo[cycle][item]
            double [][] denominatorTwo = new double [dataSetLength][numDataSets];

            if (contentType == RESPONSE_PROBABILITIES)
            {

                for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                {
                    denominator[iDSL] = 0;

                    if (choice == NORMAL)
                    {
                        switch (calculationType)
                        {
                            case AVERAGE:
                                for (int iSlices = 0; iSlices < numSlices; iSlices++)
                                {
                                    for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                    {
                                        denominator[iDSL] += responseStrengthData[iDSL][iNDS][iSlices];
                                    }
                                }
                                break;
                            case MAX_ADHOC:
                                //in this case, the same alignment selected for target 'ii' is used for all competitor items
                                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                    for (int iiNDS = 0; iiNDS < numDataSets; iiNDS++)
                                    {
                                        // for this item (iDSL) and cycle (iiNDS), we know the alignment
                                        denominatorTwo[iDSL][iiNDS] += responseStrengthData[iDSL][iNDS][alignmentAdHoc[iiNDS][iDSL]];
                                    }
                                break;
                            case MAX_ADHOC_2:
                                //in this case, the alignment selected for target 'ii' is NOT used for all competitor items
                                //instead, each competitor 'i' uses its own MAX_ADHOC discovered alignment
                                //these are just different ways of considering competition mechanisms, none of which have
                                //much empirical basis.  but see Vroomen & Van Gelder (1995, 1997)
                                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                    for (int iiNDS = 0; iiNDS < numDataSets; iiNDS++)
                                    {
                                        // for this item (iDSL) and cycle (iiNDS), we know the alignment
                                        //DIFFERENCE BETWEEN MAX_ADHOC AND MAX_ADHOC_2 OCCURS HERE: alignmentAdHoc[iNDS][iDSL] VERSUS alignmentAdHoc[iiNDS][iDSL]
                                        denominatorTwo[iDSL][iiNDS] += responseStrengthData[iDSL][iNDS][alignmentAdHoc[iNDS][iDSL]];
                                    }
                                break;
                            case MAX_POSTHOC:
                                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                    //for (int iiNDS = 0; iiNDS < numDataSets; iiNDS++)
                                    {
                                        //denominatorTwo[iDSL][iiNDS] += responseStrengthData[iDSL][iNDS][alignmentPostHoc[iiNDS]];
                                        denominator[iDSL] += responseStrengthData[iDSL][iNDS][alignmentPostHoc[iNDS]];
                                    }
                                break;
                            case STATIC:
                                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                {
                                    denominator[iDSL] += responseStrengthData[iDSL][iNDS][alignment];
                                }
                                break;
                            case FRAUENFELDER:
                                // iDSL is the index to a particular cycle of the model
                                // iNDS is the index to an item (word/phoneme) to be graphed
                                // iiNDS is the index to the potential competitors of iNDS
                                // competAlign is an index to an alignment (time slice)
                                // alignment is the user-specified alignment

                                // for all items and competitors of those items,
                                for (int iNDS = 0; iNDS < numDataSets; iNDS++)
                                {
                                    for (int iiNDS = 0; iiNDS < numDataSets; iiNDS++)
                                    {
                                        // for all possible alignments
                                        for (int competAlign = 0; competAlign < numSlices; competAlign++)
                                        {
                                            if (domain == WORDS)
                                            {
                                                // calculate length (in slices) of this word, and its currently examined competitor
                                                int wordExtent = (param.getDeltaInput() / param.getSlicesPerPhon()) * lexicon.get(iNDS).getPhon().length();
                                                int competExtent = (param.getDeltaInput() / param.getSlicesPerPhon()) * lexicon.get(iiNDS).getPhon().length();

                                                // if the word and the competitor overlap
                                                if ((alignment == competAlign) ||
                                                    (competAlign > alignment && (alignment + wordExtent) >= competAlign) ||
                                                    (competAlign < alignment && (competAlign + competExtent) >= alignment)) 
                                                {
                                                    // collect two-dimensional denominator info, ala Post-Hoc and Ad-Hoc.
                                                    // varies depending on both cycle and item.
                                                    denominatorTwo[iDSL][iNDS] += responseStrengthData[iDSL][iiNDS][competAlign];
                                                } 

                                                //if(((alignment<=(competAlign+competExtent))&&alignment>=competAlign&&competExtent>0&&wordExtent>0)||
                                                //   ((competAlign<=(alignment+wordExtent))&&competAlign>=alignment&&competExtent>0&&wordExtent>0))
                                                //    denominatorTwo[iDSL][iNDS] += responseStrengthData[iDSL][iiNDS][competAlign];
                                            }
                                            else // domain == PHONEMES
                                            {
                                                // here, just use this phoneme and its neighbors

                                                if (competAlign == alignment || competAlign == (alignment - 1) || competAlign == (alignment + 1))
                                                    denominatorTwo[iDSL][iNDS] += responseStrengthData[iDSL][iNDS][competAlign];
                                            }
                                        }
                                    }
                                }
                                break;                        
                        }
                    }
                    else if (choice == FORCED)
                    {
                        // similar, but now instead of adding all items (iNDS), just loop/add
                        // over the ones we're analyzing

                        switch (calculationType)
                        {
                            case AVERAGE:
                                for (int iSlices = 0; iSlices < numSlices; iSlices++)
                                {
                                    for (int iII = 0; iII < itemIndices.length; iII++)
                                    {
                                        denominator[iDSL] += responseStrengthData[iDSL][itemIndices[iII]][iSlices];
                                    }
                                }
                                break;
                            case MAX_ADHOC:
                                for (int iII = 0; iII < itemIndices.length; iII++)
                                    for (int iiII = 0; iiII < itemIndices.length; iiII++)
                                    {
                                        denominatorTwo[iDSL][itemIndices[iII]] += responseStrengthData[iDSL][itemIndices[iiII]][alignmentAdHoc[itemIndices[iII]][iDSL]];
                                    }
                                break;
                            case MAX_ADHOC_2:
                                for (int iII = 0; iII < itemIndices.length; iII++)
                                    for (int iiII = 0; iiII < itemIndices.length; iiII++)
                                    {
                                        denominatorTwo[iDSL][itemIndices[iII]] += responseStrengthData[iDSL][itemIndices[iiII]][alignmentAdHoc[itemIndices[iII]][iDSL]];
                                    }
                                break;
                            case MAX_POSTHOC:
                                for (int iII = 0; iII < itemIndices.length; iII++)
                                    //for (int iiII = 0; iiII < itemIndices.length; iiII++)
                                    {
                                        //denominatorTwo[iDSL][itemIndices[iII]] += responseStrengthData[iDSL][itemIndices[iiII]][alignmentPostHoc[itemIndices[iII]]];
                                        denominator[iDSL] += responseStrengthData[iDSL][itemIndices[iII]][alignmentPostHoc[itemIndices[iII]]];
                                    }
                                break;
                            case STATIC:
                                for (int iII = 0; iII < itemIndices.length; iII++)
                                {
                                    denominator[iDSL] += responseStrengthData[iDSL][itemIndices[iII]][alignment];
                                }
                                break;
                            case FRAUENFELDER:
                                // this too is sorta like AVERAGE, as in NORMAL choice above, except that words have to
                                // both be in the list and overlap
                                //String target = TraceWord.stripDashes(param.getModelInput());
                                //int targetExtent = target.length() * param.getDeltaInput() / param.getSlicesPerPhon();

                                // this is the same idea as with normal choice, above, except iterate only over selected
                                // words/phonemes.
                                for (int iII = 0; iII < itemIndices.length; iII++)
                                {                                
                                    for (int iiII = 0; iiII < itemIndices.length; iiII++)
                                    {
                                        for (int competAlign = 0; competAlign < numSlices; competAlign++)
                                        {
                                            if (domain == WORDS)
                                            {
                                                int wordExtent = (param.getDeltaInput() / param.getSlicesPerPhon()) * lexicon.get(itemIndices[iII]).getPhon().length();
                                                int competExtent = (param.getDeltaInput() / param.getSlicesPerPhon()) * lexicon.get(itemIndices[iiII]).getPhon().length();

                                                // not sure why different logic is used here vs. above... might be a problem.
                                                if (( (alignment <= (competAlign + competExtent)) && alignment >= competAlign && competExtent > 0 && wordExtent > 0) ||
                                                    ( (competAlign <= (alignment + wordExtent)) && competAlign >= alignment && competExtent > 0 && wordExtent > 0))
                                                {
                                                    denominatorTwo[iDSL][itemIndices[iII]] += responseStrengthData[iDSL][itemIndices[iiII]][competAlign];                                            
                                                }
                                            }
                                            else // domain == PHONEMES
                                            {
                                                if (competAlign == alignment || competAlign == (alignment - 1) || competAlign == (alignment + 1))
                                                        denominatorTwo[iDSL][itemIndices[iII]] += responseStrengthData[iDSL][itemIndices[iiII]][competAlign];
                                            }

                                        }
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            // compute the numerator and the plotData
            plotData = new double[itemIndices.length][dataSetLength];

            // loop over items
            for (int iII = 0; iII < itemIndices.length; iII++)
            {
                // loop over cycles
                for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                {
                    // compute the numerator
                    double numerator = 0;
                    double [][][] numSrc;   // points to either responseStrengthData or activationData;

                    if (contentType == RESPONSE_PROBABILITIES)
                        numSrc = responseStrengthData;
                    else //(contentType == ACTIVATIONS)
                        numSrc = activationData;

                    switch (calculationType)
                    {
                        case AVERAGE:
                            for (int iSlices = 0; iSlices < numSlices; iSlices++)
                                numerator += numSrc[iDSL][itemIndices[iII]][iSlices];
                            numerator /= numSlices;
                            break;
                        case MAX_ADHOC:
                            numerator = numSrc[iDSL][itemIndices[iII]][alignmentAdHoc[itemIndices[iII]][iDSL]];
                            break;
                        case MAX_ADHOC_2:
                            numerator = numSrc[iDSL][itemIndices[iII]][alignmentAdHoc[itemIndices[iII]][iDSL]];
                            break;
                        case MAX_POSTHOC:
                            numerator = numSrc[iDSL][itemIndices[iII]][alignmentPostHoc[itemIndices[iII]]];
                            break;
                        case STATIC:
                            numerator = numSrc[iDSL][itemIndices[iII]][alignment];
                            break;
                        case FRAUENFELDER:
                            numerator = (numSrc[iDSL][itemIndices[iII]][alignment] +
                                    numSrc[iDSL][itemIndices[iII]][alignment+1]) ;    // @@@ check for array out of bounds
                            break;
                    }

                    // compute the plotData
                    if (contentType == RESPONSE_PROBABILITIES){
                        switch (calculationType)
                        {
                            case AVERAGE:
                                plotData[iII][iDSL] = numerator / denominator[iDSL];
                                break;
                            case MAX_ADHOC:
                                plotData[iII][iDSL] = numerator / denominatorTwo[iDSL][itemIndices[iII]];
                                break;
                            case MAX_ADHOC_2:
                                plotData[iII][iDSL] = numerator / denominatorTwo[iDSL][itemIndices[iII]];
                                break;
                            case MAX_POSTHOC:
                                //plotData[iII][iDSL] = numerator / denominatorTwo[iDSL][itemIndices[iII]];
                                plotData[iII][iDSL] = numerator / denominator[iDSL];
                                break;
                            case STATIC:
                                plotData[iII][iDSL] = numerator / denominator[iDSL];
                                break;
                            case FRAUENFELDER:
                                plotData[iII][iDSL] = numerator / denominatorTwo[iDSL][itemIndices[iII]];
                                break;
                        }                    
                    }
                    else //ACTIVATIONS
                        plotData[iII][iDSL] = numerator;
                }
            }
                
            // convert data to an XYSeriesCollection
            XYSeries oneSeries;
            ret = new XYSeriesCollection();        

            for (int iII = 0; iII < itemIndices.length; iII++)
            {
                // get the name of the series from the lexicon (for purposes of this stub)
                if(domain == WORDS){
                    
                    if(calculationType == MAX_POSTHOC)
                        oneSeries = new XYSeries(param.getLexicon().get(itemIndices[iII]).getPhon().concat(" ["+alignmentPostHoc[itemIndices[iII]]+"]"), true, true); // allow auto-sort and duplicate Xs
                    else
                        oneSeries = new XYSeries(param.getLexicon().get(itemIndices[iII]).getPhon(), true, true); // allow auto-sort and duplicate Xs
                    
                    if(calculationType == MAX_POSTHOC)
                        oneSeries.setDescription(oneSeries.getName().concat(" "+alignmentPostHoc[itemIndices[iII]]));                
                    else
                        oneSeries.setDescription(oneSeries.getName());                
                }
                else{ //if(domain == PHONEMES)
                    oneSeries = new XYSeries(param.getPhonemeLabels()[itemIndices[iII]], true, true); // allow auto-sort and duplicate Xs
                    if(calculationType == MAX_POSTHOC)
                        oneSeries.setDescription(oneSeries.getName().concat(" "+alignmentPostHoc[itemIndices[iII]]));
                    else
                        oneSeries.setDescription(oneSeries.getName());                
                }
                for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                {
                    // X is time step, y is data
                    oneSeries.add(iDSL, plotData[iII][iDSL]);                
                }            
                ret.addSeries(oneSeries);
                //System.out.println("series"+ret.getSeries().size()+" "+oneSeries.getName());            
            }  
        }
        else{ //if competitionIndex           
            // convert data to an XYSeriesCollection
            XYSeries oneSeries;
            ret = new XYSeriesCollection();        
            
            itemsToWatch.add(new String("Competition Index"));
            
        
            
            double compIndex [];
            if(domain==this.WORDS){
                compIndex = sim.getGlobalLexicalCompetition();
                oneSeries = new XYSeries("Lexical Competition", true, true); // allow auto-sort and duplicate Xs            
            }
            else{
                compIndex = sim.getGlobalPhonemeCompetition();
                oneSeries = new XYSeries("Phoneme Competition", true, true); // allow auto-sort and duplicate Xs            
            }
            
            if(competType==RAW){ // competIndex, raw, not a slope line
                
                for (int iDSL = 0; iDSL < dataSetLength; iDSL++)
                    {
                        // X is time step, y is data
                        //System.out.println("add\t"+iDSL+"\t"+compIndex[iDSL]);
                        oneSeries.add(iDSL, compIndex[iDSL]);                
                    }
                ret.addSeries(oneSeries);
            }
            else if(competType==FIRST_DERIVATIVE){ // first derivative                                
                double[] firstDeriv = slopeRegress(compIndex, competSlope, dataSetLength);            
                for (int iDSL = 0; iDSL < firstDeriv.length; iDSL++)
                    {
                        // X is time step, y is data
                        //System.out.println("add\t"+iDSL+"\t"+firstDeriv[iDSL]);
                        oneSeries.add(iDSL, firstDeriv[iDSL]);                
                    }
                ret.addSeries(oneSeries);
            }
            else if(competType==SECOND_DERIVATIVE){ // second derivative                                
                double[] firstDeriv = slopeRegress(compIndex, competSlope, dataSetLength-1);            
                double[] secondDeriv = slopeRegress(firstDeriv, competSlope, dataSetLength-2);            
                for (int iDSL = 0; iDSL < secondDeriv.length; iDSL++)
                    {
                        // X is time step, y is data
                        oneSeries.add(iDSL, secondDeriv[iDSL]);                
                    }
                ret.addSeries(oneSeries);
            }                    
        }
        
        // and that's it!                        
        return ret;
    }
    
    private double[] slopeRegress(double[] dat, int width, int length){
        /*
         * to do the regression line, for the compet slope stuff, use:
         * b = SUM[(x-avg(x))(y-avg(y))] / SUM[(x-avg(x))^2]
         **/
        double[] deriv = new double[length];                
        double[] xAxis = makeXAxis(deriv.length);
        for(int iCI = 0; iCI < deriv.length-1; iCI++){
            double sumxyCI = 0, sumxxCI = 0;
            int idxCI; //tickCI=0;
            for(int jCI = 0 - width/2; jCI < width/2 && jCI < dat.length; jCI++){
                idxCI = iCI+jCI;
                if(idxCI<0) continue;
                if(idxCI>=xAxis.length) break;
                sumxyCI+=((xAxis[idxCI]-averagingOp(xAxis,iCI,width))*(dat[idxCI]-averagingOp(dat,iCI,width)));
                sumxxCI+=((xAxis[idxCI]-averagingOp(xAxis,iCI,width))*(xAxis[idxCI]-averagingOp(xAxis,iCI,width)));                
            }
            //System.out.println(iCI+"\tb="+(sumxyCI/sumxxCI)+" = "+sumxyCI+" / "+sumxxCI+" ["+dat[iCI]+"]");                    
            deriv[iCI] = sumxyCI / sumxxCI;                
        }
        return deriv;
    }
    private double averagingOp(double[] data, int midIndex, int width){
        double res=0;
        int tick = 0;
        for(int i = midIndex - (width/2); i < midIndex + (width/2); i++){
            if(i<0) continue;
            if(i>=data.length) break;
            res += data[i];
            tick++;
        }
        res /= tick;
        return res;
    }
    private double[] makeXAxis(int len){
        double[] res = new double[len+1];
        for(int i=1;i<=len+1;i++){
            res[i-1]=(double)i;
        }
        return res;
    }
    
    /*
     * For each sim in the iterator, calculate the analysis, then sum the
     * results to the averagedAnalysis XYSeriesCollection.  When the signal
     * comes that the iterator is finished, calculate the mean and return
     * that.  Also, keep in mind, if there is any discrepency in dimensions
     * from one analysis to the next, the averagedAnalysis will always
     * get trimmed down to the smaller dimensions.  The user is responsible
     * for making sure this analysis is interesting, e.g. correct 
     * item ordering.    
    */    
    public void averageAnalysisReset(){
        averagedAnalysis = null;
    }
    public XYSeriesCollection averageAnalysisCompletion(){
        if(currentlyIterating==false) return averagedAnalysis;
        
        for(int i=0;i<averagedAnalysis.getSeriesCount();i++){
            for(int j=0;j<averagedAnalysis.getSeries(i).getItemCount();j++){
                Number accum = averagedAnalysis.getSeries(i).getDataItem(j).getY();
                double newValue = accum.doubleValue() / (double)simsInIterator;
                averagedAnalysis.getSeries(i).getDataItem(j).setY(newValue);
            }
        }
        currentlyIterating = false;
        return averagedAnalysis;
    }
    public void averageAnalysisStep(TraceSim sim, java.util.LinkedList labels){                
        XYSeriesCollection currentAnalysis = doAnalysis(sim);
        //if analysis returned nothing, then return.
        //if(currentAnalysis==null||currentAnalysis.getSeriesCount()==0)
        //    return;
        //if the average iterator settings have not yet been initialized, do that.
        if(averagedAnalysis == null){
            averagedAnalysis = currentAnalysis;            
            setLabels(averagedAnalysis,labels);
            simsInIterator=1;
            currentlyIterating=true;
            return;
        }
        //otherwise, increment the counter ... 
        simsInIterator++;
        
        //System.out.println("avg analysis:\t"+sim.getInputString()+"\t"+averagedAnalysis.getSeriesCount());
        
        //and add the current analysis to averagedAnalysis
        for(int i=0;i<averagedAnalysis.getSeriesCount();i++){
            //if the averagedAnalysis is longer than the current analysis,
            //then we must trim the averagedAnalysis
            if(i>=currentAnalysis.getSeriesCount()){
                while(averagedAnalysis.getSeriesCount()>i)
                    averagedAnalysis.removeSeries(i);                
                break;
            }
            
                for(int j=0;j<averagedAnalysis.getSeries(i).getItemCount();j++){
                //if the averagedAnalysis is longer than the current analysis,
                //then we must trim the averagedAnalysis
                if(j>=currentAnalysis.getSeries(i).getItemCount()){
                    while(averagedAnalysis.getSeries(i).getItemCount()>j)
                        averagedAnalysis.getSeries(i).remove(j);                
                    break;
                }
                //add the value from the current analysis to the averaged analysis.
                Number accum = averagedAnalysis.getSeries(i).getDataItem(j).getY();
                Number step = currentAnalysis.getSeries(i).getDataItem(j).getY();                                
                double newValue = accum.doubleValue() + step.doubleValue();
                averagedAnalysis.getSeries(i).getDataItem(j).setY(newValue);
            }
        }        
    }
    
    public Vector discoverItemsToWatch(TraceSim sim){                
        //TODO : 
        //create a clone of this simAnalysis
        //then set the itemsToWatch vector to the full lexicon/phon list
        //run the analysis
        //discover the top N items
        //put them in a vector and return.
        TraceSimAnalysis temp = new TraceSimAnalysis(this);
        temp.setWatchType(WATCHSPECIFIED);
        if (domain == WORDS)
            temp.setItemsToWatch(sim.getParameters().getLexicon());
        else
            temp.setItemsToWatch(sim.getParameters().getPhonemeLabels());
        XYSeriesCollection xyseries = temp.doAnalysis(sim);
        XYSeries toppeaks = new XYSeries("peaks",true,true); //series name, auto-sort, permit-duplicates
        int max;
        //collect peaks for all series
        //int ceil;
        //if(domain == WORDS) ceil = sim.getParameters().getLexicon().toStringArray().length;
        //else ceil = sim.getParameters().getPhonemeLabels().length;
        
        for(int h=0;h<xyseries.getSeriesCount()&&h<sim.getParameters().getLexicon().toStringArray().length;h++){
            max=0;
            for(int j=0;j < xyseries.getSeries(h).getItemCount(); j++){
                if(xyseries.getSeries(h).getDataItem(j).getY().doubleValue()>xyseries.getSeries(h).getDataItem(max).getY().doubleValue()){
                    max = j;
                }                    
            }            
            toppeaks.add(new XYDataItem((java.lang.Double)xyseries.getSeries(h).getDataItem(max).getY(),new java.lang.Integer(h)));            
            //System.out.println("toppeaks "+h+"\t"+sim.getParameters().getLexicon().get(h));
        }
        //place the top N item labels into a vector 
        //they have been automatically sorted (ascending X-value) by the XYSeries class
        Vector result = new Vector();
        for(int i=(toppeaks.getItemCount()-1);i>(toppeaks.getItemCount()-(1+watchTopN))&&i>0;i--){
            if(domain == WORDS){
                //System.out.println("array size: "+sim.getParameters().getLexicon().toStringArray().length+", Y: "+toppeaks.getDataItem(i).getY().intValue()+"\tX: "+toppeaks.getDataItem(i).getX());
                //if(toppeaks.getDataItem(i).getY().intValue()>=sim.getParameters().getLexicon().toStringArray().length){
                //    result.add(sim.getParameters().getLexicon().toStringArray()[sim.getParameters().getLexicon().toStringArray().length-1]);                  
                //}
                //else{
                    result.add(sim.getParameters().getLexicon().toStringArray()[toppeaks.getDataItem(i).getY().intValue()]);
                //}
            }
            else{ //if(domain == PHONEMES){
                result.add(sim.getParameters().getPhonemeLabels()[toppeaks.getDataItem(i).getY().intValue()]);
            }
        }
        return result;
    }
    public void addOneWatchedItem(String itm){
        itemsToWatch.add(itm);
    }
    public boolean removeOneWatchedItem(String itm){
        return itemsToWatch.remove(itm);
    }
    
    /**
     * Get indices into the second arg for items that match an element in the
     * first arg.
     */
    public int [] itemsToArrayIndices(Vector items, String[] compare){
        Vector vResult = new Vector();
        for (int i=0; i < items.size(); i++)
            for(int j=0; j < compare.length; j++)
                if (((String)items.get(i)).equals(compare[j])){
                    vResult.add(new Integer(j));
                }
        
        // convert to fixed-length array
        int [] result = new int[vResult.size()];
        for (int i = 0; i < vResult.size(); i++)
            result[i] = ((Integer)vResult.get(i)).intValue();
        
        return result;
    }
    private XYSeriesCollection setLabels(XYSeriesCollection col,java.util.LinkedList labels){
        if(col==null||col.getSeriesCount()==0)
            return col;
        for(int i=0;i<col.getSeriesCount()&&i<labels.size();i++){
            col.getSeries(i).setName(((edu.uconn.psy.jtrace.Model.Scripting.Text)labels.get(i)).value());
        }
        return col;
    }
    public boolean isCurrentlyIterating(){
        return currentlyIterating;
    }
    
//    //DOES WORD J ALIGNED TO SLICE H OVERLAP WITH WORD _WORD ALIGNED TO SLICE _ALIGN ???
//    private boolean overlaps(int j,int h,int _word, int _align,TraceParam param){        
//        String[] words = param.getLexicon().toStringArray();
//        if(h == _align){ 
//            return true;
//        }
//        else if(h > _align){ 
//            if((_align+(words[_word].length()*2)) >= h) //-1 ??
//                return true;
//            else
//                return false;
//        }
//        else if(h < _align && ((h + (words[j].length()*2))) >= _align){  //-1 ??
//            return true;
//        }
//        else{  // ?? unknown case
//            return false;
//        }
//    }
    /**
     * Exports the specified data to the specified file. This can be moved to
     * another class if desired.
     *
     * @param an        results of doAnalysis()
     * @param outfile   file to save to
     * @return          success
     */
    public static boolean exportAnalysis(XYSeriesCollection an, File outfile, boolean append)
    {
        XYSeries oneSeries;
        FileWriter fwOut;
        
        try 
        {
            fwOut = new FileWriter(outfile, append);
        
            // print series data
            for (int iDSL = 0; iDSL < an.getSeries(0).getItemCount(); iDSL++)
            {                
                if(iDSL==0){
                    fwOut.write("cycle\t");
                    // print series names                
                    for (int iNDS = 0; iNDS < an.getSeriesCount(); iNDS++)
                    {                   
                        fwOut.write(an.getSeriesName(iNDS) + "\t");
                    }
                    fwOut.write("\n");
                }
                
                // print the cycle number
                fwOut.write(an.getSeries(0).getX(iDSL).intValue() + "\t");
                
                // print series data
                for (int iNDS = 0; iNDS < an.getSeriesCount(); iNDS++)
                {                       
                    //oneSeries = an.getSeries(iNDS);
                    fwOut.write(an.getSeries(iNDS).getY(iDSL) + "\t");
                    //
                    //System.out.println(an.getSeries(iNDS).getY(iDSL));
                }
                fwOut.write("\n");
            }
            fwOut.write("\n");
            fwOut.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    /**
     * Getter for property contentType.
     * @return Value of property contentType.
     */
    public int getContentType() {

        return this.contentType;
    }

    /**
     * Setter for property contentType.
     * @param contentType New value of property contentType.
     */
    public void setContentType(int contentType) {

        this.contentType = contentType;
    }

    /**
     * Getter for property watchType.
     * @return Value of property watchType.
     */
    public int getWatchType() {

        return this.watchType;
    }

    /**
     * Setter for property watchType.
     * @param watchType New value of property watchType.
     */
    public void setWatchType(int watchType) {

        this.watchType = watchType;
    }
    
    public void setCompetIndexType(int comp){
        competType = comp;
    }
    public int getCompetIndexType(){
        return competType;
    }    
    public void setCompetIndexSlope(int slop){
        competSlope = slop;
    }
    public int getCompetIndexSlope(){
        return competSlope;
    }
    
    public String XMLTag(){
        //XML for the TraceSimAnalysis is given as a series of scripting actions.
        String result="";
        if(domain==WORDS) result+="<action><set-graph-domain><domain>WORDS</domain></set-graph-domain></action>";
        else if(domain==PHONEMES) result+="<action><set-graph-domain><domain>PHONEMES</domain></set-graph-domain></action>";
        if(calculationType==STATIC) result+="<action><set-analysis-type><type>SPECIFIED</type></set-analysis-type></action>";
        else if(calculationType==MAX_ADHOC) result+="<action><set-analysis-type><type>MAX-ADHOC</type></set-analysis-type></action>";
        else if(calculationType==MAX_POSTHOC) result+="<action><set-analysis-type><type>MAX-POSTHOC</type></set-analysis-type></action>";
        else if(calculationType==FRAUENFELDER) result+="<action><set-analysis-type><type>FRAUNFELDER</type></set-analysis-type></action>";
        else if(calculationType==AVERAGE) result+="<action><set-analysis-type><type>AVERAGE</type></set-analysis-type></action>";        
        if(choice == NORMAL) result+="<action><set-choice-type><type>NORMAL</type></set-choice-type></action>";
        else if(choice == FORCED) result+="<action><set-choice-type><type>FORCED</type></set-choice-type></action>";
        if(contentType == ACTIVATIONS) result+="<action><set-content-type><type>ACTIVATIONS</type></set-content-type></action>";
        else if(contentType == RESPONSE_PROBABILITIES) result+="<action><set-content-type><type>RESPONSE-PROBABILITIES</type></set-content-type></action>";
        else if(contentType == COMPETITION_INDEX){ 
            result+="<action><set-content-type><type>COMPETITION</type></set-content-type></action>";
            result+="<action><set-comp-calc-type><type>";
            if(competType==RAW) result+="RAW";
            else if(competType==FIRST_DERIVATIVE) result+="FIRST-DERIVATIVE";
            else if(competType==SECOND_DERIVATIVE) result+="SECOND-DERIVATIVE";
            result+="</type></set-comp-calc-type></action>";
            result+="<action><set-comp-slope-width><type>"+competSlope+"</type></set-comp-slope-width></action>";
        }
        result+="<action><set-k-value><exponent>"+kValue+"</exponent></set-k-value></action>";  
        if(watchType == WATCHTOPN) result+="<action><set-watch-type><type>WATCH-TOP-N</type></set-watch-type></action>";
        else if(watchType == WATCHSPECIFIED) result+="<action><set-watch-type><type>WATCH-SPECIFIED</type></set-watch-type></action>";
        if(watchType == WATCHTOPN) result+="<action><set-watch-top-n><N>"+watchTopN+"</N></set-watch-top-n></action>";
        else if(watchType == WATCHSPECIFIED){ 
            result+="<action><set-watch-items><list>";
            for(int i=0;i<itemsToWatch.size();i++)
                result+="<watch><text>"+(String)itemsToWatch.get(i)+"</text></watch>";
            result+="</list></set-watch-items></action>";
        }        
        return result;            
    
    }       
}
