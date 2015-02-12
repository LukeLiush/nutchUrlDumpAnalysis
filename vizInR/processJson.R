
rm(list=ls())
library(rjson)

displayPie<-function(f, d=10){
	jsonlist <- fromJSON(file=f)[[1]]
	m <- length(jsonlist)
	cvect <- sapply(jsonlist[1:m], function(li){as.numeric(li$count)})
	nvect <- sapply(jsonlist[1:m], function(li){paste(li$count, li$category )})
	selection <- cvect>d
	pie(cvect[selection] , labels=nvect[selection])
}


	