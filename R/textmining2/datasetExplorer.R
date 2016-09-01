library(foreign)
dataset <- read.arff("../../GenerateBOWBaseline/data/hispatweets/tweets-training-es-country.arff")

#Calculating the means for each N-gram
medias <- colMeans(dataset[1:1008])

#Ordering means
mediasSort <- sort(medias, decreasing=TRUE)

print(mediasSort[1:5])

barras.ngrams<-barplot(mediasSort[1:5],col=rainbow(5), xlab="N-grams", ylab="Frequency")
text(barras.ngrams,mediasSort[1:5] + 1, labels=Tabla, xpd=TRUE)
