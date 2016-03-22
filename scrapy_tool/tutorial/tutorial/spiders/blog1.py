import scrapy
count = 0
class Blog1(scrapy.Spider):
    name = "blog1"
    allowed_domains = ["wordpress.com"]
    start_urls = [
        "https://thatandthisinmumbai.wordpress.com/"
    ]
    
    def parse(self, response):
        i = 0
        for sel in response.xpath('/html/body/div[1]/div/div[2]/aside[11]/select/option'):
            link = sel.xpath('@value').extract()
            if(i==0):
                i=1
                continue
            yield scrapy.Request(str(link[0]), callback=self.parse_dir_contents)
            
    def parse_dir_contents(self, response):
        global count
        for sel in response.xpath('//a[@class="more-link"]'):
            link = sel.xpath('@href').extract()
            count = count+1
            print count
            # yield scrapy.Request(str(link[0]), callback=self.blog_page)
        
    
    def blog_page(self, response):
        date = response.xpath('/html/body/div[1]/div/div/div/article/header/div/a/time/text()').extract()
        date = str(date[0].encode('ascii','ignore').decode('ascii'))
        title = response.xpath('/html/body/div[1]/div/div/div/article/header/h1/text()').extract()
        title = str(title[0].encode('ascii','ignore').decode('ascii'))
        content = ""
        for para in response.xpath('/html/body/div[1]/div/div/div/article/div[@class="entry-content"]/p'):
            temp = para.xpath('text()').extract()
            if(len(temp) > 0):
                temp = temp[0]
                temp = temp.encode('ascii','ignore').decode('ascii')
                content += temp
        print "TITLE : " +title
        print "DATE: " + date
        print "content: " +content
       
        for comment in response.xpath('/html/body/div[1]/div/div/div/div/ol/li'):
            commentDateTime = comment.xpath('article/footer/div/a/time/text()').extract()
            commentDateTime = str(commentDateTime[0])
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
            print "comment" + commentContent
        pass
    