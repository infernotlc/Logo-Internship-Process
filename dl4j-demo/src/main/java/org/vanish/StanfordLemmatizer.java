package org.vanish;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.BasicConfigurator;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;



public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        // pos taggingle beraber stanfordnlp object properties üret
        // lematizasyon için gerekli
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        // StanfordCoreNLP birçok model yüklediği için çalışmam anında bir kere yapmak isteriz
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String documentText)
    {
        List<String> lemmas = new LinkedList<String>();

        // verilen metin ile annotasyon oluştur
        Annotation document = new Annotation(documentText);

        // bu metindeki bütün açıklamaları(annotationları) çalıştır
        this.pipeline.annotate(document);

        // bulunan bütün cümleleri dolan
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // cümledeki bütün tokenleri dolan
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // lemma (kök) listesine kelimelerin köklerini ekle
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }

        return lemmas;
    }
    public static String text = "Marie was born in Paris. \n" +
            "Logo's workers worked in Logo's Softwares";
    public static void main(String[] args) {
        BasicConfigurator.configure();


        Properties props = new Properties();
        // etiketleri ayarla
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // pipeline'ı oluştur
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // document objesi oluştur
        CoreDocument document = pipeline.processToCoreDocument(text);
        // tokenleri göster
        for (CoreLabel tok : document.tokens()) {
            System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
        }

        String text = "How could you be seeing into my eyes like open doors? \n"+
                "You led me down into my core where I've became so numb \n"+
                "Without a soul my spirit's sleeping somewhere cold \n"+
                "Until you find it there and led it back home \n"+
                "You woke me up inside \n"+
                "Called my name and saved me from the dark \n"+
                "You have bidden my blood and it ran \n"+
                "Before I would become undone \n"+
                "You saved me from the nothing I've almost become \n"+
                "You were bringing me to life \n"+
                "Now that I knew what I'm without \n"+
                "You can've just left me \n"+
                "You breathed into me and made me real \n"+
                "Frozen inside without your touch \n"+
                "Without your love, darling \n"+
                "Only you are the life among the dead \n"+
                "I've been living a lie, there's nothing inside \n"+
                "You were bringing me to life.";
        StanfordLemmatizer slem = new StanfordLemmatizer();
        System.out.println(slem.lemmatize(text));
    }
}