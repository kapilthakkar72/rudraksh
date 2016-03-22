# http://www.lakshmisharath.com/page/

import scrapy

class Blog4(scrapy.Spider):
    name = "blog4"
    start_urls = ["http://www.anitabora.com/blog/category/travel/"]
    
    
    def parse(self, response):
        # table = response.xpath('/html/body/div[3]/div[2]/div[2]/div[2]/div[2]/div[2]/div[2]/div/div[4]/div[1]/div/div/div[2]/div[1]/div/div/div[1]/div[1]/div[2]/div[1]')
        for sel in response.xpath('//a[@rel="bookmark"]'):
            link = sel.xpath('a/@href').extract()
            print link
            # yield scrapy.Request(str(link[0]), callback=self.blog_page)
            
            
    def blog_page(self, response):
        date = response.xpath('/html/body/div[2]/div[2]/div[2]/div/div[1]/article/header/div/a[16]/time').extract()
        date = str(date[0])
        title = response.xpath('/html/body/div[2]/div[2]/div[2]/div/div[1]/article/header/h1/text()').extract()
        title = str(title[0])
        content = ""
        
        contentB = response.xpath('/html/body/div[2]/div[2]/div[2]/div/div[1]/article/section[class="entry-content "]')
        for contentBody in contentB.xpath('.//p'):
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