package generatebowbaseline;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
/**
 *
 * @author kico masotomayor
 */
public class Features {
    public int NComas = 0;
    public int NPuntos = 0;
    public int N2Puntos = 0;
    public int NExclamacion = 0;
    public int NInterrogacion = 0;
    public int NUpperCase = 0;
    public int NLaugh = 0;
    public int NEndingIco = 0;
    public int NEndingIto = 0;
    public int NEnding = 0;
    public int NTypical = 0;
    //public int NDistinctWords = 0;

    private static final String[] VALUES_LAUGH = new String[] { "jeje", "jaja", "xddd", "hehe", "haha", "jiji", "risa" };
    private static final Set<String> Laugh = new HashSet<>(Arrays.asList(VALUES_LAUGH));

    private static final String[] VALUES_ENDING = new String[] { "ás", "és", "ís" };
    private static final Set<String> ending = new HashSet<>(Arrays.asList(VALUES_ENDING));
    
    public void GetNumFeatures(String text) {
        for (int i=0;i<text.length();i++) {
            if (text.charAt(i) == ',') {
                NComas++;
            }
            if (text.charAt(i) == '.') {
                NPuntos++;
            }
            if (text.charAt(i) == ':') {
                N2Puntos++;
            }
            if (text.charAt(i) == '!') {
            	NExclamacion++;
            }
            if (text.charAt(i) == '?') {
            	NInterrogacion++;
            }
            if (Character.isUpperCase(text.charAt(i))){
            	NUpperCase++;
            }
        }

        for (String word : text.split(" ")) {
            //Laugh frequency
            if (word.length() > 4) {
                if (Laugh.contains(word.toLowerCase().substring(0, 3))) {
                    NLaugh++;
                }
            }
            //Ending frequency
            if (word.length() > 3) {
                if (ending.contains(word.substring(word.length() - 3))) {
                    NEnding++;
                }
            }

            if (word.length() > 3) {
                if (word.endsWith("ico")) {
                    NEndingIco++;
                }
                if (word.endsWith("ito")) {
                    NEndingIto++;
                }
            }

            //Typical Words
            if (word.contains("vos")) {
                NTypical++;
            }
        }

        //Ndistinct words
        //Multiset<String> wordCounts = HashMultiset.create(Arrays.asList(words));
        //NDistinctWords += wordCounts.size();
    }
}
