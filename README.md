# rudraksh

1 Group Members

* Kapil Thakkar (2014MCS2124)
* Mohit Jain (2014MCS2800)

2 Goal

Design web based application to suggest local sight-seeing and things to do at location chosen by user.

2.1 Description

Users will provide web application some place which he/she wants to visit. The web app will crawl through blogs related to that place and extract the common entities such as Taj Mahal in Agra or Bungee Jumping in Auckland. The web app will also provide reviews of accommodation and food if available. Web app will provide ratings for each of the entities based on sentiment analysis. Nearby places will also be suggested by making use of Knowledge Graph.

2.2 Dataset

Data will be fetched through blogs related to searched location, on the internet. To start with, we have written crawlers to fetch blogs from three different sources. We wish to automate this process so that top 100(to say) blogs thrown by search engine can be processed on the fly.

2.3 Literature/Code Search

Scrapy/Selenium will be used to fetch the blog data. Existing entity extraction tools such as Alchemy will be explored.

2.4 Evaluation

Since our project does not involve classification tasks, we would not be able to give numerical metrics. Henceforth, accuracy of the output produced, will be subjected to human assessment.


******** Directory Strucute ******************

* /scrapy-1.0 Documentation
	
	Scrapy is python tool used for creating corpus of Travel Blogs. This folder contains documentation of this library.

* /scrapy_tool

	This folder has Crawlers written in scrapy-python

* /Idea Doc

	Explains problem statement
