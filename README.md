# nutchUrlDumpAnalysis

The java program aims to provide extract the information from the Nutch url dump, and convert them form csv outputed from nutch to JSON format.
and The analysis is that we simply count the number of occurances for each url category.
The following is the definition of url categories in this program and note this program aims to provide the support to analyse crawled data in the project NSF polar scietifc.
https://github.com/NSF-Polar-Cyberinfrastructure/datavis-hackathon/issues/1 

Because there are many urls crawled and that reference the pages dynamically generated, it is probably better we group those based on the portion that comes before the "?";
The java program read the nutch dump produced in CSV format, and compute the count of occurances for categories.

1st, generate the url dump file with the following cmd.

./nutch readdb [crawldb] -dump [output_path] -format csv 

pass the dump file to the java program, the java program automatically compute the count of occurances for each categories and output them in json format.

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

This json file produced by the java program is then input to the R program for visualization.
The R program is a simple program to quickly visualize the data in pie chart and to give a quick feel with the data. 

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
    








