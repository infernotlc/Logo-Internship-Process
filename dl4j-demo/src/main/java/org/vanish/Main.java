package org.vanish;


import com.opencsv.CSVWriter;
import org.apache.log4j.BasicConfigurator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.PerformanceListener;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.util.*;

// Verilen metin girdisine göre, modelin hangi kategoriye (örneğin, "company_info", "all_products" vb.)
// ait olduğunu tahmin edebilir ve buna uygun bir yanıt üretebilir.
public class Main {
    public static ArrayList<String> stopWords;
    public static StanfordLemmatizer slem = new StanfordLemmatizer();

    public static int FEATURES_COUNT = 73;
    public static int CLASSES_COUNT = 7;
    public static int NUM_CHATS;

    public static Dictionary reply2Code = new Hashtable();
    public static String[] code2Reply = {"company_info",
            "all_products",
            "prosthesis_products",
            "exoskeleton_product",
            "demo_request",
            "buy_request",
            "end_logo"};
    public static String path = "D:/dl4j-demo/src/main/resources/chats.txt";
    public static String chat_path = "D:/dl4j-demo/src/main/resources/chats.csv";
    public static String vec_path = "D:/dl4j-demo/src/main/resources/chatVecs.csv";
    public static String chatbot_model = "D:/dl4j-demo/src/main/resources/chatbot_model";

    public static String replies_path = "D:/dl4j-demo/src/main/resources/bot_replies/";

