# nutchUrlDumpAnalysis

The java program aims to extract the information from the Nutch url dump, and convert them form csv outputed from nutch to JSON format.
and we simply count the number of occurances for each url category.
note this program aims to provide the support to analyse the crawled data in the project NSF polar research.
https://github.com/NSF-Polar-Cyberinfrastructure/datavis-hackathon/issues/1 

There are many urls crawled and that reference the pages dynamically generated, it is probably better we group those based on the portion that comes before the "?"; 
The java program read the nutch dump file produced in CSV format, and compute the count of occurances for the url categories.

1st, generate the url dump file with the following cmd in the nutch machine.

./<nutch_home>/runtime/local/bin/nutch readdb [crawldb] -dump [output_path] -format csv 

pass the dump file to the java program, the java program automatically compute the count of occurances for each categories and output them in json format. The following shows a snippet of the resultant json file.

{
    "unfetched_categories": [
        {
            "category": "https://www.aoncadis.org/dataset/CSASN_I8Inlet_bkg_conductivity_2010.01.html?",
            "count": "1",
            "isDynamic": "true"
        },
        {
            "category": "https://www.aoncadis.org/dataset/ActiveLayerArcssAtq2014.html?",
            "count": "1",
            "isDynamic": "true"
        },
        {
            "category": "https://www.aoncadis.org/dataset/interannualvariability_barrow_nearshore.html?",
            "count": "1",
            "isDynamic": "true"
        },
        ...
        ]
}
Technically, 
The java program extract the entries associated with "db_fetched" i.e. status code = 1, and there are two main types of urls 

i.e.
1) dynamic urls reference dynamic pages (those urls all have "?" inside them)
The following is an example.
http://gcmd.gsfc.nasa.gov/KeywordSearch/Home.do?Portal=amd_cl&MetadataType=0&lbnode=mdlb3
The program groups and clusters by looking at the portion before the "?" and count those urls that have the same "url portion", the portion after "?" will be refered to as a application portion. The following is an example of the url portion which you will be seeing in the produced json.
http://gcmd.gsfc.nasa.gov/KeywordSearch/Home.do? 

2) static urls reference static pages (those urls that do not have "?")
e.g. the following is an example.
http://gcmd.gsfc.nasa.gov/KeywordSearch/ipy/images/header.gif
For those urls, the program will take the portion before the last "/" as the url portion and group those that have the same url portion. e.g. the above url has the following url portion and count is computed based on those that have the same portion as follows.
http://gcmd.gsfc.nasa.gov/KeywordSearch/ipy/images/


This json file produced by the java program is then the input to the R program for visualization.
The R program has many useful builtin functions, and as an example, a simple R program that quickly visualizes the data in a pie chart is attached.

With a pie chart, it might be easier to see what are the major categories that nutch fails to fetch.

Notice, the program aims to analyse the unfetched urls in the dump.

How to Run:
Java
main: NutchDumpSummaryMain.class
2 parameters (currently not configurable): 
        #1: nutchCSVDumpPath: specifies the location of csv dump file
		#2: summaryOutputPath: specifies the location of the json output file.
Dependencies: Tika 1.6


R
Dependencies: library(rjson)
main:processJson.R
    source('processJson.R')
    displayPie('acadis.json',3)
    # there 3 parameters for displayPie
        #1: file: indicate the location of the json file to be displayed in pie
        #2: countThreshold: control which categories need to be shown based on the count, if setting the threshold =3 (by default it is 10), those categories that do have a count of 10 will be shown in the pie. Usually there are lots of url that only have count 1 which make it difficult to show them all in the pie, it is sometimes better to visualize the the majority of the group that nutch fails to fetch.
        #3: main, this specifies the title or description of the pie.
    








