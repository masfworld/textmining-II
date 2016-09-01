package generatebowbaseline;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author kico masotomayor
 */
public class Weka {
    
    public static String HeaderToWeka(ArrayList<String> NgramChar, int iNTerms, String classValue)
    {
        String sHeader =  "@relation 'BOW'\n" ;

        for (int i=0;i<NgramChar.size();i++) {
            String sTerm = NgramChar.get(i);
            sHeader += "@attribute 'term-" + sTerm.replaceAll("'", "quote") + "' real\n";
            
            if (i>=iNTerms) {
                break;
            }
        }       
        
        sHeader += "@attribute 'ncomas' real\n" +
                "@attribute 'npuntos' real\n" +
                "@attribute 'n2puntos' real\n"+
                "@attribute 'nexclamacion' real\n"+
                "@attribute 'ninterrogacion' real\n"+
                "@attribute 'nuppercase' real\n"+
                "@attribute 'nlaugh' real\n"+
                "@attribute 'nending' real\n"+
                "@attribute 'nendingIco' real\n"+
                "@attribute 'nendingIto' real\n"+
                "@attribute 'ntypical' real\n";
        sHeader += "@attribute 'class' {" + classValue + "}\n" +
        "@data\n";
        return sHeader;
    }
    
    public static String FeaturesToWeka(ArrayList<String> NgramChar, Hashtable<String, Integer>oDoc, Hashtable<String, Integer>oDocNgrama, Features oFeatures, int iN, String classValue)    {
        String weka = "";
        int iTotal = oDoc.size();
        for (int i=0;i<NgramChar.size();i++) {
            String sTerm = NgramChar.get(i);
            double freq = 0;
            if (oDoc.containsKey(sTerm)) {
                freq = ((double)oDoc.get(sTerm) / (double)iTotal);
            }
            
            weka += freq + ",";
            
            if (i>=iN) {
                break;
            }
        }
              
        if (iTotal != 0) {
            weka += ((double) oFeatures.NComas / (double) iTotal) + "," +
                    ((double) oFeatures.NPuntos / (double) iTotal) + "," +
                    ((double) oFeatures.N2Puntos / (double) iTotal) + "," +
                    ((double) oFeatures.NExclamacion / (double) iTotal) + "," +
                    ((double) oFeatures.NInterrogacion / (double) iTotal) + "," +
                    ((double) oFeatures.NUpperCase / (double) iTotal) + "," +
                    ((double) oFeatures.NLaugh / (double) iTotal) + "," +
                    ((double) oFeatures.NEnding / (double) iTotal) + "," +
                    ((double) oFeatures.NEndingIco / (double) iTotal) + "," +
                    ((double) oFeatures.NEndingIto / (double) iTotal) + "," +
                    ((double) oFeatures.NTypical / (double) iTotal) + ",";
        }
        else{
            weka += (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + "," +
                    (0.0) + ",";
        }
        weka +=  classValue + "\n";
        
        return weka;
    }
}