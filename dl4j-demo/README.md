# Logo CHATBOT
Bu maven projesinde deeplearning4j ve StanfordCoreNLP kutuphaneleri kullanilarak chatbot olusturulmustur

## Gereklilikler
- JDK 1.8 or sonrasi (1.8 kullanildi)
- Apache Maven
- IntelliJ IDEA/ Eclipse (IntelliJ kullanildi)
- Git

## Maven Kutuphaneleri
- apache.cassandra.all (4.0.5 kullanilan)
- deeplearning4j.core (0.9.1 kullanilan)
- nd4j.native.platform (0.9.1 kullanilan)
- deeplearning4j.vertx (1.0.0-M2.1 kullanilan)
- edu.stanford.nlp.corenlp (4.4.0 kullanilan)
- opencsv (4.1 kullanildi)

## Deeplearning4j Hakkinda
Isminden anlasilacagi gibi kutuphane javada yazilmis olup Kotlin, Scala gibi jvm dilleri ile uyumludur ile uyumludur. Bu acik kaynakli kutuphane, Apache Spark ve Hadoop gibi en son dagitilmis hesaplama frameworklerine sahiptir. Bu da avantaji olarak sayilabilir.
Bazi ozellikleri:

- Açik kaynak, genel kullanim
- Gelistirme ortamlarina yapay zekayi katmak
- Detayli API dokumani
- Bircok dilde ornek proje
- Hadoop ve Apache Spark ile uyumlu

## Daha Cok Bilgi ve Referans
- https://medium.com/datactw/deep-learning-for-java-dl4j-getting-started-tutorial-2259c76c0a7c
- https://deeplearning4j.konduit.ai/multi-project/tutorials/beginners 
- https://www.youtube.com/c/Deeplearning4jSkymind 
- https://stanfordnlp.github.io/CoreNLP/
- https://stackoverflow.com/questions/1578062/lemmatization-java


## Projeyi Calistirmak:
- Kaynak dosya yollarini kendi bilgisayarlarinizdaki yerleriyle uygun olarak degistirin.
- Main sinifini calistirmadan once hangi cevaplarin chatbotu sonlandirdigini kontrol edebilirsiniz.
- Sorulari inceleyip benzer sorular sorarak cevaplari alabilirsiniz.
- Cevap gruplarina gore etiketleme yapilmis olup sordugunuz sorunun kelime vektorune yakinligini bulmakta ve buna bagli yanit grubundan cevap secmektedir.

