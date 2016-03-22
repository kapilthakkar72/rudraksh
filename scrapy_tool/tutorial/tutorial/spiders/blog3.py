# http://traveltalesfromindia.in/more-stories/page/2/

import scrapy

class Blog3(scrapy.Spider):
    name = "blog3"
    
    url_generator = "http://traveltalesfromindia.in/more-stories/page/"
    start_urls = []
    for i in range(1,30):
        start_urls.append(url_generator+str(i)+"/")
    
    
    def parse(self, response):
        for sel in response.xpath('/html/body/div[2]/div/div[1]/div/div/div/ul/li'):
            link = sel.xpath('a/@href').extract()
            print link
            yield scrapy.Request(str(link[0]), callback=self.blog_page)
            
    def blog_page(self, response):
        date = response.xpath('/html/body/div/div/div[1]/div/div[1]/div[1]/span[2]/text()').extract()
        date = str(date[0])
        title = response.xpath('/html/body/div/div/div[1]/div/div[1]/h3/a/text()').extract()
        title = str(title[0])
        content = ""
        
        contentB = response.xpath('/html/body/div/div/div[1]/div/div[1]')
        divs = contentB.xpath('.//div')
        for contentBody in divs.xpath('.//p'):
            temp = contentBody.xpath('text()').extract()
            if(len(temp) > 0):
                temp = temp[0]
                temp = temp.encode('ascii','ignore').decode('ascii')
                content += temp
        print content     
        # Comments
        for comment in response.xpath('/html/body/div/div/div[1]/div/div[3]/div/ol/li'):
            commentDateTime = comment.xpath('article/footer/div[2]/a/time/text()').extract()
            commentDateTime = str(commentDateTime[0]).strip()
            tempList = commentDateTime.split(' at ')
            commentDate = tempList[0]
            commentTime = tempList[1]
            commentContent = ""
            for para in comment.xpath('article/div[1]/p'):
                temp = para.xpath('text()').extract()
                if(len(temp) > 0):
                    temp = temp[0]
                    temp = temp.encode('ascii','ignore').decode('ascii')
                    commentContent += temp
            print commentContent