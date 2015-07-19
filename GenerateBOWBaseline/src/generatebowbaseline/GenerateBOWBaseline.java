package generatebowbaseline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author kicorangel masotomayor
 */
public class GenerateBOWBaseline {
    //Max. and min. of Character Ngram
    private static int MINSIZENGRAM = 2;
    private static int MAXSIZENGRAM = 5;
    
    public static void main(String[] args) {
        String TRUTH = "data/pan13/pan13-author-profiling-test-corpus2-2013-04-29/truth-es.txt";
        String PATH = "data/pan13/pan13-author-profiling-test-corpus2-2013-04-29/es/";
        String NGRAMChar = "data/pan13/ngram-esChar.txt";
        
        try {
			//Read truth file and it's inserted in memory
            Hashtable<String, TruthInfo> oTruth = ReadTruth(TRUTH);
            //Read Trigrams file, or generate it
            ArrayList<String>oNgramChar = ReadTrigramsChar(PATH, NGRAMChar);
            String OUTPUT = "data/pan13/pan-ap-13-training-es-{task}.arff";
            GenerateBaseline(PATH, oNgramChar, oTruth, OUTPUT.replace("{task}", "gender"), "MALE, FEMALE");
            GenerateBaseline(PATH, oNgramChar, oTruth, OUTPUT.replace("{task}", "age"), "10S, 20S, 30S");
            
        }catch (Exception ex) {
            
        }
    }

