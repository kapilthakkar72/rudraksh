import scrapy

class Blog2(scrapy.Spider):
    name = "blog2"
    
    url_generator = "http://www.shadowsgalore.com/blog/page/"
    start_urls = []
    for i in range(1,87):
        start_urls.append(url_generator+str(i)+"/")
    
    
    def parse(self, response):
        for sel in response.xpath('//a[@class="read-more-button"]'):
            link = sel.xpath('@href').extract()
            print link
            yield scrapy.Request(str(link[0]), callback=self.blog_page)
            
    def blog_page(self, response):
        date = response.xpath('/html/body/div[1]/div/div/div/div[1]/article/div[1]/div/p/span[1]/text()').extract()
        date = str(date[0].encode('ascii','ignore').decode('ascii'))
        title = response.xpath('/html/body/div[1]/div/div/div/div[1]/article/div[1]/h1/text()').extract()
        title = str(title[0].encode('ascii','ignore').decode('ascii'))
        content = ""
        for contentBody in response.xpath('//p'):
            temp = contentBody.xpath('text()').extract()
            if(len(temp) > 0):
                temp = temp[0]
                temp = temp.encode('ascii','ignore').decode('ascii')
                content += temp
        print content
        '''
        # Not getting fetched... dont know why...
        commentContent = ""
        commentArea = response.xpath('/html/body/div[3]/section/div[2]/ul')
        for comments in commentArea.xpath('.//p'):
            temp = para.xpath('text()').extract()
            if(len(temp) > 0):
                temp = temp[0]
                temp = temp.encode('ascii','ignore').decode('ascii')
                commentContent += temp
        '''