    public static void SetUp() {
        Scanner s = null;
        try {
            s = new Scanner(new File("D:/dl4j-demo/src/main/resources/stopwords.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        stopWords = new ArrayList<String>();
        while (s.hasNext()) {
            stopWords.add(s.next().toLowerCase());
        }
        s.close();

        BasicConfigurator.configure();


        try {
            Word2Vec vec = trainingVec(path);
            generateWordVecsFile(chat_path, vec_path, vec);

            MultiLayerNetwork model = loadData(vec_path);
            ModelSerializer.writeModel(model, chatbot_model, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    public static Word2Vec trainingVec(String path) throws FileNotFoundException {
        //metin verilerinden kelime vektörleri oluşturur

        SentenceIterator iter = new LineSentenceIterator(new File(path));
        iter.setPreProcessor((SentencePreProcessor) sentence -> {
            List<String> tokens = slem.lemmatize(sentence);

            sentence = String.join(" ", tokens);
            return sentence.toLowerCase();
        });


        // kelimeleri almak için boşlukları kullan


        //token build
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(2)//2den az kelime frekansına sahip kelimeleri alma
                .layerSize(FEATURES_COUNT)//giris katmanı boyutu
                .seed(42)//model tekrarlanabilirliği tohum sayısı
                .windowSize(5)//kelime bağlamı için
                .iterate(iter)
                .tokenizerFactory(t)
                .stopWords(stopWords)
                .build();

        vec.fit();


        // kelime vektörlerini yaz
        try {
            WordVectorSerializer.writeWordVectors(vec, "vec.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vec;
    }

    public static INDArray sentenceVector(String sentence, Word2Vec vec) {
        //word2vec modelini kullanarak
        //gelen metni işler ve kelime vektörlerine dönüştürür.


        List<String> words = slem.lemmatize(sentence);
        INDArray sentVect = Nd4j.zeros(1, FEATURES_COUNT);
        for (String s : words) {
            String word = (String) s;
            INDArray e = vec.getWordVectorMatrix(word);

            if (e != null) {
                sentVect = sentVect.add(e);
            }
        }
        return sentVect;
    }

    public static void generateWordVecsFile(String csvFile, String modelFile, Word2Vec vec) {
        //eğitim verilerini kelime vektörlerine dönüştürür.ve csv dosyasını kaydeder
        try {
            FileWriter outputfile = new FileWriter(modelFile);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            File file = new File(csvFile);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] tempArr;
            NUM_CHATS = 0;
            while ((line = br.readLine()) != null) {
                tempArr = line.split(",");
                String sentence = (tempArr[0]);
                String out = tempArr[1];
                INDArray sentVec = sentenceVector(sentence, vec);
                int n = sentVec.shape()[1];
                boolean check_ok = true;
                for (int i = 0; i < n; i++) {
                    if (sentVec.getDouble(i) == 0d) {
                        check_ok = false;
                        break;
                    }
                }
                if (check_ok) {
                    String[] data = new String[n + 1];
                    for (int i = 0; i < n; i++) {
                        data[i] = String.valueOf(sentVec.getDouble(i));
                    }
                    data[n] = (String) reply2Code.get(out);
                    writer.writeNext(data);
                    NUM_CHATS += 1;
                }
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static MultiLayerNetwork loadData(String chatVecPath) {
        // eğitim verilerini yükler ve uygun formatta ayarlar
        try (RecordReader recordReader = new CSVRecordReader(0, ',')) {
            recordReader.initialize(new FileSplit(
                    new ClassPathResource("chatVecs.csv").getFile()
            ));


            //veriseti üzerinde gez
            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, NUM_CHATS - 2, FEATURES_COUNT, CLASSES_COUNT);
            DataSet allData = iterator.next();
            allData.shuffle(0);


            //veriyi böl
            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);
            DataSet trainingData = testAndTrain.getTrain();
            DataSet testingData = testAndTrain.getTest();


            //training çalıştırılması ve test
            System.out.println("\n\n\nInitiating Deep Learning");
            MultiLayerNetwork model = chatbotNetwork(trainingData, testingData);
            return model;

            //hata yakala
        } catch (Exception e) {
            Thread.dumpStack();
            new Exception("Stack trace").printStackTrace();
            System.out.println("Error: " + e.getLocalizedMessage());
        }

        return null;
    }

    private static MultiLayerNetwork chatbotNetwork(DataSet trainingData, DataSet testData) {
        //çok katmanlı sinir ağı oluşturur ve eğitimi yapar.Eğitim verilerinin model
        // tarafından işlenmesi modelin eğitilmesi ve performans değerlendirmesini içerir.
        System.out.println("\n\n\nModel Configuration");

        int layerSize = 32;
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
//                  .seed(0)
                .iterations(2000)
                .activation(Activation.RELU)//hızlı max(0,x)
                .weightInit(WeightInit.XAVIER)//agırlıkları rastgele baslatir
                .updater(new Adam())
                .l2(0.001)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(layerSize).build())
                .layer(1, new DenseLayer.Builder().nIn(layerSize).nOut(layerSize).build())
                .layer(2, new DenseLayer.Builder().nIn(layerSize).nOut(layerSize).build())
                .layer(3, new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)
                        .nIn(layerSize).nOut(CLASSES_COUNT).build())
                .backprop(true).pretrain(false)
                .build();

        System.out.println("\n\n\nModel Training");
        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.setListeners(new PerformanceListener(1, true));
        model.init();
        model.fit(trainingData);

        //eğitim verisi üzerinden hesaplama
        System.out.println("\n\nEvaluating over training data");
        INDArray output2 = model.output(trainingData.getFeatureMatrix());
        Evaluation eval2 = new Evaluation(CLASSES_COUNT);
        eval2.eval(trainingData.getLabels(), output2);
        System.out.printf(eval2.stats());

        //test verisi üzerinden hesaplama
        System.out.println("\n\nEvaluating over test data");
        INDArray output = model.output(testData.getFeatureMatrix());
        Evaluation eval = new Evaluation(CLASSES_COUNT);
        eval.eval(testData.getLabels(), output);
        System.out.printf(eval.stats());

        return model;

    }



    public static void main(String[] args) {

        for (int i = 0; i < code2Reply.length; i++) {
            reply2Code.put(code2Reply[i], String.valueOf(i));
        }
        System.out.println(reply2Code);

//        SetUp(); //her şeyi tekrar eğitmek için kaldır

        try {
            BasicConfigurator.configure();
            Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel("vec.txt");

            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(chatbot_model);

            String input_sentence;
            Scanner sc = new Scanner(System.in);
            INDArray input_vector;
            INDArray output;

            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            BufferedReader br = new BufferedReader(new FileReader(replies_path + "start_logo.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Logo:" + line);
            }

            String reply;
            boolean logo = true;
            while (logo) {
                System.out.print("User:");
                input_sentence = sc.nextLine();

                input_vector = sentenceVector(input_sentence, word2Vec);


                long startTime = System.nanoTime();
                output = model.output(input_vector);
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;

                reply = code2Reply[(int) Double.parseDouble(output.argMax().toString())];

                System.out.println("***" + duration + " ms ***");
                br = new BufferedReader(new FileReader(replies_path + reply + ".txt"));
                while ((line = br.readLine()) != null) {
                    System.out.println("Logo:" + line);
                }
                if (reply == "end_logo") {
                    logo = false;
                }


            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}