    /*
    Read corpus file.
    Generate the features vector to insert in a weka file
    * */
    private static void GenerateBaseline(String path, ArrayList<String> aNgramChar, Hashtable<String, TruthInfo> oTruth, String outputFile, String classValues) {
        FileWriter fw = null;
        int nTerms = 1000;
        
        try {
            fw = new FileWriter(outputFile);
            fw.write(Weka.HeaderToWeka(aNgramChar, nTerms, classValues));
            fw.flush();
            
            File directory = new File(path);
            String [] files = directory.list();
            for (int iFile = 0; iFile < files.length; iFile++) 
            {
                System.out.println("--> Generating " + (iFile+1) + "/" + files.length);
                try {
                    Hashtable<String, Integer> oDocBOW = new Hashtable<>();
                    Hashtable<String, Integer> oDocNgrams = new Hashtable<>();

                    String sFileName = files[iFile];

                    File fXmlFile = new File(path + "/" + sFileName);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(fXmlFile);
                    NodeList documents = doc.getDocumentElement().getElementsByTagName("conversation");
                    String []fileInfo = sFileName.split("_");
                    String sAuthor = fileInfo[0];
                    String sAuthorContent = "";
                    for (int i=0;i<documents.getLength();i++) {
                        try {
                            Element element = (Element)documents.item(i);
                            String sHtml = element.getTextContent();
                            String sContent = GetText(sHtml);

                            sAuthorContent += sContent + " " ;

                            StringReader reader = new StringReader(sContent);
                            
                            NGramTokenizer gramTokenizer = new NGramTokenizer(reader, MINSIZENGRAM, MAXSIZENGRAM);
                            CharTermAttribute charTermAttribute = gramTokenizer.addAttribute(CharTermAttribute.class);
                            gramTokenizer.reset();           
                            
                            gramTokenizer.reset();        
                            
                            while (gramTokenizer.incrementToken()) {
                                String sTerm = charTermAttribute.toString();
                                int iFreq = 0;
                                if (oDocBOW.containsKey(sTerm)) {
                                    iFreq = oDocBOW.get(sTerm);
                                }
                                oDocBOW.put(sTerm, ++iFreq);
                            }
                            
                            gramTokenizer.end();
                            gramTokenizer.close();
                        } catch (Exception ex) {
                                    System.out.println("ERROR: " + ex.toString());
                            String s = ex.toString();
                        }
                    }
                    
                    Features oFeatures = new Features();
                    oFeatures.GetNumFeatures(sAuthorContent);
                    
                    if (oTruth.containsKey(sAuthor)) {
                        TruthInfo truth = oTruth.get(sAuthor);
                        String sGender = truth.Gender.toUpperCase();
                        String sAge = truth.Age.toUpperCase();

                        if (classValues.contains("MALE")) {
                            fw.write(Weka.FeaturesToWeka(aNgramChar, oDocBOW, oDocNgrams, oFeatures, nTerms, sGender));
                        } else {
                            fw.write(Weka.FeaturesToWeka(aNgramChar, oDocBOW, oDocNgrams, oFeatures, nTerms, sAge));
                        }
                        fw.flush();
                    }

                 } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.toString());
                 }
            }
        } catch (Exception ex) {
            
        } finally {
            if (fw!=null) { try { fw.close(); } catch (Exception k) {} }
        }
    }

    private static ArrayList<String> ReadTrigramsChar(String corpusPath, String ngramPath) {
        Hashtable<String, Integer> oNgrams = new Hashtable<>();
        ArrayList<String> aNgrams = new ArrayList<>();

        if (new File(ngramPath).exists()) {
            FileReader fr = null;
            BufferedReader bf = null;

            try {
                fr = new FileReader(ngramPath);
                bf = new BufferedReader(fr);
                String sCadena = "";

                while ((sCadena = bf.readLine())!=null)
                {
                    String []data = sCadena.split(":::");
                    if (data.length==2) {
                        String sTerm = data[0];
                        aNgrams.add(sTerm);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            } finally {
                if (bf!=null) { try { bf.close(); } catch (Exception k) {} }
                if (fr!=null) { try { fr.close(); } catch (Exception k) {} }
            }
        } else {
            File directory = new File(corpusPath);
            File []files = directory.listFiles();

            for (int iFile = 0; iFile < files.length; iFile++)  {
                System.out.println("--> Preprocessing " + (iFile+1) + "/" + files.length);

                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(files[iFile]);
                    NodeList documents = doc.getDocumentElement().getElementsByTagName("conversation");

                    double iDocs = documents.getLength();
                    for (int i=0;i<iDocs;i++) {
                        Element element = (Element)documents.item(i);
                        String sHtml = element.getTextContent();
                        String sContent = GetText(sHtml);

                        StringReader reader = new StringReader(sContent);                        
                        
                        NGramTokenizer gramTokenizer = new NGramTokenizer(reader, MINSIZENGRAM, MAXSIZENGRAM);
                        CharTermAttribute charTermAttribute = gramTokenizer.addAttribute(CharTermAttribute.class);
                        gramTokenizer.reset();                                                                      

                        while (gramTokenizer.incrementToken()){
                        	String sTerm = charTermAttribute.toString();
                            int iFreq = 0;
                            if (oNgrams.containsKey(sTerm)) {
                                iFreq = oNgrams.get(sTerm);
                            }
                            oNgrams.put(sTerm, ++iFreq);
                            //System.out.println(charTermAttribute.toString());
                        }
                        
                        gramTokenizer.end();
                        gramTokenizer.close();
                    }                                       
                } catch (Exception ex) {

                }
            }

            //Se ordena por frecuencia
            ValueComparator bvc =  new ValueComparator(oNgrams);
            TreeMap<String,Integer> sorted_map = new TreeMap<>(bvc);
            sorted_map.putAll(oNgrams);

            //Se guarda en disco
            FileWriter fw = null;
            try {
                fw = new FileWriter(ngramPath);
                for( Iterator it = sorted_map.keySet().iterator(); it.hasNext();) {
                    String sTerm = (String)it.next();
                    int iFreq = oNgrams.get(sTerm);

                    aNgrams.add(sTerm);
                    fw.write(sTerm + ":::" + iFreq + "\n");
                    fw.flush();
                }
            } catch (Exception ex) {

            } finally {
                if (fw!=null) { try {fw.close();} catch(Exception k) {} }
            }
        }

        return aNgrams;
    }

    private static Hashtable<String, TruthInfo> ReadTruth(String path) {
        Hashtable<String, TruthInfo> oTruth = new Hashtable<String, TruthInfo>();
        
        FileReader fr = null;
        BufferedReader bf = null;
        
        try {
            fr = new FileReader(path);
            bf = new BufferedReader(fr);
            String sCadena = "";

            while ((sCadena = bf.readLine())!=null)
            {
                String []data = sCadena.split(":::");
                if (data.length==3) {
                    String sAuthorId = data[0];
                    if (!oTruth.containsKey(sAuthorId)) {
                        TruthInfo info = new TruthInfo();
                        info.Gender = data[1];
                        info.Age= data[2];
                        oTruth.put(sAuthorId, info);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            if (bf!=null) { try { bf.close(); } catch (Exception k) {} }
            if (fr!=null) { try { fr.close(); } catch (Exception k) {} }
        }
        
        return oTruth;
    }
    
    public static String GetText(String html)
    {
        try {
            Html2Text html2text = new Html2Text();
            Reader in = new StringReader(html);
            html2text.parse(in);
            return html2text.getText();
        } catch (IOException ex) {
            return html;
        }
    }
}