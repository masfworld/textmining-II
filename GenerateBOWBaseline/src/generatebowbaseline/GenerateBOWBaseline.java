package generatebowbaseline;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

/**
 * @author kicorangel masotomayor
 */
public class GenerateBOWBaseline {
    //Max. and min. of Character Ngram
    private static int MINSIZENGRAM = 2;
    private static int MAXSIZENGRAM = 5;
    
    public static void main(String[] args) {
        String TRUTH = "data/hispatweets/training.txt";
        String PATH = "data/hispatweets/";
        String NGRAMChar = "data/hispatweets/ngram-esChar.txt";
        
        try {
			//Read truth file and it's inserted in memory
            Hashtable<String, TruthInfo> oTruth = ReadTruth(TRUTH);
            //Read Trigrams file, or generate it
            ArrayList<String>oNgramChar = ReadTrigramsChar(PATH, NGRAMChar);
            String OUTPUT = "data/hispatweets/tweets-training-es-{task}.arff";
            GenerateBaseline(PATH, oNgramChar, oTruth, OUTPUT.replace("{task}", "gender"), "MALE, FEMALE");
            GenerateBaseline(PATH, oNgramChar, oTruth, OUTPUT.replace("{task}", "country"), "MEXICO, COLOMBIA, VENEZUELA, ESPANA, ARGENTINA, PERU, CHILE");
            
        }catch (Exception ignored) {
            
        }
    }

    /**
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

            ArrayList<File> files = getFilesFromSubfolders(path, new ArrayList<File>());

            assert files != null;
            int countFiles = 0;
            for (File file : files)
            {
                System.out.println("--> Generating " + (++countFiles) + "/" + files.size());
                try {
                    Hashtable<String, Integer> oDocBOW = new Hashtable<>();
                    Hashtable<String, Integer> oDocNgrams = new Hashtable<>();

                    String sFileName = file.getName();

                    //File fJsonFile = new File(path + "/" + sFileName);
                    //Get name without extension
                    String sAuthor = sFileName.substring(0, sFileName.lastIndexOf('.'));

                    Scanner scn = new Scanner(file, "UTF-8");
                    String sAuthorContent = "";
                    //Reading and Parsing Strings to Json
                    while(scn.hasNext()){
                        JSONObject tweet= (JSONObject) new JSONParser().parse(scn.nextLine());

                        String textTweet = (String) tweet.get("text");

                        sAuthorContent += textTweet + " " ;

                        StringReader reader = new StringReader(textTweet);

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
                    }
                    
                    Features oFeatures = new Features();
                    oFeatures.GetNumFeatures(sAuthorContent);

                    if (oTruth.containsKey(sAuthor)) {
                        TruthInfo truth = oTruth.get(sAuthor);
                        String sGender = truth.gender.toUpperCase();
                        //If gender is unknown, this author is not interesting
                        if (sGender.equals("UNKNOWN")) continue;
                        String sCountry = truth.country.toUpperCase();

                        if (classValues.contains("MALE")) {
                            fw.write(Weka.FeaturesToWeka(aNgramChar, oDocBOW, oDocNgrams, oFeatures, nTerms, sGender));
                        } else {
                            fw.write(Weka.FeaturesToWeka(aNgramChar, oDocBOW, oDocNgrams, oFeatures, nTerms, sCountry));
                        }
                        fw.flush();
                    }

                 } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.toString());
                 }
            }
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.toString());
        } finally {
            if (fw!=null) { try { fw.close(); } catch (Exception ignored) {} }
        }
    }

    /**
     * Read NGrams. This method read from file if it exists, in other case generate this file
     * */
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
            ArrayList<File> files = getFilesFromSubfolders(corpusPath, new ArrayList<File>());
            //File directory = new File(corpusPath);
            //File []files = directory.listFiles();

            int countFiles = 0;
            for (File file : files)  {
                System.out.println("--> Preprocessing " + (++countFiles) + "/" + files.size());

                try {
                    Scanner scn = new Scanner(file, "UTF-8");

                    //Reading and Parsing Strings to Json
                    while(scn.hasNext()){
                        JSONObject tweet= (JSONObject) new JSONParser().parse(scn.nextLine());

                        String textTweet = (String) tweet.get("text");

                        StringReader reader = new StringReader(textTweet);

                        NGramTokenizer gramTokenizer = new NGramTokenizer(reader, MINSIZENGRAM, MAXSIZENGRAM);
                        CharTermAttribute charTermAttribute = gramTokenizer.addAttribute(CharTermAttribute.class);
                        gramTokenizer.reset();

                        while (gramTokenizer.incrementToken()){
                            String sTerm = charTermAttribute.toString();
                            if (sTerm.endsWith(":")){
                                sTerm = sTerm.substring(0, sTerm.length()-1);
                            }
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
                    System.out.println("Error reading JSON file");
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
                System.out.println("ERROR: " + ex.toString());
            } finally {
                if (fw!=null) { try {fw.close();} catch(Exception k) {} }
            }
        }

        return aNgrams;
    }

    /**
     * Read truth file where class item is known
     *
     * @return hash key=author, value=true class
     * */
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
                        info.country = data[1];
                        info.gender = data[2];
                        oTruth.put(sAuthorId, info);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            if (bf!=null) { try { bf.close(); } catch (Exception ignored) {} }
            if (fr!=null) { try { fr.close(); } catch (Exception ignored) {} }
        }
        
        return oTruth;
    }

    /**
     * Get all files from directory.
     * It returns files from subfolders and discard first level files from directoryName
     * */
    private static ArrayList<File> getFilesFromSubfolders(String directoryName, ArrayList<File> files) {

        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files.add(file);
            } else if (file.isDirectory()) {
                getFilesFromSubfolders(file.getAbsolutePath(), files);
            }
        }
        return files;
    }
}