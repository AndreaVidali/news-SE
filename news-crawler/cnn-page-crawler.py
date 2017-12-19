import urllib.request
from bs4 import BeautifulSoup

# url = "https://en.wikipedia.org/wiki/List_of_state_and_union_territory_capitals_in_India"
# url = "https://www.nytimes.com/2017/12/12/world/europe/belgium-electricity.html?smid=tw-nytimes&smtyp=cur"
# url = "https://www.nytimes.com/2017/12/13/us/politics/tax-bill-republicans-deal.html?hp&action=click&pgtype=Homepage&clickSource=image&module=b-lede-package-region&region=top-news&WT.nav=top-news"
# url = "https://www.nytimes.com/interactive/2017/12/14/climate/republicans-global-warming-maps.html?hp&action=click&pgtype=Homepage&clickSource=story-heading&module=second-column-region&region=top-news&WT.nav=top-news"
# url = "https://t.co/dM34E1OHKU"
# url = "https://t.co/EqfoMZVZhb"
url = "https://t.co/6WKilAdtVz"
#url = "http://www.cnn.com/2017/12/18/politics/trump-administration-immigration-hardline/index.html?sr=twCNN121817trump-administration-immigration-hardline0725PMVODtop"
url = "https://t.co/CMeuLd6Y8q"
page = urllib.request.urlopen(url)

soup = BeautifulSoup(page, 'lxml')

#print(soup.prettify())

with open('cnn-page.txt', 'w', encoding="utf8") as outfile:
    outfile.write(soup.prettify())


p_par = soup.find_all('p')
div_par = soup.find_all('div')

for par in p_par:
    print(par)

for div in div_par:
    print(div)
    #print(div.text)


text_end = bool(0)

for idx, div in enumerate(div_par):
    if idx > 11 and not text_end:
        if div.attrs != {'class': ['zn-body__paragraph zn-body__footer">', '']}:
            print(div.text)
        else:
            text_end = bool(1)


# prendere prima riga di ogni file txt del testo della notizia così si evita il par sotto per l'help
# aggiungere campi nel file della news e magari mettere link + teto + caption twitter nella stessa riga
